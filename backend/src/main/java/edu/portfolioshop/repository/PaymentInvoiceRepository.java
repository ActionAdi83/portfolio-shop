package edu.portfolioshop.repository;

import edu.portfolioshop.entities.PaymentInvoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentInvoiceRepository extends MongoRepository<PaymentInvoice, String> {
    Optional<PaymentInvoice> findByBtcpayInvoiceId(String btcpayInvoiceId);
    Optional<PaymentInvoice> findByOrderIdAndStatusNot(String orderId, String status);
}
