package com.example.ogani.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column (nullable =  false)
    private Long orderId;
    
    @Column(nullable = false)
    private String reviewerName;
    
    @Column(nullable = false)
    private Integer reviewRating;
    
    @Column(columnDefinition = "TEXT")
    private String reviewComment;
    
    @Column(nullable = false)
    private LocalDateTime reviewDate;

    private boolean hasReview = true;

    @PrePersist
    protected void onCreate() {
        reviewDate = LocalDateTime.now();
    }
}