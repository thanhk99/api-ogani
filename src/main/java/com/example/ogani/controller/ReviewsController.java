package com.example.ogani.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.request.ReviewRequest;
import com.example.ogani.models.Reviews;
import com.example.ogani.service.ReviewsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/reviews")
public class ReviewsController {
    
    @Autowired
    private ReviewsService reviewsService;
    
    @PostMapping("/create")
    public ResponseEntity<?> CreateReview(@RequestBody ReviewRequest entity) {
        return reviewsService.createReview(entity);
    }
    
    @GetMapping("/")
    public ResponseEntity<?> getAllReview() {
        return reviewsService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewsByProductId(@PathVariable  Long id) {
        return reviewsService.getReviewsByProductId(id);
    }

    @GetMapping("/average/{productId}")
    public ResponseEntity<?> getAverageRatingByProductId(@PathVariable Long productId) {
        return reviewsService.getAverageRatingByProductId(productId);
    }
    @GetMapping("/product/{productId}/order/{orderId}")
    public ResponseEntity<?> getProductOrderId(@PathVariable Long productId, @PathVariable Long orderId) {
        return reviewsService.getProductOrderId(productId, orderId);
    }
    @GetMapping("/count/{productId}")
    public ResponseEntity<?> getReviewCountByProductId(@PathVariable Long productId) {
        return reviewsService.getReviewCountByProductId(productId);
    }
    
    
}