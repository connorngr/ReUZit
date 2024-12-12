package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Order;
import com.connorng.ReUzit.model.Status;
import com.connorng.ReUzit.service.OrderService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam Status status, @RequestParam Long transactionId) {
        String email = userService.getCurrentUserEmail();
        Order updatedOrder = orderService.updateOrderStatus(id, status, transactionId, email);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getAllOrdersByUser() {
        String email = userService.getCurrentUserEmail();
        List<Order> orders = orderService.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }
}
