package com.example.ogani.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Reviews;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {
       @Query(value = "SELECT AVG(r.review_rating) FROM reviews r WHERE r.product_id = :productId" , nativeQuery = true)
       Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);

       @Query(value = "SELECT * FROM reviews WHERE product_id = :productId AND order_id = :orderId", 
              nativeQuery = true)
       Reviews findByProductIdAndOrderId(@Param("productId") Long productId, 
                                          @Param("orderId") Long orderId);

       @Query(value = "SELECT COUNT(*) > 0 FROM reviews WHERE product_id = :productId AND reviewer_name = :reviewerName AND order_id = :orderId", 
           nativeQuery = true)
       Long existsByProductIdAndReviewerNameAndOrderId(@Param("productId") Long productId, 
                                                        @Param("reviewerName") String reviewerName, 
                                                        @Param("orderId") Long orderId);
}
