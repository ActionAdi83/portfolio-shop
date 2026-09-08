package edu.portfolioshop.repository;

import edu.portfolioshop.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategorySlug(String categorySlug);
}
