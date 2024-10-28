package com.connorng.ReUzit.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Assuming User entity already exists

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Transient // Chỉ dùng cho mục đích truyền thông tin, không lưu vào DB
    private Long categoryId;

    @Column(nullable = false)
    private String condition;

    @Column(nullable = false)
    private String status;  // e.g., active, sold, removed

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

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

    public void addImage(Image image) {
        images.add(image);
        image.setListing(this); // Đảm bảo thiết lập mối quan hệ 2 chiều
    }

    // Phương thức tiện ích để xóa hình ảnh
    public void removeImage(Image image) {
        images.remove(image);
        image.setListing(null);
    }

    @Override
    public String toString() {
        return "Listing{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                // không gọi images ở đây
                '}';
    }
}
