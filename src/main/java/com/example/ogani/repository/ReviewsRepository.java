package com.example.ogani.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Reviews;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
    @Query(value = "SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = :productId" , nativeQuery = true)
    Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);

    Reviews findByProductIdAndOrderId(Long productId, Long orderId);

    boolean existsByProductIdAndReviewerNameAndOrderId(Long productId, String reviewerName, Long orderId);
}
