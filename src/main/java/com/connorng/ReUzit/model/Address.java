package com.connorng.ReUzit.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // Phía ngược lại của quan hệ ManyToOne
    private User user;

    @NotNull
    @Size(min = 5, max = 255)
    private String fullName; // Tên người nhận hàng

    @NotNull
    private String phoneNumber; // Số điện thoại liên hệ

    @NotNull
    @Size(min = 5, max = 255)
    private String street; // Địa chỉ chi tiết

    @Size(min = 2, max = 100)
    private String city; // Thành phố

    @NotNull
    @Size(min = 2, max = 100)
    private String province; // Tỉnh/Thành phố trực thuộc trung ương

    @NotNull
    @Size(min = 2, max = 100)
    private String district; // Quận/Huyện

    @Size(max = 255)
    private String ward; // Xã/Phường (có thể tùy chọn)

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false; // Đánh dấu địa chỉ mặc định

    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;
}
