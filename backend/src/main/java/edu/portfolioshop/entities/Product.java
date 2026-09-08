package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;

    /** References Category.slug rather than its id — stable across a category rename. */
    private String categorySlug;

    private List<String> imageUrls = new ArrayList<>();

    private int stock;
}
