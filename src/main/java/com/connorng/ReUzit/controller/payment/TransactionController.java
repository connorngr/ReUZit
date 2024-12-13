package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.service.OrderService;
import com.connorng.ReUzit.service.TransactionService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(createdTransaction);
    }

    @GetMapping("/seller-orders")
    public ResponseEntity<List<Transaction>> getSellerOrders() {
        String email = userService.getCurrentUserEmail(); // Fetches the current user's email
        List<Transaction> transactions = transactionService.getAllOrdersBySellerEmail(email);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/buyer-orders")
    public ResponseEntity<List<Transaction>> getBuyerOrders() {
        String email = userService.getCurrentUserEmail(); // Assume this fetches the current user's email
        List<Transaction> transactions = transactionService.getAllOrdersByBuyerEmail(email);
        return ResponseEntity.ok(transactions);
    }

    // New API to get all transactions
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        String email = userService.getCurrentUserEmail();
        List<Transaction> transactions = transactionService.getAllTransactions(email);
        return ResponseEntity.ok(transactions);
    }
    @PutMapping("/process-pending-transactions")
    public ResponseEntity<String> processPendingTransactions() {
        String email = userService.getCurrentUserEmail();

        try {
            orderService.processPendingTransactions(email);
            return ResponseEntity.ok("Pending transactions processed successfully!");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to process pending transactions.");
        }
    }

    @PutMapping("/refund-failed")
    public ResponseEntity<String> refundFailedPayments() {
        String email = userService.getCurrentUserEmail();
        try {
            orderService.refundFailedPayments(email);
            return ResponseEntity.ok("Refunds for failed payments processed successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to process refunds.");
        }
    }

    @GetMapping("/deposits")
    public ResponseEntity<List<Transaction>> getDepositTransactions() {
        String email = userService.getCurrentUserEmail();
        List<Transaction> transactions = transactionService.getAllDepositTransactions(email);
        return ResponseEntity.ok(transactions);
    }
}
