package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.model.Order;
import com.connorng.ReUzit.service.OrderService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getAllOrdersByUser() {
        String email = userService.getCurrentUserEmail();
        List<Order> orders = orderService.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }
}
