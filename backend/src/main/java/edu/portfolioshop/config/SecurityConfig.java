package edu.portfolioshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring Security OAuth2 resource-server config validating Keycloak JWTs.
 *
 * Adapted from fanvote-communication's edu.fanvote.config.SecurityConfig — same
 * custom {@link #jwtAuthenticationConverter()}, same shape, different route table.
 */
@Configuration
// Turns on @PreAuthorize. Without it those annotations are silently inert — they
// compile, they read like protection, and they enforce nothing.
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()

                        // Browsing the catalogue is public — it is the shop window.
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // BTCPay cannot present a token, so its webhook has to be open.
                        // The HMAC signature on the body is what authenticates it —
                        // verified before anything in it is trusted.
                        .requestMatchers(HttpMethod.POST, "/api/payments/btcpay/webhook").permitAll()

                        // The admin screens are part of the portfolio demo, so any signed-in
                        // visitor can look around read-only — only the mutating verbs (create,
                        // edit, delete, change an order's status) require the shop-admin role.
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("shop-admin")

                        // Own account, own addresses, own orders, opening a checkout — all
                        // derive "who" from the token, never from the request.
                        .requestMatchers("/api/account/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Realm roles out of a Keycloak token.
     *
     * A plain {@code JwtGrantedAuthoritiesConverter} with
     * {@code setAuthoritiesClaimName("realm_access.roles")} does not read a nested
     * claim — that setter names a claim, and the lookup behind it is a flat map
     * get, so it resolves to null and produces an empty authority list for every
     * token. Copied verbatim from fanvote-communication, where this was a fixed
     * production bug: every hasRole() check failed silently until this custom
     * converter replaced it.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            if (!(jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess)) {
                return List.of();
            }
            if (!(realmAccess.get("roles") instanceof Collection<?> roles)) {
                return List.of();
            }
            return roles.stream()
                    .map(String::valueOf)
                    // ROLE_ because hasRole("x") checks for authority "ROLE_x".
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return jwtAuthenticationConverter;
    }

    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("https://shop.adriandragota.com", "http://localhost:4300")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
}
