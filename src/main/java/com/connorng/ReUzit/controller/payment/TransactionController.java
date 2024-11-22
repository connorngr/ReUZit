package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(createdTransaction);
    }
}
