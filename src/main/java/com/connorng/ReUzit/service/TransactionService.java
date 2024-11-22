package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service

public class TransactionService{
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllOrdersBySellerEmail(String email) {
        return transactionRepository.findAllByReceiver_Email(email);
    }

    // Add the new findById method
    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }
}
