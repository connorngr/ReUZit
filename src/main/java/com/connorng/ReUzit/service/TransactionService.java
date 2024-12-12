package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Roles;
import com.connorng.ReUzit.model.Transaction;
import com.connorng.ReUzit.model.TransactionType;
import com.connorng.ReUzit.model.User;
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

    @Autowired
    private UserService userService;

    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllOrdersBySellerEmail(String email) {
        // Get the seller (current user's) information
        Optional<User> seller = userService.findByEmail(email);
        // Fetch all transactions where sender is the seller and the transaction type is PRODUCT_SALE
        return transactionRepository.findAllBySenderAndTransactionType(seller.get(), TransactionType.PRODUCT_SALE);
    }

    public List<Transaction> getAllOrdersByBuyerEmail(String email) {
        // Get the buyer (current user's) information
        Optional<User> buyer = userService.findByEmail(email);
        // Fetch all transactions where receiver is the buyer and the transaction type is PRODUCT_SALE
        return transactionRepository.findAllByReceiverAndTransactionType(buyer.get(), TransactionType.PRODUCT_SALE);
    }

    public List<Transaction> getAllTransactions(String email) {
        // Find the first Admin by role
        User admin = userService.findFirstByRole(Roles.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // If the email is not the admin's email, throw a security exception
        if (!admin.getEmail().equals(email)) {
            throw new SecurityException("You are not authorized to access all transactions.");
        }

        // Return all transactions where the type is PRODUCT_SALE
        return transactionRepository.findAllByTransactionType(TransactionType.PRODUCT_SALE);
    }

    // Add the new findById method
    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }
}
