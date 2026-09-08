package edu.portfolioshop.service;

import edu.portfolioshop.dto.ProductRequest;
import edu.portfolioshop.entities.Product;
import edu.portfolioshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;

    public List<Product> findAll(String categorySlug) {
        return (categorySlug == null || categorySlug.isBlank())
                ? products.findAll()
                : products.findByCategorySlug(categorySlug);
    }

    public Product findById(String id) {
        return products.findById(id).orElseThrow(() -> new NoSuchElementException("No product " + id));
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return products.save(product);
    }

    public Product update(String id, ProductRequest request) {
        Product product = findById(id);
        apply(product, request);
        return products.save(product);
    }

    public void delete(String id) {
        products.deleteById(id);
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategorySlug(request.categorySlug());
        product.setImageUrls(request.imageUrls() == null ? List.of() : request.imageUrls());
        product.setStock(request.stock());
    }
}
