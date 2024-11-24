package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.service.TransactionService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(createdTransaction);
    }

    @GetMapping("/seller-orders")
    public ResponseEntity<List<Transaction>> getSellerOrders() {
        String email = userService.getCurrentUserEmail(); // Assume this fetches the current user's email
        List<Transaction> transactions = transactionService.getAllOrdersBySellerEmail(email);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/buyer-orders")
    public ResponseEntity<List<Transaction>> getBuyerOrders() {
        String email = userService.getCurrentUserEmail(); // Assume this fetches the current user's email
        List<Transaction> transactions = transactionService.getAllOrdersByBuyerEmail(email);
        return ResponseEntity.ok(transactions);
    }
}
