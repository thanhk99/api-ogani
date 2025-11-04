package com.example.ogani.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Order;
import com.example.ogani.models.OrderDetail;
import com.example.ogani.models.User;
import com.example.ogani.models.Order.OrderStatus;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.dtos.request.CreateOrderDetailRequest;
import com.example.ogani.dtos.request.CreateOrderRequest;
import com.example.ogani.dtos.request.ProductInOrderRequest;
import com.example.ogani.dtos.response.Notification;
import com.example.ogani.repository.OrderDetailRepository;
import com.example.ogani.repository.OrderRepository;
import com.example.ogani.repository.ProductRepository;
import com.example.ogani.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SseNotificationService sseNotificationService;

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Order order = new Order();
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Not Found User With Username:" + request.getUsername()));
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
        order.setDateOrder(LocalDateTime.now());
        log.info(order.getPayMethod());
        if (request.getPayMethod().equals("COD")) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else {
            order.setOrderStatus(OrderStatus.PENDING);
        }
        order.setPayMethod(request.getPayMethod());
        orderRepository.save(order);
        long totalPrice = 0;
        // kiểm tra số lượng tồn của sản phẩm

        for (CreateOrderDetailRequest rq : request.getOrderDetails()) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setName(rq.getName());
            orderDetail.setPrice(rq.getPrice());
            orderDetail.setQuantity(rq.getQuantity());
            orderDetail.setSubTotal(rq.getPrice() * rq.getQuantity());
            orderDetail.setProductId(rq.getProductId());
            orderDetail.setOrder(order);
            totalPrice += orderDetail.getSubTotal();
            orderDetailRepository.save(orderDetail);

        }
        subTotalProduct(request.getOrderDetails());
        order.setTotalPrice(totalPrice);
        order.setUser(user);

        Order saveOrder = orderRepository.save(order);
        sendNewOrderNotification(saveOrder);

        return saveOrder;
    }

    public List<Order> getList() {
        return orderRepository.findAll(Sort.by("id").descending());
    }

    public List<Order> getOrderByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Not Found User With Username:" + username));

        List<Order> orders = orderRepository.getOrderByUser(user.getUid());
        return orders;
    }

    public ResponseEntity<?> processCheckOrder(List<ProductInOrderRequest> productIds) {
        List<String> errors = new ArrayList<>();

        for (ProductInOrderRequest productInOrder : productIds) {
            long productId = productInOrder.getProductId();
            String productName = productInOrder.getProductName();
            int quantityRequest = productInOrder.getQuantity();

            // Lấy sản phẩm theo ID
            var productOpt = productRepository.findQuantityById(productId);

            // Kiểm tra sản phẩm tồn tại
            if (productOpt == null) {
                errors.add(
                        String.format("Sản phẩm %s - ID %d không tồn tại trong hệ thống.", productName, productId));
                continue;
            }

            long availableQuantity = productOpt.getQuantity();
            String name = productOpt.getName();

            // Kiểm tra số lượng tồn kho
            if (availableQuantity <= 0) {
                errors.add(String.format("Sản phẩm %s đã hết hàng.", name));
            } else if (availableQuantity < quantityRequest) {
                errors.add(String.format("Sản phẩm %s chỉ còn %d sản phẩm, không đủ để mua %d sản phẩm.",
                        name, availableQuantity, quantityRequest));
            }
        }

        // Nếu có lỗi, trả về danh sách lỗi
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
        }

        return ResponseEntity.ok(Map.of("message", "Tất cả sản phẩm đều đủ số lượng tồn."));
    }

    @Transactional
    public void subTotalProduct(List<CreateOrderDetailRequest> orderDetails) {
        for (CreateOrderDetailRequest rq : orderDetails) {
            String productName = rq.getName();
            int quantity = rq.getQuantity();
            // findByName trả về List<Product> vì có thể có nhiều sản phẩm trùng tên
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
            order.setPayDateTime(LocalDateTime.now());
            orderRepository.save(order);
            return true;
        }

        return false;
    }

    public Order confirmOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Chỉ cho phép xác nhận đơn hàng khi đang ở trạng thái CONFIRMED
            if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
                order.setOrderStatus(OrderStatus.SHIPPING);
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException(
                        "Không thể xác nhận đơn hàng với trạng thái hiện tại: " + order.getOrderStatus());
            }
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    public Order shipOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Chỉ cho phép bắt đầu giao hàng khi đã thanh toán (PAID)
            if (order.getOrderStatus() == OrderStatus.PAID) {
                order.setOrderStatus(OrderStatus.SHIPPING);
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException(
                        "Không thể bắt đầu giao hàng với trạng thái hiện tại: " + order.getOrderStatus());
            }
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    public Order completeOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Chỉ cho phép hoàn thành đơn hàng khi đang giao (SHIPPING)
            if (order.getOrderStatus() == OrderStatus.SHIPPING) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setPayDateTime(LocalDateTime.now());
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException(
                        "Không thể hoàn thành đơn hàng với trạng thái hiện tại: " + order.getOrderStatus());
            }
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    public Order cancelOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Chỉ cho phép huỷ đơn hàng khi chưa hoàn thành
            if (order.getOrderStatus() != OrderStatus.COMPLETED &&
                    order.getOrderStatus() != OrderStatus.CANCELLED) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                return orderRepository.save(order);
            } else {
                throw new IllegalStateException(
                        "Không thể huỷ đơn hàng với trạng thái hiện tại: " + order.getOrderStatus());
            }
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    // API để cập nhật ngày thanh toán (khi thanh toán thành công)
    public Order updatePaymentDate(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setPayDateTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.PAID);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    public ResponseEntity<?> getOrderDetail(Long orderId) {
        List<OrderDetail> orderOptional = orderDetailRepository.findByOrderId(orderId);
        if (orderOptional != null) {
            return ResponseEntity.ok(orderOptional);
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Không có dữ liệu"));

    }

    private void sendNewOrderNotification(Order order) {
        Notification notification = new Notification();
        notification.setType("NEW_ORDER");
        notification.setMessage("🆕 Có đơn hàng mới #" + order.getId() + " từ " +
                order.getFirstname() + " " + order.getLastname());
        notification.setOrderId(order.getId());
        notification.setTimestamp(LocalDateTime.now());

        // Gửi đến admin (user ID = 1)
        sseNotificationService.sendNotification(2L, notification);

        // Hoặc broadcast đến tất cả admin đang kết nối
        // sseNotificationService.broadcast(notification);

        System.out.println("📢 Notification sent for new order: " + order.getId());
    }
}
