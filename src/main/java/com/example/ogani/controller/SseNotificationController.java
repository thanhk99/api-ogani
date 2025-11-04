package com.example.ogani.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.ogani.dtos.response.Notification;
import com.example.ogani.service.SseNotificationService;


@RestController
@RequestMapping("/api/sse")
public class SseNotificationController {

    @Autowired
    private SseNotificationService sseNotificationService;

    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId) {
        return sseNotificationService.subscribe(userId);
    }

    @PostMapping("/notify/{userId}")
    public void sendNotification(@PathVariable Long userId,
            @RequestBody Notification message) {
        sseNotificationService.sendNotification(userId, message);
    }

    @PostMapping("/broadcast")
    public void broadcast(@RequestBody Notification message) {
        sseNotificationService.broadcast(message);
    }
}
