package edu.portfolioshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty List<@Valid Item> items,

        /** Reuse a saved address from the account's address book. Mutually
         * exclusive with {@link #shippingAddress}; one of the two is required. */
        String addressId,

        /** An address supplied inline instead of one already saved. */
        @Valid AddressRequest shippingAddress) {

    public record Item(String productId, int quantity) {
    }
}
