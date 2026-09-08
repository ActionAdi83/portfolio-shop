package edu.portfolioshop.config;

import edu.portfolioshop.entities.Category;
import edu.portfolioshop.entities.PaymentInvoice;
import edu.portfolioshop.entities.UserProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Creates the uniqueness constraints the shop relies on.
 *
 * Spring Boot leaves {@code spring.data.mongodb.auto-index-creation} off, so an
 * {@code @Indexed(unique = true)} annotation on its own creates nothing — declared
 * explicitly here instead, same pattern as fanvote's PaymentIndexInitializer.
 */
@Configuration
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexInitializer.class);

    @Bean
    public ApplicationRunner shopIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                mongoTemplate.indexOps(PaymentInvoice.class)
                        .ensureIndex(new Index().on("btcpayInvoiceId", Sort.Direction.ASC).unique());
                mongoTemplate.indexOps(Category.class)
                        .ensureIndex(new Index().on("slug", Sort.Direction.ASC).unique());
                mongoTemplate.indexOps(UserProfile.class)
                        .ensureIndex(new Index().on("userId", Sort.Direction.ASC).unique());
            } catch (RuntimeException e) {
                // Worth shouting about: without the invoice index, a redelivered
                // webhook could in principle create a second invoice record.
                log.error("Could not create the shop's uniqueness indexes", e);
            }
        };
    }
}
