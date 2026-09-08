package edu.portfolioshop.service;

import edu.portfolioshop.dto.AddressRequest;
import edu.portfolioshop.dto.OrderCreateRequest;
import edu.portfolioshop.entities.Address;
import edu.portfolioshop.entities.Order;
import edu.portfolioshop.entities.OrderItem;
import edu.portfolioshop.entities.OrderStatus;
import edu.portfolioshop.entities.Product;
import edu.portfolioshop.entities.ShippingAddress;
import edu.portfolioshop.repository.AddressRepository;
import edu.portfolioshop.repository.OrderRepository;
import edu.portfolioshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final AddressRepository addresses;

    public List<Order> findByUser(String userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order findOwned(String id, String userId) {
        return orders.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("No order " + id + " for this account"));
    }

    public List<Order> findAll() {
        return orders.findAllByOrderByCreatedAtDesc();
    }

    public Order findById(String id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("No order " + id));
    }

    /**
     * Builds an order from cart contents and a shipping address, snapshotting each
     * line's price and name, and taking the ordered quantity out of stock.
     *
     * TODO (follow-up): this is not transactional against a replica-set-less
     * standalone Mongo, so a crash between the stock decrement and the order save
     * could in principle drop stock without an order to show for it. Acceptable
     * for a first iteration; a production version would use a Mongo transaction
     * or a compensating reconciliation job.
     */
    public Order createOrder(String userId, OrderCreateRequest request) {
        if (request.items().isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one item");
        }

        List<OrderItem> items = new ArrayList<>();
        for (OrderCreateRequest.Item requested : request.items()) {
            if (requested.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for " + requested.productId());
            }
            Product product = products.findById(requested.productId())
                    .orElseThrow(() -> new NoSuchElementException("No product " + requested.productId()));
            if (product.getStock() < requested.quantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for " + product.getName() + ": " + product.getStock() + " left");
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(requested.quantity());
            item.setUnitPrice(product.getPrice());
            items.add(item);

            product.setStock(product.getStock() - requested.quantity());
            products.save(product);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setItems(items);
        order.setShippingAddress(resolveShippingAddress(userId, request));
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(Instant.now());
        return orders.save(order);
    }

    private ShippingAddress resolveShippingAddress(String userId, OrderCreateRequest request) {
        if (request.addressId() != null && !request.addressId().isBlank()) {
            Address saved = addresses.findByIdAndUserId(request.addressId(), userId)
                    .orElseThrow(() -> new NoSuchElementException("No address " + request.addressId() + " for this account"));
            return toShippingAddress(saved);
        }
        AddressRequest inline = request.shippingAddress();
        if (inline == null) {
            throw new IllegalArgumentException("Either addressId or shippingAddress is required");
        }
        ShippingAddress address = new ShippingAddress();
        address.setLabel(inline.label());
        address.setLine1(inline.line1());
        address.setLine2(inline.line2());
        address.setCity(inline.city());
        address.setPostalCode(inline.postalCode());
        address.setCountry(inline.country());
        return address;
    }

    private ShippingAddress toShippingAddress(Address saved) {
        ShippingAddress address = new ShippingAddress();
        address.setLabel(saved.getLabel());
        address.setLine1(saved.getLine1());
        address.setLine2(saved.getLine2());
        address.setCity(saved.getCity());
        address.setPostalCode(saved.getPostalCode());
        address.setCountry(saved.getCountry());
        return address;
    }

    public Order updateStatus(String id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        return orders.save(order);
    }

    /** Records the BTCPay invoice opened for a fresh order and moves it into
     * AWAITING_PAYMENT — one read-modify-save, so the invoice id set here cannot be
     * lost the way a caller mutating its own (unsaved) Order instance would lose it. */
    public void markAwaitingPayment(String orderId, String btcpayInvoiceId) {
        Order order = findById(orderId);
        order.setBtcpayInvoiceId(btcpayInvoiceId);
        if (order.getStatus() == OrderStatus.NEW) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
        }
        orders.save(order);
    }

    /** Marks an order PAID. Safe to call more than once — setting the same status twice
     * does nothing surprising, and the real idempotency guard lives in PaymentService. */
    public void markPaid(String orderId) {
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orders.save(order);
        }
    }
}
