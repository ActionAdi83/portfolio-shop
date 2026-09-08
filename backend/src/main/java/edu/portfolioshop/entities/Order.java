package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    /** Keycloak `sub` of the buyer. */
    private String userId;

    private List<OrderItem> items = new ArrayList<>();

    private ShippingAddress shippingAddress;

    private OrderStatus status = OrderStatus.NEW;

    /** Set once a BTCPay invoice has been opened for this order. */
    private String btcpayInvoiceId;

    private Instant createdAt;

    public BigDecimal totalAmount() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
