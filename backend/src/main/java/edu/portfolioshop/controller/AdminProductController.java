package edu.portfolioshop.controller;

import edu.portfolioshop.dto.ProductRequest;
import edu.portfolioshop.entities.Product;
import edu.portfolioshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Role-gated in SecurityConfig: everything under /api/admin/** needs shop-admin. */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService products;

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(products.create(request));
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        return products.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        products.delete(id);
        return ResponseEntity.noContent().build();
    }
}
