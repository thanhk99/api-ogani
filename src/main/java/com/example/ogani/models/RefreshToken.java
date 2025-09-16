package com.example.ogani.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens", uniqueConstraints = @UniqueConstraint(name = "UK_user_refresh_token", columnNames = {
        "uid" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
