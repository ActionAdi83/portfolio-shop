package edu.portfolioshop.controller;

import edu.portfolioshop.dto.OrderStatusUpdateRequest;
import edu.portfolioshop.entities.Order;
import edu.portfolioshop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orders;

    @GetMapping
    public List<Order> list() {
        return orders.findAll();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable String id) {
        return orders.findById(id);
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable String id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orders.updateStatus(id, request.status());
    }
}
