package com.example.ogani.dtos.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Notification  {
    private String type;
    private String message;
    private LocalDateTime timestamp;
    private Long orderId;
}
