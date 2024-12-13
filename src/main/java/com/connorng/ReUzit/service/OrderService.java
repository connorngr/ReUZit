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
    private PaymentService paymentService;

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

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, Status status, Long transactionId, String email) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                throw new RuntimeException("Order not found with ID: " + orderId);
            }

            Order order = orderOptional.get();

            // Fetch the transaction by transactionId
            Optional<Transaction> transactionOptional = transactionService.findById(transactionId);
            if (transactionOptional.isEmpty()) {
                throw new RuntimeException("Transaction not found with ID: " + transactionId);
            }

            Transaction transaction = transactionOptional.get();
            User seller = transaction.getSender(); // Assuming receiver is the seller
            User buyer = transaction.getReceiver(); // Assuming sender is the buyer

            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Check if the user has the admin role
            boolean isAdmin = currentUser.getRole().equals(Roles.ROLE_ADMIN);

            if (!isAdmin && !buyer.getEmail().equals(email)) {
                throw new SecurityException("You are not authorized to update this transaction.");
            }

            Long amount = order.getListing().getPrice();
            User admin = userService.findFirstByRole(Roles.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

            String emailSubject;
            String emailBody;

            switch (transaction.getPayment().getMethod()) {
                case BANK_TRANSFER -> {
                    handleBankTransferStatus(status, order, transaction, seller, buyer, admin, amount, isAdmin);
                }
                case COIN -> {
                    handleCoinStatus(status, order, transaction, seller, buyer, admin, amount, isAdmin);
                }
                case COD -> {
                    handleCODStatus(status, order, transaction, seller);
                }
                default -> throw new IllegalArgumentException("Unsupported payment method");
            }

            order.getListing().setStatus(status);
            return orderRepository.save(order);

        } catch (SecurityException e) {
            throw new SecurityException("Unauthorized action: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("An error occurred: " + e.getMessage(), e);
        }
    }

    private void handleBankTransferStatus(Status status, Order order, Transaction transaction, User seller, User buyer, User admin, Long amount, boolean isAdmin) {
        try {
            switch (status) {
                case SOLD -> {
                    order.setConfirmationDate(new Date());
                    Long adminFee = (long) (amount * 0.01); // Admin takes a 1% fee
                    if (admin.getMoney() < amount - adminFee) {
                        throw new IllegalArgumentException("Admin does not have sufficient funds to process the payment.");
                    }
                    seller.setMoney(seller.getMoney() + (amount - adminFee));
                    userService.save(seller);

                    admin.setMoney(admin.getMoney() - (amount - adminFee));
                    userService.save(admin);

                    transaction.getPayment().setStatus(Payment.PaymentStatus.SUCCESS);
                    transactionService.addTransaction(transaction);
                    // Send email to seller
                    String emailSubject = "Xác nhận bán hàng thành công!";
                    String emailBody = String.format(
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
                case INACTIVE -> {
                    if (admin.getMoney() < amount) {
                        throw new IllegalArgumentException("Admin does not have sufficient funds to refund the buyer.");
                    }
                    //Add 3 day
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date()); // Lấy ngày hiện tại
                    calendar.add(Calendar.DATE, 3); // Cộng thêm 3 ngày
                    transaction.getPayment().getOrder().setConfirmationDate(calendar.getTime());
                    // Send email to seller about cancellation
                    String emailSubject = "Đơn hàng đã bị hủy";
                    String emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đã bị hủy.\n\n" +
                                    "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ đội ngũ hỗ trợ của chúng tôi.\n" +
                                    "Vui lòng trả hàng trong 3 ngày cho người bán nếu không có thắc mắc nào.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                case PENDING -> {
                    if (isAdmin) {
                        if (order.getListing().getStatus() == Status.SOLD) {
                            if (seller.getMoney() < amount) {
                                throw new IllegalArgumentException("Seller does not have sufficient funds to revert the sale.");
                            }
                            if(order.getConfirmationDate().before(new Date())) {
                                throw new IllegalArgumentException("It's past time to change status");
                            }
                            seller.setMoney(seller.getMoney() - amount);
                            userService.save(seller);

                            admin.setMoney(admin.getMoney() + amount);
                            userService.save(admin);
                            transaction.getPayment().setStatus(Payment.PaymentStatus.PENDING);
                            transactionService.addTransaction(transaction);
                        } else if (order.getListing().getStatus() == Status.INACTIVE) {
                            if (buyer.getMoney() < amount) {
                                throw new IllegalArgumentException("Buyer does not have sufficient funds to proceed.");
                            }
                            if(order.getConfirmationDate().before(new Date())) {
                                throw new IllegalArgumentException("It's past time to change status");
                            }

                            transaction.getPayment().setStatus(Payment.PaymentStatus.PENDING);
                            transactionService.addTransaction(transaction);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to handle bank transfer status: " + e.getMessage(), e);
        }
    }

    private void handleCoinStatus(Status status, Order order, Transaction transaction, User seller, User buyer, User admin, Long amount, boolean isAdmin) {
        try {
            switch (status) {
                case SOLD -> {
                    order.setConfirmationDate(new Date());
                    Long adminFee = (long) (amount * 0.01); // Admin takes a 1% fee
                    if (admin.getMoney() < amount - adminFee) {
                        throw new IllegalArgumentException("Admin does not have sufficient funds to process the payment.");
                    }
                    seller.setMoney(seller.getMoney() + (amount - adminFee));
                    userService.save(seller);

                    admin.setMoney(admin.getMoney() - (amount - adminFee));
                    userService.save(admin);
                    transaction.getPayment().setStatus(Payment.PaymentStatus.SUCCESS);
                    transactionService.addTransaction(transaction);

                    // Send email to seller
                    String emailSubject = "Xác nhận bán hàng thành công!";
                    String emailBody = String.format(
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
                case INACTIVE -> {
                    if (admin.getMoney() < amount) {
                        throw new IllegalArgumentException("Admin does not have sufficient funds to refund the buyer.");
                    }
                    //Add 3 day
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date()); // Lấy ngày hiện tại
                    calendar.add(Calendar.DATE, 3); // Cộng thêm 3 ngày
                    transaction.getPayment().getOrder().setConfirmationDate(calendar.getTime());
                    // Send email to seller about cancellation
                    String emailSubject = "Đơn hàng đã bị hủy";
                    String emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đã bị hủy.\n\n" +
                                    "Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ đội ngũ hỗ trợ của chúng tôi.\n" +
                                    "Vui lòng trả hàng trong 3 ngày cho người bán nếu không có thắc mắc nào.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                case PENDING -> {
                    if (isAdmin) {
                        if (order.getListing().getStatus() == Status.SOLD) {
                            if (seller.getMoney() < amount) {
                                throw new IllegalArgumentException("Seller does not have sufficient funds to revert the sale.");
                            }
                            if(order.getConfirmationDate().before(new Date())) {
                                throw new IllegalArgumentException("It's past time to change status");
                            }
                            seller.setMoney(seller.getMoney() - amount);
                            userService.save(seller);

                            admin.setMoney(admin.getMoney() + amount);
                            userService.save(admin);
                            transaction.getPayment().setStatus(Payment.PaymentStatus.PENDING);
                            transactionService.addTransaction(transaction);
                        } else if (order.getListing().getStatus() == Status.INACTIVE) {
                            if (buyer.getMoney() < amount) {
                                throw new IllegalArgumentException("Buyer does not have sufficient funds to proceed.");
                            }
                            if(order.getConfirmationDate().before(new Date())) {
                                throw new IllegalArgumentException("It's past time to change status");
                            }

                            transaction.getPayment().setStatus(Payment.PaymentStatus.PENDING);
                            transactionService.addTransaction(transaction);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to handle coin status: " + e.getMessage(), e);
        }
    }
    private void handleCODStatus(Status status, Order order, Transaction transaction, User seller) {
        try {
            switch (status) {
                case SOLD -> {

                    String emailSubject = "Xác nhận giao hàng COD thành công!";
                    String emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Sản phẩm \"%s\" đã được giao thành công.\n\n" +
                                    "Hãy kiểm tra thông tin giao dịch trong hệ thống.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    transaction.getPayment().setStatus(Payment.PaymentStatus.SUCCESS);
                    transactionService.addTransaction(transaction);
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                case INACTIVE -> {
                    String emailSubject = "Đơn hàng COD đã bị hủy";
                    String emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đã bị hủy.\n\n" +
                                    "Vui lòng kiểm tra lại thông tin trong hệ thống hoặc liên hệ đội ngũ hỗ trợ nếu cần.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    transaction.getPayment().setStatus(Payment.PaymentStatus.FAILED);
                    transactionService.addTransaction(transaction);
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
                case PENDING -> {
                    String emailSubject = "Trạng thái đơn hàng COD đang chờ xử lý";
                    String emailBody = String.format(
                            "Xin chào %s,\n\n" +
                                    "Đơn hàng của sản phẩm \"%s\" đang trong trạng thái chờ xử lý.\n\n" +
                                    "Vui lòng kiểm tra lại thông tin và xác nhận.\n\n" +
                                    "Trân trọng,\nĐội ngũ ReUzit",
                            seller.getLastName(),
                            order.getListing().getTitle()
                    );
                    transaction.getPayment().setStatus(Payment.PaymentStatus.PENDING);
                    transactionService.addTransaction(transaction);
                    emailSenderService.sendEmail(seller.getEmail(), emailSubject, emailBody);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to handle COD status: " + e.getMessage(), e);
        }
    }

    public void processPendingTransactions(String email) {
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if the user has the admin role
        boolean isAdmin = currentUser.getRole().equals(Roles.ROLE_ADMIN);

        if (!isAdmin) {
            throw new SecurityException("You are not authorized to update this transaction.");
        }
        // Fetch all transactions with PENDING payments
        List<Transaction> pendingTransactions = transactionService.findByPaymentStatus(Payment.PaymentStatus.PENDING);

        Date currentDate = new Date();

        for (Transaction transaction : pendingTransactions) {
            Payment payment = transaction.getPayment();
            Order order = payment.getOrder();

            // Check if order confirmation date is older than today
            if (order != null && order.getConfirmationDate().before(currentDate) && order.getListing().getStatus().equals(Status.PENDING)) {
                updateOrderStatus(order.getId(), Status.SOLD, transaction.getId(), email);
            }
        }
    }

    public void refundFailedPayments(String email) {
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check if the user has the admin role
        boolean isAdmin = currentUser.getRole().equals(Roles.ROLE_ADMIN);

        if (!isAdmin) {
            throw new SecurityException("You are not authorized to update this transaction.");
        }
        // Fetch all transactions with FAILED status
        List<Transaction> failedTransactions = transactionService.findByListingStatus(Status.INACTIVE);

        Date currentDate = new Date();

        for (Transaction transaction : failedTransactions) {
            Payment payment = transaction.getPayment();
            Order order = payment.getOrder();

            if (order != null && order.getConfirmationDate().before(currentDate) && order.getListing().getStatus().equals(Status.INACTIVE) &&
            payment.getStatus().equals(Payment.PaymentStatus.PENDING)) {

                User buyer = transaction.getReceiver();
                User admin = userService.findFirstByRole(Roles.ROLE_ADMIN)
                        .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

                Long amount = order.getListing().getPrice();

                // Deduct the money from admin
                if (admin.getMoney() < amount) {
                    throw new IllegalArgumentException("Admin does not have sufficient funds to process the refund.");
                }
                admin.setMoney(admin.getMoney() - amount);
                userService.save(admin);

                // Refund the money to the buyer
                buyer.setMoney(buyer.getMoney() + amount);
                userService.save(buyer);

                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentService.addPayment(payment);
                order.setConfirmationDate(new Date());
                save(order);
                // Log the refund process
                System.out.println("Refunded " + amount + " to buyer: " + buyer.getEmail());
            }
        }
    }

    public List<Order> getOrdersByUserEmail(String email) {
        return orderRepository.findByUser_Email(email);
    }
}
