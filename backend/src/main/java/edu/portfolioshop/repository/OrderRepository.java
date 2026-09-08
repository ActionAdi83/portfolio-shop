package edu.portfolioshop.repository;

import edu.portfolioshop.entities.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Order> findByIdAndUserId(String id, String userId);
    List<Order> findAllByOrderByCreatedAtDesc();
}
