package edu.portfolioshop.dto;

import jakarta.validation.constraints.NotBlank;

public record InvoiceCreateRequest(@NotBlank String orderId) {
}
