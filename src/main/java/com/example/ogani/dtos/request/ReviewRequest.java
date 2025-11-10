package com.example.ogani.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewRequest {
    private Long orderId;
    private Long productId;
    private Integer reviewRating;
    private String reviewComment;
    private String reviewerName;
}