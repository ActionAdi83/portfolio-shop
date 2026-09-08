package edu.portfolioshop.controller;

import edu.portfolioshop.dto.OrderCreateRequest;
import edu.portfolioshop.entities.Order;
import edu.portfolioshop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orders;

    /** Creates an order from cart contents + a shipping address. The checkout
     * invoice is opened separately, via POST /api/payments/invoices. */
    @PostMapping
    public Order create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrderCreateRequest request) {
        return orders.createOrder(jwt.getSubject(), request);
    }

    @GetMapping("/{id}")
    public Order get(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return orders.findOwned(id, jwt.getSubject());
    }
}
