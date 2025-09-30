package com.example.ogani.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Order;
import com.example.ogani.models.OrderDetail;
import com.example.ogani.models.User;
import com.example.ogani.models.Order.OrderStatus;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateOrderDetailRequest;
import com.example.ogani.dtos.request.CreateOrderRequest;
import com.example.ogani.dtos.request.ProductInOrderRequest;
import com.example.ogani.repository.OrderDetailRepository;
import com.example.ogani.repository.OrderRepository;
import com.example.ogani.repository.ProductRepository;
import com.example.ogani.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService{
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Order order = new Order();
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new NotFoundException("Not Found User With Username:" + request.getUsername()));
        order.setFirstname(request.getFirstname());
        order.setLastname(request.getLastname());
        order.setCountry(request.getCountry());
        order.setAddress(request.getAddress());
        order.setTown(request.getTown());
        order.setState(request.getState());
        order.setPostCode(request.getPostCode());
        order.setEmail(request.getEmail());
        order.setPhone(request.getPhone());
        order.setNote(request.getNote());   
        order.setOrderStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        long totalPrice = 0;
        //kiểm tra số lượng tồn của sản phẩm

        for(CreateOrderDetailRequest rq: request.getOrderDetails()){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(rq.getName());
            orderDetail.setPrice(rq.getPrice());
            orderDetail.setQuantity(rq.getQuantity());
            orderDetail.setSubTotal(rq.getPrice()* rq.getQuantity());
            
            orderDetail.setOrder(order);
            totalPrice += orderDetail.getSubTotal();
            orderDetailRepository.save(orderDetail);
            
        }
        subTotalProduct(request.getOrderDetails());
        order.setTotalPrice(totalPrice);
        order.setUser(user);
        Order saveOrder =  orderRepository.save(order);
        return saveOrder;
    }

    public List<Order> getList() {
        return orderRepository.findAll(Sort.by("id").descending());
    }

    public List<Order> getOrderByUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Not Found User With Username:" + username));

        List<Order> orders = orderRepository.getOrderByUser(user.getUid());
        return orders;  
    }

    public ResponseEntity<?> processCheckOrder(List<ProductInOrderRequest> productIds) {
        for (ProductInOrderRequest productInOrder : productIds) {
            long productId = productInOrder.getProductId();
            int quantity = productInOrder.getQuantity();
            // // Kiểm tra số lượng tồn của sản phẩm với productId và quantity
            long availableQuantity = productRepository.findQuantityById(productId).getQuantity();
            if (availableQuantity == 0 || availableQuantity < quantity) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Sản phẩm với ID " + productId + " không đủ số lượng tồn.");
            }
            
        }
        return ResponseEntity.ok(Map.of( "message", "Sản phẩm đủ số lượng tồn"));
    }

    @Transactional
    public void subTotalProduct(List<CreateOrderDetailRequest> orderDetails) {
        for (CreateOrderDetailRequest rq : orderDetails) {
            String productName = rq.getName();
            int quantity = rq.getQuantity();
            //findByName trả về List<Product> vì có thể có nhiều sản phẩm trùng tên
            productRepository.findByName(productName).stream().findFirst().map(product -> {
                int newQuantity = product.getQuantity() - quantity;
                product.setQuantity(newQuantity);
                return productRepository.save(product);
            }).orElseThrow(() -> new NotFoundException("Not Found Product With Name: " + productName));
        }
    }

    public boolean existsByOrderCode(String orderId) {
        return orderRepository.existsById(Long.parseLong(orderId));
    }

    public boolean existsByOrderId(String orderId) {
        return orderRepository.existsById(Long.parseLong(orderId));
    }

    @Transactional
    public boolean confirmOrderPayment(String orderCode) {
        Order order = orderRepository.findById(Long.parseLong(orderCode))
                .orElseThrow(() -> new NotFoundException("Not Found Order With Id: " + orderCode));
        if (order != null) {
            order.setOrderStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
            return true;
        }

        return false;
    }

}
