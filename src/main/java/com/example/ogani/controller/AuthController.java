package com.example.ogani.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.request.LoginRequest;
import com.example.ogani.dtos.request.SignupRequest;
import com.example.ogani.dtos.response.MessageResponse;
import com.example.ogani.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.loginService(loginRequest);
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {

        authService.signup(request);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // @PostMapping("/logout")
    // @Operation(summary = "Đăng xuất")
    // public ResponseEntity<?> logoutUser() {
    // return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
    // .body(new MessageResponse("You've been logout!"));
    // }
}
