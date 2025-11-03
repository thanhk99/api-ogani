package com.example.ogani.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.PasswordResetToken;
import com.example.ogani.models.User;

import jakarta.transaction.Transactional;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long>{

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    int deleteAllByExpiryDateBefore(@Param("now") LocalDateTime now);

    PasswordResetToken findByUser(User user);
    
}
