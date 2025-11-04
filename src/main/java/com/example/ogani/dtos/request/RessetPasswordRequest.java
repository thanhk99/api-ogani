package com.example.ogani.dtos.request;

import lombok.Data;

@Data

public class RessetPasswordRequest {
    private String token ;
    private String password;
    private String confirmPassword;
}
