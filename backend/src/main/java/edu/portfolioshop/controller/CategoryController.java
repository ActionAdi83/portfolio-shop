package edu.portfolioshop.controller;

import edu.portfolioshop.entities.Category;
import edu.portfolioshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public — the category dropdown/sidebar has to be readable before signing in. */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categories;

    @GetMapping
    public List<Category> list() {
        return categories.findAll();
    }
}
