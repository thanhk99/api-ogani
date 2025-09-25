package com.example.ogani.dtos.response;

import jakarta.persistence.Lob;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    String name;
    @Lob
    private byte[] image;
    double price;
    int quantity;
    String category;
    String description;
    String content;
}
