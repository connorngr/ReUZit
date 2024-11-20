package com.connorng.ReUzit.controller.payment;

import com.connorng.ReUzit.Common.vnpay.Config;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.Order;
import com.connorng.ReUzit.model.Payment;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.ListingService;
import com.connorng.ReUzit.service.OrderService;
import com.connorng.ReUzit.service.PaymentService;
import com.connorng.ReUzit.service.UserService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

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

    @GetMapping("/paymentCallback")
    public void paymentCallback(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String idListing = queryParams.get("idListing");
        String idUser = queryParams.get("idUser");
        String price = queryParams.get("vnp_Amount");
        System.out.println("Listing:" + idListing + " and User:"+idUser + " and price:"+price);

        if (idListing == null || idUser == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }
        System.out.println("Listing: " + idListing + " and User: " + idUser);

        if(idListing != null && !idListing.equals("")){
            if("00".equals(vnp_ResponseCode)) {
                //transaction finish
                //update database
                try {
                    // Lấy các đối tượng User và Listing từ database
                    User user = userService.findById(Long.parseLong(idUser));
                    Listing listing = listingService.findById(Long.parseLong(idListing));

                    if (user == null || listing == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "User or Listing not found");
                        return;
                    }

                    // Tạo đối tượng Order
                    Order order = new Order();
                    order.setUser(user);
                    order.setListing(listing);
                    order.setAmount(Double.parseDouble(price) / 100); // Giá trị từ API thường được trả về nhân 100
                    Order savedOrder = orderService.createOrder(order);

                    System.out.println("Order created: " + savedOrder.getId());

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
    }

    @GetMapping("/pay")
    public String getPay(@PathParam("price") long price, @PathParam("idListing") Integer idListing, @PathParam("idUser") Integer idUser) throws UnsupportedEncodingException{
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
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl+"?idListing="+idListing+"&idUser="+idUser); // ? add id listing
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
