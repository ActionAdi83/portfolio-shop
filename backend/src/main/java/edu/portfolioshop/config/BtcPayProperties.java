package edu.portfolioshop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection details for the self-hosted BTCPay Server instance.
 *
 * Everything here comes from the environment — no value may be committed with a
 * default that works. Mirrors fanvote's edu.fanvote.config.BtcPayProperties, minus
 * the payout-related fields: this shop never pays anyone out, it only ever
 * receives payment for an order.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "btcpay")
public class BtcPayProperties {

    /** Base URL of the BTCPay instance, e.g. http://btcpay:49392 */
    private String url;

    /**
     * Base URL the browser reaches BTCPay on, when that differs from {@link #url}.
     *
     * BTCPay builds the checkout link in its response from the Host header of the
     * call that created the invoice, not from its own BTCPAY_EXTERNALURL, so a link
     * opened from a container-network call comes back pointing at a hostname no
     * browser can resolve and has to be rewritten before it is handed to a
     * customer. Left blank, no rewriting happens.
     */
    private String publicUrl;

    /** Store the shop's invoices are created against. */
    private String storeId;

    /** Greenfield API key, sent as "Authorization: token <key>". */
    private String apiKey;

    /** Shared secret BTCPay signs webhook deliveries with. */
    private String webhookSecret;

    /** Fiat currency prices are denominated in. */
    private String currency = "USD";

    /** How long a customer has to pay before the invoice expires. */
    private int invoiceExpirationMinutes = 60;
}
