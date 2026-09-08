package edu.portfolioshop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.portfolioshop.config.BtcPayProperties;
import edu.portfolioshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/**
 * Receives invoice events from BTCPay.
 *
 * Copied verbatim (signature verification is security-critical) from fanvote's
 * edu.fanvote.controllers.BtcPayWebhookController. This endpoint is unauthenticated
 * by necessity — BTCPay cannot present a token — so the HMAC signature is the only
 * thing separating a real settlement from anyone who can reach the URL. It is
 * verified over the exact bytes that arrived, before the body is parsed.
 */
@RestController
@RequestMapping("/api/payments/btcpay")
@RequiredArgsConstructor
public class BtcPayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BtcPayWebhookController.class);

    private final BtcPayProperties props;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<?> handle(@RequestBody byte[] payload,
                                    @RequestHeader(value = "BTCPay-Sig", required = false) String signature) {
        if (props.getWebhookSecret() == null || props.getWebhookSecret().isBlank()) {
            log.error("Webhook received but BTCPAY_WEBHOOK_SECRET is not set — rejecting");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Webhook secret not configured"));
        }
        if (!signatureMatches(payload, signature)) {
            log.warn("Rejected BTCPay webhook with a bad signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid signature"));
        }

        try {
            JsonNode event = objectMapper.readTree(payload);
            String type = event.path("type").asText();
            String invoiceId = event.path("invoiceId").asText();

            if (invoiceId.isBlank()) {
                return ResponseEntity.ok(Map.of("status", "ignored"));
            }

            switch (type) {
                case "InvoiceSettled" -> paymentService.settleInvoice(invoiceId);
                case "InvoiceProcessing" -> paymentService.markStatus(invoiceId, "PROCESSING");
                case "InvoiceExpired" -> paymentService.markStatus(invoiceId, "EXPIRED");
                case "InvoiceInvalid" -> paymentService.markStatus(invoiceId, "INVALID");
                // InvoiceCreated and the payment-level events carry nothing this
                // application acts on; acknowledging them stops BTCPay retrying.
                default -> log.debug("Ignoring BTCPay event {}", type);
            }
            return ResponseEntity.ok(Map.of("status", "processed"));
        } catch (Exception e) {
            // A 5xx makes BTCPay redeliver, which is what we want for a transient fault.
            log.error("Failed to process BTCPay webhook", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Processing failed"));
        }
    }

    private boolean signatureMatches(byte[] payload, String signature) {
        if (signature == null || payload == null) {
            return false;
        }
        // BTCPay sends "sha256=<hex>"; older builds send the bare hex digest.
        String provided = signature.startsWith("sha256=") ? signature.substring("sha256=".length()) : signature;

        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(props.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = hmac.doFinal(payload);
            byte[] actual = HexFormat.of().parseHex(provided.trim().toLowerCase());
            // Constant-time: a String.equals here leaks the digest one byte at a time.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false; // not valid hex
        } catch (Exception e) {
            log.error("Could not verify webhook signature", e);
            return false;
        }
    }
}
