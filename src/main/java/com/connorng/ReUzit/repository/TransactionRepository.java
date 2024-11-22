package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByReceiver_Email(String email);
}
