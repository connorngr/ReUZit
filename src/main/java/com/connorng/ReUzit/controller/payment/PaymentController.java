package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.common.vnpay.Config;
import com.connorng.ReUzit.model.*;
import com.connorng.ReUzit.service.*;
import com.connorng.ReUzit.service.WishListService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private WishListService selectedListingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<Payment> addPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.addPayment(payment);
        return ResponseEntity.ok(createdPayment);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id, @RequestParam Payment.PaymentStatus status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updatedPayment);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/createCodOrder")
    public ResponseEntity<Order> createCodOrder(
            @RequestBody PaymentRequest order) {
        System.out.println(order);
        String authenticatedEmail = userService.getCurrentUserEmail();
        // Lấy thông tin người dùng từ email đã xác thực
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Người dùng không tồn tại
        }
        User user = userOptional.get();

        // Lấy thông tin danh sách sản phẩm từ idListing
        Listing listing = listingService.findById(order.getListing().getId());
        if (listing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Sản phẩm không tồn tại
        }

        // Lấy địa chỉ giao hàng từ idAddress
        Address address = addressService.getAddressById(order.getShippingAddress().getId());
        if (address == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Địa chỉ không tồn tại
        }

        // Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setShippingAddress(address);
        newOrder.setUser(user);
        newOrder.setListing(listing);
        newOrder.setOrderDate(new Date());
        newOrder.getListing().setPrice(order.getListing().getPrice());  // Sử dụng giá đã gửi từ yêu cầu
        Order savedOrder = orderService.createOrder(newOrder);

        // Tạo đối tượng Payment với trạng thái PENDING cho COD
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setStatus(Payment.PaymentStatus.PENDING);  // Thanh toán PENDING
        payment.setMethod(Payment.PaymentMethod.COD);  // Phương thức thanh toán là COD
        payment.setTransactionId("COD-" + savedOrder.getId());  // Tạo ID giao dịch giả cho COD
        payment.setPaymentDate(new Date());
        Payment savedPayment = paymentService.addPayment(payment);

        // Tạo và lưu Transaction cho COD
        Transaction transaction = new Transaction();
        transaction.setPayment(savedPayment);
        transaction.setSender(user);  // Người mua
        transaction.setReceiver(listing.getUser());  // Người bán (admin)
        transaction.setTransactionDate(new Date());
        transactionService.addTransaction(transaction);

        // Cập nhật trạng thái sản phẩm thành ACTIVE
        listing.setStatus(Status.ACTIVE);  // Trạng thái sản phẩm là ACTIVE
        listingService.saveListing(listing);

        // Trả về kết quả thành công
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping("/paymentCallback")
    public void paymentCallback(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String idListing = queryParams.get("idListing");
        String idUser = queryParams.get("idUser"); // delete
        String price = queryParams.get("vnp_Amount");
        String transactionId = queryParams.get("vnp_TransactionNo");
        String idAddress = queryParams.get("idAddress");

        if (idListing == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }
        Address address = addressService.getAddressById(Long.parseLong(idAddress));

        if(idListing != null && !idListing.equals("")){
            if("00".equals(vnp_ResponseCode)) {
                //transaction finish
                //update database
                try {
                    // Get object User and Listing from database
                    Optional<User> userOptional = userService.findById(Long.parseLong(idUser));
                    Listing listing = listingService.findById(Long.parseLong(idListing));

                    if (!userOptional.isPresent()|| listing == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "User or Listing not found");
                        return;
                    }
                    User user = userOptional.get();

                    // Create object order
                    Order order = new Order();
                    order.setShippingAddress(address);
                    order.setUser(user);
                    order.setListing(listing);
                    order.getListing().setPrice(Long.parseLong(price) / 100); // Value api usually return * 100
                    Order savedOrder = orderService.createOrder(order);
                    // Create and save a new Payment record
                    Payment payment = new Payment();
                    payment.setOrder(savedOrder);
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setMethod(Payment.PaymentMethod.BANK_TRANSFER);
                    payment.setTransactionId(transactionId); // Generate transaction ID
                    payment.setPaymentDate(new Date());
                    Payment savedPayment = paymentService.addPayment(payment);

                    // add money for admin
                    User userAdmin = userService.updateMoney("arty16@gmail.com", listing.getPrice());

                    // **Create and save Transaction**
                    Transaction transaction = new Transaction();
                    transaction.setPayment(savedPayment); // Link with payment
                    transaction.setSender(user); // seller (user)
                    transaction.setReceiver(listing.getUser()); // Admin get money
                    transaction.setTransactionDate(new Date());
                    transactionService.addTransaction(transaction);

                    listing.setStatus(Status.SOLD);
                    listingService.saveListing(listing);
                response.sendRedirect("http://localhost:5173/order");
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process the transaction");
                }
            }  else {
                // transaction fail
                // solve database. ex: update transaction fail
                response.sendRedirect("http://localhost:5173/transaction-failed");
            }
        }
    }

    @GetMapping("/pay")
        public String getPay(@PathParam("price") long price, @PathParam("idListing") Integer idListing, @PathParam("idAddress") Integer idAddress, @PathParam("idUser") Integer idUser) throws UnsupportedEncodingException{
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = price*100;
        String bankCode = "NCB";

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl+"?idListing="+idListing+"&idUser="+idUser+ "&idAddress=" + idAddress); // ? add id listing
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        return paymentUrl;
    }

    @GetMapping("/depositCallback")
    public void depositCallback(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String idUser = queryParams.get("idUser");
        String price = queryParams.get("vnp_Amount");
        String transactionId = queryParams.get("vnp_TransactionNo");
        String email = userService.getCurrentUserEmail();

            if("00".equals(vnp_ResponseCode)) {
                //transaction finish
                //update database
                try {
                    // Get object User and Listing from database
                    Optional<User> userOptional = userService.findByEmail(email);

                    if (!userOptional.isPresent()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                        return;
                    }
                    User user = userOptional.get();

                    response.sendRedirect("http://localhost:5173/congratulation");
                } catch (Exception e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process the transaction");
                }
            }  else {
                // transaction fail
                // solve database. ex: update transaction fail
                response.sendRedirect("http://localhost:5173/transaction-failed");
            }
        }


    @GetMapping("/deposit")
    public String getDeposit(@PathParam("price") long price, @PathParam("idUser") Integer idUser) throws UnsupportedEncodingException{
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = price*100;
        String bankCode = "NCB";

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrlDeposit+"?idUser="+idUser); // ? add id listing
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        return paymentUrl;
    }
}
