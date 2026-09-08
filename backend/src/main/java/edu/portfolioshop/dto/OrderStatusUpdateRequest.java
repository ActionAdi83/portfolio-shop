package edu.portfolioshop.dto;

import edu.portfolioshop.entities.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {
}
