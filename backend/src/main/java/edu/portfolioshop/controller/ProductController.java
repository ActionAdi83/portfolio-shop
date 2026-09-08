package edu.portfolioshop.controller;

import edu.portfolioshop.entities.Product;
import edu.portfolioshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public catalogue browsing — no authentication required. */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService products;

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String categorySlug) {
        return products.findAll(categorySlug);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable String id) {
        return products.findById(id);
    }
}
