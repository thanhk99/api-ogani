package com.example.ogani.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ogani.models.RefreshToken;
import com.example.ogani.models.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByUser(User user);

    RefreshToken findByToken(String requestRefreshToken);

}
