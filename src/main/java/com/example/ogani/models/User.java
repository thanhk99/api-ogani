package com.example.ogani.models;

import lombok.*;
import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    private String firstname;

    private String lastname;

    private String password;

    private String country;

    private String state;

    private String address;

    private String phone;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
    }
}
