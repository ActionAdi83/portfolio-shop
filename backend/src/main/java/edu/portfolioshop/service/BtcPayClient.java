package edu.portfolioshop.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.portfolioshop.config.BtcPayProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin wrapper over the BTCPay Greenfield API.
 *
 * Copied near-verbatim from fanvote's edu.fanvote.service.BtcPayClient, minus the
 * payout call — this shop only ever opens and reads back invoices, it never pays
 * anyone out.
 */
@Service
@RequiredArgsConstructor
public class BtcPayClient {

    private static final Logger log = LoggerFactory.getLogger(BtcPayClient.class);

    /** Field name matches the bean name so injection stays unambiguous. */
    private final RestTemplate btcPayRestTemplate;
    private final BtcPayProperties props;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Invoice(
            String id,
            String storeId,
            BigDecimal amount,
            String currency,
            String checkoutLink,
            String status,
            Map<String, Object> metadata) {
    }

    public Invoice createInvoice(BigDecimal amount,
                                 String currency,
                                 String redirectUrl,
                                 Map<String, String> metadata) {
        Map<String, Object> checkout = new LinkedHashMap<>();
        checkout.put("expirationMinutes", props.getInvoiceExpirationMinutes());
        checkout.put("redirectAutomatically", true);
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            checkout.put("redirectURL", redirectUrl);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount.toPlainString());
        body.put("currency", currency);
        body.put("metadata", metadata);
        body.put("checkout", checkout);

        return exchange(HttpMethod.POST, "/api/v1/stores/" + props.getStoreId() + "/invoices", body, Invoice.class);
    }

    public Invoice getInvoice(String invoiceId) {
        return exchange(HttpMethod.GET,
                "/api/v1/stores/" + props.getStoreId() + "/invoices/" + invoiceId, null, Invoice.class);
    }

    private <T> T exchange(HttpMethod method, String path, Object body, Class<T> responseType) {
        if (props.getUrl() == null || props.getUrl().isBlank()
                || props.getApiKey() == null || props.getApiKey().isBlank()
                || props.getStoreId() == null || props.getStoreId().isBlank()) {
            throw new IllegalStateException(
                    "BTCPay is not configured — set BTCPAY_URL, BTCPAY_API_KEY and BTCPAY_STORE_ID");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, "token " + props.getApiKey());

        String url = props.getUrl().replaceAll("/+$", "") + path;
        try {
            return btcPayRestTemplate.exchange(url, method, new HttpEntity<>(body, headers), responseType).getBody();
        } catch (HttpStatusCodeException e) {
            // The response body carries BTCPay's own error message; without it the
            // stack trace only ever says "400 Bad Request".
            log.error("BTCPay {} {} failed: {} {}", method, path, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("BTCPay call failed: " + e.getStatusCode(), e);
        }
    }
}
