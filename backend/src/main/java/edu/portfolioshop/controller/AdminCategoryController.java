package edu.portfolioshop.controller;

import edu.portfolioshop.dto.CategoryRequest;
import edu.portfolioshop.entities.Category;
import edu.portfolioshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categories;

    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categories.create(request));
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        return categories.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categories.delete(id);
        return ResponseEntity.noContent().build();
    }
}
