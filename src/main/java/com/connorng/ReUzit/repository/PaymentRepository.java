package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Return all payment by status
    List<Payment> findByStatus(Payment.PaymentStatus status);
    // Find by idOrder
    List<Payment> findByOrder_Id(Long orderId);
    // Find by transactionId
    Payment findByTransactionId(String transactionId);
}
