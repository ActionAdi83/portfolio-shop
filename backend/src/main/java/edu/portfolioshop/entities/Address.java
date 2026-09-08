package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** One entry in a customer's address book — the "cont client" address book. */
@Getter
@Setter
@Document(collection = "addresses")
public class Address {

    @Id
    private String id;

    /** Keycloak `sub` of the owning account. Every query filters on this. */
    private String userId;

    /** Free-text label, e.g. "Home", "Office". */
    private String label;

    private String line1;
    private String line2;
    private String city;
    private String postalCode;
    private String country;
}
