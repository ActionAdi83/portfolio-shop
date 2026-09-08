package edu.portfolioshop.service;

import edu.portfolioshop.dto.CategoryRequest;
import edu.portfolioshop.entities.Category;
import edu.portfolioshop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categories;

    public List<Category> findAll() {
        return categories.findAll();
    }

    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        return categories.save(category);
    }

    public Category update(String id, CategoryRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No category " + id));
        category.setName(request.name());
        category.setSlug(request.slug());
        return categories.save(category);
    }

    // TODO (follow-up): products reference a category by slug, and deleting the
    // category here does not touch them — they simply stop appearing in that
    // category's filter while still existing and still buyable. Fine for a first
    // iteration; a production version would either refuse the delete while
    // products reference the slug, or reassign them to an "uncategorised" bucket.
    public void delete(String id) {
        categories.deleteById(id);
    }
}
