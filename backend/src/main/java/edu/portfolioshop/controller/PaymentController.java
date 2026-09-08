package edu.portfolioshop.controller;

import edu.portfolioshop.config.BtcPayProperties;
import edu.portfolioshop.dto.InvoiceCreateRequest;
import edu.portfolioshop.dto.InvoiceResponse;
import edu.portfolioshop.entities.PaymentInvoice;
import edu.portfolioshop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService payments;
    private final BtcPayProperties props;

    /** Opens a BTCPay invoice for an order and returns the checkout link. Answers
     * 503 rather than crashing if BTCPay has not been configured, same as fanvote. */
    @PostMapping("/invoices")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        if (props.getUrl() == null || props.getUrl().isBlank()
                || props.getApiKey() == null || props.getApiKey().isBlank()
                || props.getStoreId() == null || props.getStoreId().isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Payments are not configured on this server yet"));
        }

        PaymentInvoice invoice = payments.openInvoice(request.orderId(), null);
        return ResponseEntity.ok(new InvoiceResponse(invoice.getBtcpayInvoiceId(), invoice.getCheckoutLink(), invoice.getStatus()));
    }
}
