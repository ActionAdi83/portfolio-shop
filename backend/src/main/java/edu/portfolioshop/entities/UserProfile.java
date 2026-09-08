package edu.portfolioshop.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The shop-side profile fields Keycloak has no place for.
 *
 * Identity (email, name, login) stays in Keycloak — this is only the extra bits an
 * order needs, such as a phone number, and only ever addressed by the JWT `sub`.
 */
@Getter
@Setter
@Document(collection = "user_profiles")
public class UserProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String fullName;
    private String phone;
}
