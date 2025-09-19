package com.example.ogani.dtos.request;

import com.example.ogani.models.User.Role;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SignupRequest {
    private String username;

    private String email;

    private String password;

    private Role role ;

}