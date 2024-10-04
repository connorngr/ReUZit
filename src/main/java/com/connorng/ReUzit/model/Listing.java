package com.connorng.ReUzit.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Assuming User entity already exists

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String condition;

    @Column(nullable = false)
    private String status;  // e.g., active, sold, removed

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Image> images;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;
    //Created in db for the first time
    @PrePersist
    protected void onCreate() {
        this.createdAt = new java.util.Date();
        this.updatedAt = new java.util.Date();
    }
    //When update
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new java.util.Date();
    }
}
