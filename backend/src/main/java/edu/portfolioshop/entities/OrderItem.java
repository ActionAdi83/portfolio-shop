package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** One line of an order. {@link #unitPrice} is a snapshot taken when the order was
 * placed — it must not move if the product's price changes afterwards. */
@Getter
@Setter
public class OrderItem {

    private String productId;

    /** Snapshot of the product's name at order time, so history reads fine even if
     * the product is later renamed or deleted. */
    private String productName;

    private int quantity;

    private BigDecimal unitPrice;
}
