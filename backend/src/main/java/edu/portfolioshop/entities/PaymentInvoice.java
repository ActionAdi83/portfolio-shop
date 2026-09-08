package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One BTCPay invoice opened against an {@link Order}, mirroring fanvote's
 * PaymentInvoice for the same reason: the BTCPay id is unique so a redelivered
 * webhook can never create a second record, and {@link #orderMarkedPaid} makes the
 * grant (here, marking the order PAID) idempotent.
 */
@Getter
@Setter
@Document(collection = "payment_invoices")
public class PaymentInvoice {

    @Id
    private String id;

    @Indexed(unique = true)
    private String btcpayInvoiceId;

    private String orderId;

    private BigDecimal amount;
    private String currency;

    /** NEW | PROCESSING | SETTLED | EXPIRED | INVALID */
    private String status;

    private String checkoutLink;

    private Instant createdAt;
    private Instant settledAt;

    /** True once the order behind this invoice was actually marked PAID. */
    private boolean orderMarkedPaid;
}
