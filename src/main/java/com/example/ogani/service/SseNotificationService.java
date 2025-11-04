package com.example.ogani.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.ogani.dtos.response.Notification;
import com.example.ogani.models.Order;
import com.example.ogani.repository.OrderRepository;

@Service
public class SseNotificationService {
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; 

    @Autowired
    private OrderRepository orderRepository;
    public SseEmitter subscribe(Long userId) {
        completeEmitter(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> completeEmitter(userId));
        emitter.onTimeout(() -> completeEmitter(userId));

        sendWelcomeEvent(emitter, userId);

        return emitter;
    }

    public void sendNotification(Long userId, Notification message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            sendEvent(emitter, "notification", message);
        }
    }

    public void broadcast(Notification message) {
        emitters.forEach((userId, emitter) -> {
            sendEvent(emitter, "notification", message);
        });
    }

    public void completeEmitter(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    private void sendWelcomeEvent(SseEmitter emitter, Long userId) {
        executor.execute(() -> {
            try {
                String welcomeMessage = "SSE connection established for user: " + userId;
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data(welcomeMessage));
            } catch (Exception e) {
                completeEmitter(userId);
            }
        });
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        executor.execute(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception e) {
                completeEmitter(findUserIdByEmitter(emitter));
            }
        });
    }

    private Long findUserIdByEmitter(SseEmitter targetEmitter) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getValue().equals(targetEmitter))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(null);
    }

    public List<Long> getConnectedUsers() {
        return new ArrayList<>(emitters.keySet());
    }

    public boolean isUserConnected(Long userId) {
        return emitters.containsKey(userId);
    }

    public Order createOrder(Order order) {
        // Lưu order vào database
        Order savedOrder = orderRepository.save(order);
        
        // Gửi thông báo real-time
        Notification notification = new Notification();
        notification.setType("NEW_ORDER");
        notification.setMessage("Có đơn hàng mới #" + savedOrder.getId() + " từ " + savedOrder.getFirstname()+ " "+ savedOrder.getLastname());
        notification.setTimestamp(LocalDateTime.now());
        notification.setOrderId(savedOrder.getId());
        
        sendNotification(2L, notification);
        
        return savedOrder;
    }
}
