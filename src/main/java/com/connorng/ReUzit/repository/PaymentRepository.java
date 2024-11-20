package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
