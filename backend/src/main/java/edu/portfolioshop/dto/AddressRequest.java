package edu.portfolioshop.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String label,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country) {
}
