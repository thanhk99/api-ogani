package com.example.ogani.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.models.Reviews;
import com.example.ogani.repository.ReviewsRepository;

@Service
public class ReviewsService {
    
    @Autowired
    private ReviewsRepository reviewsRepository;

    public ResponseEntity<?> createReview(Reviews reviewRequest) {
        if (reviewsRepository.existsByProductIdAndReviewerNameAndOrderId(
            reviewRequest.getProductId(), reviewRequest.getReviewerName(),reviewRequest.getOrderId().getId())) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "You have already submitted a review for this product"
            ));
        }
        
        
        reviewsRepository.save(reviewRequest);
        return ResponseEntity.ok().body(Map.of(
            "message", "Review submitted successfully"
        ));
    }

    public ResponseEntity<?> getReviewsByProductId(Long productId) {
        return ResponseEntity.ok(reviewsRepository.findAll().stream()
            .filter(review -> review.getProductId().equals(productId))
            .toList());
    }

    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewsRepository.findAll());
    }

    public ResponseEntity<?> getAverageRatingByProductId(Long productId) {
        double averageRating = reviewsRepository.findAverageRatingByProductId(productId)
                .orElse(0.0);
        return ResponseEntity.ok(Map.of(
            "averageRating", averageRating
        ));
    }
    public ResponseEntity<?> getProductOrderId(Long productId , Long orderId) {
        Reviews list = reviewsRepository.findByProductIdAndOrderId(productId,orderId);
        // if (list == null) {
        //     return ResponseEntity.badRequest().body(Map.of(
        //         "message", "No review found for this product and order"
        //     ));
        // }
        return ResponseEntity.ok(Map.of(
            "data", list
        ));
    }

    public ResponseEntity<?> getReviewCountByProductId(Long productId) {
        long reviewCount = reviewsRepository.findAll().stream()
            .filter(review -> review.getProductId().equals(productId))
            .count();
        return ResponseEntity.ok(Map.of(
            "reviewCount", reviewCount
        ));
    }

    // public ResponseEntity<?> getOrderToReview(Long orderId){
        
    // }
}
