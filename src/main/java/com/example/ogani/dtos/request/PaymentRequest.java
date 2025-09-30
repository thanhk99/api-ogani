package com.example.ogani.dtos.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private long amount;
    private String orderInfo;
    private String orderType;
    
}
