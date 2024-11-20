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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // e.g., MOMO, BANK_TRANSFER, DIRECT

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // e.g., PENDING, SUCCESS, FAILED

    private String transactionId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    public enum PaymentMethod {
        MOMO, BANK_TRANSFER, DIRECT
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }
}

