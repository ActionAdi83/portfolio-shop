package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;

    /** URL-safe, unique. Products reference a category by this rather than its id. */
    @Indexed(unique = true)
    private String slug;
}
