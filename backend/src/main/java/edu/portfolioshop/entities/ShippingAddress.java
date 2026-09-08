package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;

/** A copy of an {@link Address}, embedded in an order so it survives the address
 * book entry being edited or deleted later. */
@Getter
@Setter
public class ShippingAddress {

    private String label;
    private String line1;
    private String line2;
    private String city;
    private String postalCode;
    private String country;
}
