package com.example.ogani.dtos.response;

import com.example.ogani.models.User.Role;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private long id;

    private String username;

    private String email;

    private Role roles;
}
