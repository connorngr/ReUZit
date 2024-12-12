package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.model.TransactionType;
import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByReceiver_Email(String email);
    List<Transaction> findAllBySender_Email(String email);
    List<Transaction> findAllByTransactionType(TransactionType transactionType);
    List<Transaction> findAllBySenderAndTransactionType(User sender, TransactionType transactionType);
    List<Transaction> findAllByReceiverAndTransactionType(User receiver, TransactionType transactionType);
}
