package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service

public class TransactionService{
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
