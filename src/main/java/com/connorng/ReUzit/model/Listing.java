package com.connorng.ReUzit.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    @Column(columnDefinition = "TEXT") // Sử dụng kiểu TEXT trong cơ sở dữ liệu
    private String description;

    @Column(nullable = false)
    private Long price;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
