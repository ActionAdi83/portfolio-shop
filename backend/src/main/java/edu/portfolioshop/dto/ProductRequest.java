package edu.portfolioshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotBlank String categorySlug,
        List<String> imageUrls,
        @PositiveOrZero int stock) {
}
