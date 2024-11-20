package com.connorng.ReUzit.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;  // Có thể là buyer hoặc admin

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // Có thể là admin hoặc seller

    private Double amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
}
