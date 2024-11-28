package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.*;
import com.connorng.ReUzit.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    public Order createOrder(Order order) {
        order.setOrderDate(new Date());
        // setConfirmationDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Lấy ngày hiện tại
        calendar.add(Calendar.DATE, 3); // Cộng thêm 3 ngày
        order.setConfirmationDate(calendar.getTime());

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, Status status, Long transactionId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            System.out.println(transactionId);

            // Fetch the transaction by transactionId
            Optional<Transaction> transactionOptional = transactionService.findById(transactionId);
            if (transactionOptional.isEmpty()) {
                throw new RuntimeException("Transaction not found with ID: " + transactionId);
            }

            Transaction transaction = transactionOptional.get();
            User seller = transaction.getReceiver(); // Assuming receiver is the seller
            User buyer = transaction.getSender();   // Assuming sender is the buyer
            Long amount = order.getListing().getPrice();

            if (transaction.getPayment().getMethod().name().equals("BANK_TRANSFER")) {
                // Update based on status
                if (status == Status.SOLD) {
                    order.setConfirmationDate(new Date());
                    Long adminFee = (long) (amount * 0.1); // Admin takes a 10% fee
                    seller.setMoney(seller.getMoney() + (amount - adminFee));
                    userService.createUser(seller);

                    User admin = userService.findByEmail("arty16@gmail.com")
                            .orElseThrow(() -> new IllegalArgumentException("Admin not found with email: arty16@gmail.com"));
                    admin.setMoney(admin.getMoney() - (amount - adminFee));
                    userService.createUser(admin);
                }
                if (status == Status.INACTIVE) {
                    // Refund amount from admin to buyer
                    buyer.setMoney(buyer.getMoney() + amount);
                    userService.createUser(buyer);

                    // Deduct from admin account
                    User admin = userService.findByEmail("arty16@gmail.com")
                            .orElseThrow(() -> new IllegalArgumentException("Admin not found with email: arty16@gmail.com"));
                    admin.setMoney(admin.getMoney() - amount);
                    userService.createUser(admin);
                }
            }
            if (transaction.getPayment().getMethod().name().equals("COD")) {
                if (status == Status.SOLD) {
                    transaction.getPayment().setStatus(Payment.PaymentStatus.SUCCESS);
                }
                if (status == Status.INACTIVE) {
                    transaction.getPayment().setStatus(Payment.PaymentStatus.FAILED);
                }
            }
            order.getListing().setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with ID: " + orderId);
    }

    public List<Order> getOrdersByUserEmail(String email) {
        return orderRepository.findByUser_Email(email);
    }
}
