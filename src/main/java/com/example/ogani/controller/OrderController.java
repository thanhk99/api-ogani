package com.example.ogani.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.models.Order;
import com.example.ogani.dtos.request.CreateOrderRequest;
import com.example.ogani.dtos.request.ProductInOrderRequest;
import com.example.ogani.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*",maxAge = 3600)
public class OrderController {
    @Autowired
    private OrderService orderService;


    @GetMapping("/")
    @Operation(summary="Lấy ra danh sách đặt hàng")
    public ResponseEntity<List<Order>> getList(){
        List<Order> list = orderService.getList();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/user")
    @Operation(summary="Lấy ra danh sách đặt hàng của người dùng bằng username")
    public ResponseEntity<List<Order>> getListByUser(@RequestParam("username") String username){
        List<Order> list = orderService.getOrderByUser(username);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    @Operation(summary="Đặt hàng sản phẩm")
    public ResponseEntity<?> placeOrder(@RequestBody CreateOrderRequest request){

        Order order = orderService.placeOrder(request);

        return ResponseEntity.ok(Map.of("message", "Đặt hàng thành công" , "OrderId" ,order.getId() ));
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkOrder(@RequestBody List<ProductInOrderRequest> productIds) {
        return orderService.processCheckOrder(productIds);
    }
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.confirmOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bắt đầu giao hàng (PAID -> SHIPPING)
    @PutMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.shipOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hoàn thành đơn hàng (SHIPPING -> COMPLETED)
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.completeOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Huỷ đơn hàng
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật ngày thanh toán (khi thanh toán VNPay thành công)
    @PutMapping("/{id}/payment-date")
    public ResponseEntity<?> updatePaymentDate(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.updatePaymentDate(id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetaiOrder(@PathVariable long id) {
        return orderService.getOrderDetail(id);
    }
    
    
}
