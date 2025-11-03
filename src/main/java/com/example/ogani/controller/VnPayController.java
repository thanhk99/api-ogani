package com.example.ogani.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.request.PaymentRequest;
import com.example.ogani.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class VnPayController {

    @Autowired
    private OrderService orderService;
    
    // Thông tin cấu hình VnPay
    private final String vnp_TmnCode = "SB1YAE0Q"; // Terminal ID của bạn
    private final String vnp_HashSecret = "LHGM8QJ30I04W1IWX3V226XHJB73RE0C"; // Khóa bí mật
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";// dẫn đến sandbox
    private final String vnp_Version = "2.1.0";
    private final String vnp_Command = "pay";
    private final String vnp_ReturnUrl = "http://localhost:4200/payment-result";// url mà fe nhận kết quả sau thanh toán

    // tạo api thanh toán
    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, String>> createPayment(
            @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest request) {
        
        // Tạo các tham số cơ bản
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(paymentRequest.getAmount() * 100)); // theo yc của vnpay đồng*100
        
        // sinh thời gian theo vnpay yêu cầu
        Calendar cld = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        vnp_Params.put("vnp_CurrCode", "VND");// loại tiền tệ
        vnp_Params.put("vnp_IpAddr", getClientIP(request));//ip người dùng
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_OrderInfo", paymentRequest.getOrderInfo());// mô tả order
        vnp_Params.put("vnp_OrderType", paymentRequest.getOrderType() != null ? paymentRequest.getOrderType() : "other"); // mặc định là other
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);// trang fe nhận kq
        
        // Tạo mã tham chiếu giao dịch ngẫu nhiên
        String vnp_TxnRef = generateTransactionRef();
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        // Sắp xếp tham số và tạo hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);// theo alphabet
        StringBuilder hashData = new StringBuilder();// chuỗi tạo chữ ký bí mật
        StringBuilder query = new StringBuilder();//chuỗi để ghep vào url cuối
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Tạo chuỗi hash
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                // Tạo chuỗi query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }// key = value nối bằng &

        // Tạo chuỗi hash
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());//dùng khóa bí mật vnp_HashSecret để tạo mã hash xác thực dữ liệu
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        String paymentUrl = vnp_Url + "?" + query;// tạo url hoàn chỉnh để ng dùng chuyểnt tới
        
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        
        return new ResponseEntity<>(response, HttpStatus.OK);// trả về cho fe 
    }
    
    
    // thanh toán xong VnPay gưi tham số về cho BE
    @GetMapping("/payment-callback")
    public ResponseEntity<Map<String, String>> paymentCallback(
            @RequestParam Map<String, String> queryParams) {
        
        Map<String, String> response = new HashMap<>();
        
        log.info("Received payment callback with params: {}", queryParams);// kiểm tra chữ ký có đúng K?
        
        boolean isValidSignature = validatePaymentReturn(queryParams);
        
        if (isValidSignature) {
            String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
            log.info("Payment response code: {}", vnp_ResponseCode);
            // đúng '00' thành công, khác thì lỗi
            if ("00".equals(vnp_ResponseCode)) {
                String orderId = queryParams.get("vnp_OrderInfo");
                
                try {
                    // Xác nhận thanh toán đơn hàng
                    orderService.confirmOrderPayment(orderId);
                    
                    // Thanh toán thành công
                    response.put("status", "SUCCESS");
                    response.put("message", "Thanh toán thành công");
                    response.put("transactionId", queryParams.get("vnp_TransactionNo"));
                    response.put("orderInfo", orderId);
                    
                    // Xử lý amount an toàn
                    String amountStr = queryParams.get("vnp_Amount");
                    try {
                        long amount = Long.parseLong(amountStr) / 100;
                        response.put("amount", String.valueOf(amount));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid amount format: {}", amountStr);
                        response.put("amount", "0");
                    }
                    
                } catch (Exception e) {
                    log.error("Error confirming order payment for order: {}", orderId, e);
                    response.put("status", "ERROR");
                    response.put("message", "Lỗi xác nhận đơn hàng");
                }
                
            } else {
                // Thanh toán thất bại
                response.put("status", "FAILED");
                response.put("message", "Thanh toán thất bại");
                response.put("responseCode", vnp_ResponseCode);
                
                // Lấy thông tin đơn hàng để ghi log
                String orderId = queryParams.get("vnp_OrderInfo");
                log.warn("Payment failed for order: {}, response code: {}", orderId, vnp_ResponseCode);
            }
        } else {
            // Chữ ký không hợp lệ
            response.put("status", "INVALID_SIGNATURE");
            response.put("message", "Chữ ký không hợp lệ");
            log.warn("Invalid signature detected in payment callback");
        }
        
        log.info("Payment callback response: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // hàm kiểm tra chữ ký
    private boolean validatePaymentReturn(Map<String, String> vnp_Params) {
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        Map<String, String> validParams = new HashMap<>();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                validParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Sắp xếp tham số
        List<String> fieldNames = new ArrayList<>(validParams.keySet());
        Collections.sort(fieldNames);
        
        // Tạo chuỗi hash
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = validParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return calculatedHash.equals(vnp_SecureHash);
    }
    
    /**
     * Tạo mã tham chiếu giao dịch
     */
    private String generateTransactionRef() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + new Random().nextInt(1000);
    }

    /**
     * Hàm tạo hash HMAC-SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] rawHmac = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo hash", e);
        }
    }

    /**
     * Chuyển đổi byte sang chuỗi hex
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Lấy IP client
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    
}