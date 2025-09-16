package com.example.ogani.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ogani.models.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

}
