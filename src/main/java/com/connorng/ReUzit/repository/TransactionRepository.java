package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
