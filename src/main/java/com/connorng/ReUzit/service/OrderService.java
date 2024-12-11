package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.*;
import com.connorng.ReUzit.model.Order;
import com.connorng.ReUzit.model.Status;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.model.Transaction;
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

    @Autowired
    private EmailSenderService emailSenderService;

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

            // Fetch the transaction by transactionId
            Optional<Transaction> transactionOptional = transactionService.findById(transactionId);
            if (transactionOptional.isEmpty()) {
                throw new RuntimeException("Transaction not found with ID: " + transactionId);
            }

            Transaction transaction = transactionOptional.get();
            User seller = transaction.getSender(); // Assuming receiver is the seller
            User buyer = transaction.getReceiver();   // Assuming sender is the buyer
            Long amount = order.getListing().getPrice();

            User admin = userService.findFirstByRole(Roles.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            String emailSubject;
            String emailBody;

            if (transaction.getPayment().getMethod().name().equals("BANK_TRANSFER")) {
                // Update based on status
                if (status == Status.SOLD) {
                    order.setConfirmationDate(new Date());
                    Long adminFee = (long) (amount * 0.01); // Admin takes a 10% fee
                    seller.setMoney(seller.getMoney() + (amount - adminFee));
                    userService.save(seller);

                    admin.setMoney(admin.getMoney() - (amount - adminFee));
                    userService.save(admin);
                    // Send email to seller
                    emailSubject = "Xác nhận bán hàng thành công!";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Sản phẩm \"%s\" đã được bán thành công với giá %d VND.\n" +
                                    "Số tiền sau phí đã được cộng vào tài khoản của bạn: %d VND.\n\n" +
                                    "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle(),
                            amount,
                            amount - adminFee
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                if (status == Status.INACTIVE) {
                    // Refund amount from admin to buyer
                    buyer.setMoney(buyer.getMoney() + amount);
                    userService.save(buyer);

                    // Deduct from admin account
                    admin.setMoney(admin.getMoney() - amount);
                    userService.save(admin);
                    // Send email to seller about cancellation
                    emailSubject = "Đơn hàng đã bị hủy";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đã bị hủy.\n\n" +
                                    "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ đội ngũ hỗ trợ của chúng tôi.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
            }
            if (transaction.getPayment().getMethod().name().equals("COIN")) {
                // Update based on status
                if (status == Status.SOLD) {
                    order.setConfirmationDate(new Date());
                    Long adminFee = (long) (amount * 0.01); // Admin takes a 10% fee
                    seller.setMoney(seller.getMoney() + (amount - adminFee));
                    userService.save(seller);

                    admin.setMoney(admin.getMoney() - (amount - adminFee));
                    userService.save(admin);
                    // Send email to seller
                    emailSubject = "Xác nhận bán hàng thành công!";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Sản phẩm \"%s\" đã được bán thành công với giá %d VND.\n" +
                                    "Số tiền sau phí đã được cộng vào tài khoản của bạn: %d VND.\n\n" +
                                    "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle(),
                            amount,
                            amount - adminFee
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                if (status == Status.INACTIVE) {
                    // Refund amount from admin to buyer
                    buyer.setMoney(buyer.getMoney() + amount);
                    userService.save(buyer);

                    // Deduct from admin account
                    admin.setMoney(admin.getMoney() - amount);
                    userService.save(admin);
                    // Send email to seller about cancellation
                    emailSubject = "Đơn hàng đã bị hủy";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đã bị hủy.\n\n" +
                                    "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ đội ngũ hỗ trợ của chúng tôi.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
            }
            if (transaction.getPayment().getMethod().name().equals("COD")) {
                if (status == Status.SOLD) {
                    transaction.getPayment().setStatus(Payment.PaymentStatus.SUCCESS);
                    emailSubject = "Xác nhận giao hàng COD thành công!";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Sản phẩm \"%s\" đã được giao thành công.\n\n" +
                                    "Hãy kiểm tra thông tin giao dịch trong hệ thống.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                if (status == Status.INACTIVE) {
                    emailSubject = "Giao hàng COD thất bại";
                    emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" không thành công.\n\n" +
                                    "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ đội ngũ hỗ trợ của chúng tôi.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                transactionService.addTransaction(transaction);
            }
            order.getListing().setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with ID: " + orderId);
    }

    public List<Order> getOrdersByUserEmail(String email) {
        return orderRepository.findByUser_Email(email);
    }

    private void sendStatusChangeEmail(User seller, Order order, Status status) {
        String sellerEmail = seller.getEmail();
        String subject = "Cập nhật trạng thái đơn hàng trên ReUzit";

        String body = String.format(
                "Xin chào %s,\n\n" +
                        "Trạng thái của đơn hàng cho sản phẩm \"%s\" đã được cập nhật.\n" +
                        "Trạng thái mới: %s\n\n" +
                        "Số tiền bạn nhận được (nếu có): %d VND\n\n" +
                        "Cảm ơn bạn đã sử dụng ReUzit!",
                seller.getLastName(),
                order.getListing().getTitle(),
                status.name(),
                (status == Status.SOLD ? (long) (order.getListing().getPrice() * 0.9) : 0) // Deduct admin fee for SOLD status
        );

        emailSenderService.sendEmail(sellerEmail, subject, body);
    }
}
