package com.example.ogani.service;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.ogani.dtos.request.LoginRequest;
import com.example.ogani.dtos.request.SignupRequest;
import com.example.ogani.dtos.response.AuthResponse;
import com.example.ogani.dtos.request.RefreshTokenRequest;
import com.example.ogani.models.RefreshToken;
import com.example.ogani.models.User;
import com.example.ogani.models.User.Role;
import com.example.ogani.repository.RefreshTokenRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.security.jwt.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> signup(SignupRequest signupRequest) {
        try {
            // Validate dữ liệu đầu vào
            if (signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error", "VALIDATION_ERROR",
                                "message", "Email không được để trống",
                                "field", "email"));
            }

            if (signupRequest.getPassword() == null || signupRequest.getPassword().length() < 6) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error", "VALIDATION_ERROR",
                                "message", "Mật khẩu phải có ít nhất 6 ký tự",
                                "field", "password"));
            }

            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT) // 409 Conflict
                        .body(Map.of(
                                "error", "EMAIL_EXISTS",
                                "message", "Email đã được sử dụng",
                                "field", "email"));
            }

            User user = new User();
            user.setUsername(signupRequest.getUsername());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole(signupRequest.getRole() != null ? signupRequest.getRole() : Role.ROLE_USER);
            user.setEnabled(true);

            User savedUser = userRepository.save(user);

            return ResponseEntity
                    .status(HttpStatus.CREATED) // 201 Created
                    .body(Map.of(
                            "success", true,
                            "message", "Đăng ký thành công",
                            "data", Map.of(
                                    "id", savedUser.getUid(),
                                    "username", savedUser.getUsername(),
                                    "email", savedUser.getEmail())));

        } catch (Exception e) {
            // Xử lý lỗi hệ thống
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error", "SERVER_ERROR",
                            "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> loginService(LoginRequest loginRequest) {
        try {
            // Xác thực thông tin đăng nhập
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Lấy thông tin user
            User user = userRepository.findByUsername(loginRequest.getUsername()).get();
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Account already in use"));
            }

            String accessToken = jwtUtil.generateAccessToken(user);
            RefreshToken refreshToken = jwtUtil.generateRefreshToken(user);

            return ResponseEntity.ok(
                    new AuthResponse(
                            accessToken,
                            refreshToken.getToken(),
                            user.getUid(),
                            user.getUsername(),
                            user.getRole()));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "LOGIN_ERROR",
                            "message", "Tài khoản mật khẩu không đúng"));
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    // @Transactional
    // public ResponseEntity<?> RefreshTokenService(RefreshTokenRequest refreshTokenRequest) {
    //     try {
    //         String requestRefreshToken = refreshTokenRequest.getRefreshToken();
    //         if (requestRefreshToken == null) {
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                     .body(Map.of("error", "Invalid refresh token"));
    //         }

    //         RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken);
    //         if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
    //             refreshTokenRepository.delete(refreshToken);
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "refresh token expried"));
    //         }
    //         refreshTokenRepository.delete(refreshToken);

    //         // Tạo token mới
    //         User user = refreshToken.getUser();
    //         String newAccessToken = jwtUtil.generateAccessToken(user);

    //         // Trả về response
    //         return ResponseEntity.ok(
    //                 new AuthResponse(
    //                         newAccessToken,
    //                         refreshToken.getToken(),
    //                         user.getUid(),
    //                         user.getUsername()));

    //     } catch (ResponseStatusException e) {
    //         throw e; // Re-throw các lỗi đã được định nghĩa
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "refresh token expried"));
    //     }
    // }

    public ResponseEntity<?> isExistEmail(SignupRequest signupRequest) {
        try {
            User isExisit = userRepository.findByEmail(signupRequest.getEmail());
            if (isExisit != null) {
                return ResponseEntity.ok(Map.of("message", "email đã tồn tại"));
            } else {
                return ResponseEntity.ok(Map.of("message", "success"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "SERVER_ERROR"));
        }
    }
}