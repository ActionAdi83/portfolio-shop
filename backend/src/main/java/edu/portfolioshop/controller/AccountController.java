package edu.portfolioshop.controller;

import edu.portfolioshop.dto.AddressRequest;
import edu.portfolioshop.dto.ProfileRequest;
import edu.portfolioshop.entities.Address;
import edu.portfolioshop.entities.Order;
import edu.portfolioshop.entities.UserProfile;
import edu.portfolioshop.service.AddressService;
import edu.portfolioshop.service.OrderService;
import edu.portfolioshop.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The signed-in customer's own data: profile, address book, order history.
 *
 * Every route here derives "who" from the JWT subject, never from the request —
 * same rule fanvote's controllers follow for private data.
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final ProfileService profiles;
    private final AddressService addresses;
    private final OrderService orders;

    @GetMapping
    public UserProfile getProfile(@AuthenticationPrincipal Jwt jwt) {
        return profiles.find(jwt.getSubject());
    }

    @PutMapping
    public UserProfile updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody ProfileRequest request) {
        return profiles.update(jwt.getSubject(), request);
    }

    @GetMapping("/addresses")
    public List<Address> listAddresses(@AuthenticationPrincipal Jwt jwt) {
        return addresses.findByUser(jwt.getSubject());
    }

    @PostMapping("/addresses")
    public Address createAddress(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddressRequest request) {
        return addresses.create(jwt.getSubject(), request);
    }

    @PutMapping("/addresses/{id}")
    public Address updateAddress(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable String id,
                                 @Valid @RequestBody AddressRequest request) {
        return addresses.update(id, jwt.getSubject(), request);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        addresses.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<Order> myOrders(@AuthenticationPrincipal Jwt jwt) {
        return orders.findByUser(jwt.getSubject());
    }
}
