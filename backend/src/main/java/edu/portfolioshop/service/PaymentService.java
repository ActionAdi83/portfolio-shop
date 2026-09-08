package edu.portfolioshop.service;

import edu.portfolioshop.config.BtcPayProperties;
import edu.portfolioshop.entities.Order;
import edu.portfolioshop.entities.OrderStatus;
import edu.portfolioshop.entities.PaymentInvoice;
import edu.portfolioshop.repository.PaymentInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Order payments, settled through the self-hosted BTCPay Server.
 *
 * Adapted from fanvote's edu.fanvote.service.PaymentService: an order is only ever
 * marked PAID from {@link #settleInvoice(String)}, which runs off a signed webhook
 * and re-reads the invoice from BTCPay before trusting anything about it — the
 * same idempotent re-fetch-and-claim pattern, minus subscriptions and credit.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final BtcPayClient btcPay;
    private final BtcPayProperties props;
    private final PaymentInvoiceRepository invoices;
    private final OrderService orderService;
    private final MongoTemplate mongoTemplate;

    /**
     * Opens an invoice for an order and returns the record holding the checkout
     * link to send the customer to.
     */
    public PaymentInvoice openInvoice(String orderId, String redirectUrl) {
        Order order = orderService.findById(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Order " + orderId + " is already paid");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order " + orderId + " is cancelled");
        }

        var amount = order.totalAmount();

        BtcPayClient.Invoice created = btcPay.createInvoice(
                amount,
                props.getCurrency(),
                redirectUrl,
                Map.of("orderId", orderId));

        PaymentInvoice invoice = new PaymentInvoice();
        invoice.setBtcpayInvoiceId(created.id());
        invoice.setOrderId(orderId);
        invoice.setAmount(amount);
        invoice.setCurrency(props.getCurrency());
        invoice.setStatus(created.status() == null ? "NEW" : created.status().toUpperCase());
        invoice.setCheckoutLink(browserReachable(created.checkoutLink()));
        invoice.setCreatedAt(Instant.now());
        invoice.setOrderMarkedPaid(false);

        PaymentInvoice saved = invoices.save(invoice);
        orderService.markAwaitingPayment(orderId, created.id());
        return saved;
    }

    /**
     * Swaps the internal BTCPay origin in a checkout link for the one the browser
     * can reach — see fanvote's PaymentService#browserReachable for the full
     * reasoning. Only the origin is replaced; the invoice path is BTCPay's own.
     */
    private String browserReachable(String checkoutLink) {
        String publicUrl = props.getPublicUrl();
        if (checkoutLink == null || publicUrl == null || publicUrl.isBlank()) {
            return checkoutLink;
        }
        String internal = props.getUrl() == null ? "" : props.getUrl().replaceAll("/+$", "");
        if (internal.isBlank() || !checkoutLink.startsWith(internal)) {
            return checkoutLink;
        }
        return publicUrl.replaceAll("/+$", "") + checkoutLink.substring(internal.length());
    }

    public PaymentInvoice findByBtcpayId(String btcpayInvoiceId) {
        return invoices.findByBtcpayInvoiceId(btcpayInvoiceId).orElse(null);
    }

    /**
     * Marks the order behind a settled invoice PAID.
     *
     * Safe to call repeatedly: BTCPay retries deliveries and replays are trivially
     * forgeable at the network level, so the grant is guarded by
     * {@code orderMarkedPaid} on the invoice, claimed with an atomic
     * find-and-modify before anything is granted.
     */
    public void settleInvoice(String btcpayInvoiceId) {
        PaymentInvoice invoice = invoices.findByBtcpayInvoiceId(btcpayInvoiceId).orElse(null);
        if (invoice == null) {
            log.warn("Webhook for unknown invoice {} — ignoring", btcpayInvoiceId);
            return;
        }
        if (invoice.isOrderMarkedPaid()) {
            log.debug("Invoice {} already granted — ignoring redelivery", btcpayInvoiceId);
            return;
        }

        // Never trust the webhook body for money. Ask BTCPay what it actually holds.
        BtcPayClient.Invoice remote = btcPay.getInvoice(btcpayInvoiceId);
        if (remote == null || !"Settled".equalsIgnoreCase(remote.status())) {
            log.warn("Invoice {} is {} at BTCPay, not Settled — refusing to grant",
                    btcpayInvoiceId, remote == null ? "missing" : remote.status());
            return;
        }
        if (remote.amount() == null || remote.amount().compareTo(invoice.getAmount()) < 0) {
            log.warn("Invoice {} settled for {} but {} was expected — refusing to grant",
                    btcpayInvoiceId, remote.amount(), invoice.getAmount());
            return;
        }

        // Claim the invoice before granting anything, so two deliveries arriving
        // together cannot both get past here — a read-then-write on the flag would.
        PaymentInvoice claimed = mongoTemplate.findAndModify(
                Query.query(Criteria.where("btcpayInvoiceId").is(btcpayInvoiceId)
                        .and("orderMarkedPaid").is(false)),
                new Update()
                        .set("orderMarkedPaid", true)
                        .set("status", "SETTLED")
                        .set("settledAt", Instant.now()),
                PaymentInvoice.class);
        if (claimed == null) {
            log.debug("Invoice {} was already claimed by another delivery", btcpayInvoiceId);
            return;
        }

        // Past this point the invoice is marked done. If the work below throws, that
        // mark has to come back off: the webhook answers 5xx, BTCPay redelivers, and
        // a claim left standing would make the retry decide there was nothing to do —
        // turning a transient fault into a payment that silently vanished.
        try {
            orderService.markPaid(invoice.getOrderId());
            log.info("Invoice {} settled — order {} marked PAID", btcpayInvoiceId, invoice.getOrderId());
        } catch (RuntimeException e) {
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("btcpayInvoiceId").is(btcpayInvoiceId)),
                    new Update().set("orderMarkedPaid", false),
                    PaymentInvoice.class);
            log.error("Settling invoice {} failed; released the claim so a redelivery can retry",
                    btcpayInvoiceId, e);
            throw e;
        }
    }

    public void markStatus(String btcpayInvoiceId, String status) {
        invoices.findByBtcpayInvoiceId(btcpayInvoiceId).ifPresent(invoice -> {
            invoice.setStatus(status);
            invoices.save(invoice);
        });
    }
}
