package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.payment.status = :status")
    List<Transaction> findByPaymentStatus(@Param("status") Payment.PaymentStatus status);
    List<Transaction> findAllByTransactionType(TransactionType transactionType);
    List<Transaction> findAllBySenderAndTransactionType(User sender, TransactionType transactionType);
    List<Transaction> findAllByReceiverAndTransactionType(User receiver, TransactionType transactionType);
    @Query("SELECT t FROM Transaction t WHERE t.payment.order.listing.status = :status")
    List<Transaction> findByOrder_Listing_Status(@Param("status") Status status);

}
