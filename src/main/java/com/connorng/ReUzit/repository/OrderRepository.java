package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_Email(String email); // Tìm đơn hàng theo email người dùng
}
