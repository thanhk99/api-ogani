package com.example.ogani.dtos.request;

import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInOrderRequest {
    
    private long productId;
    private int quantity;
}
