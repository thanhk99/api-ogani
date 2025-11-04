package com.example.ogani.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.ogani.dtos.request.LoginRequest;
import com.example.ogani.dtos.request.RessetPasswordRequest;
import com.example.ogani.dtos.request.SignupRequest;
import com.example.ogani.dtos.response.AuthResponse;
import com.example.ogani.models.PasswordResetToken;
import com.example.ogani.models.RefreshToken;
import com.example.ogani.models.User;
import com.example.ogani.models.User.Role;
import com.example.ogani.repository.PasswordResetTokenRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private PasswordResetTokenRepository passwordResetTokenRepository ;

    @Autowired 
    private EmailService emailService;

    @Autowired 
    private EmailTemplateService emailTemplateService;

    private static final int EXPIRATION_MINUTES = 10; // Token hết hạn sau 1 giờ

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

    @Async("taskExecutor")
    public void handleForgotPasswordAsync(User user) {
        try {
            System.out.println("🔄 [ASYNC] Starting email sending process for: " + user.getEmail());
            
            PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUser(user);
            String resetToken = "";
            
            if (passwordResetToken != null) {
                if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                    resetToken = generateResetToken(user);
                } else {
                    resetToken = passwordResetToken.getToken();
                }

            }      
            else{
                resetToken=generateResetToken(user);
            }
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
            String htmlContent = emailTemplateService.createForgotPasswordTemplate(
                user.getFirstname() + user.getLastname(),
                resetToken,
                resetLink
            );
            
            emailService.sendHtmlMessage(
                user.getEmail(),
                " Đặt Lại Mật Khẩu",
                htmlContent
            );
            System.out.println(resetToken);
            System.out.println(" [ASYNC] Email sent successfully to: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println(" [ASYNC] Failed to send email to: " + user.getEmail());
            e.printStackTrace();
        }
    }

    public String generateResetToken(User user) {
        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteByUser(user);

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        
        passwordResetTokenRepository.save(resetToken);
        
        return token;
    }

    public ResponseEntity<?> validateAndResetPassword(RessetPasswordRequest entity) {
        Optional<PasswordResetToken> exitstToken = passwordResetTokenRepository.findByToken(entity.getToken());
        if(!exitstToken.isPresent()){
            return ResponseEntity.badRequest().body(Map.of("status","bad_request","message","Token không hợp lệ"));
        }
        PasswordResetToken resetToken =exitstToken.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
                return ResponseEntity.badRequest().body(Map.of("status","bad_request","message","Token hết hạn"));
        }

        User user = resetToken.getUser();
        if(!entity.getConfirmPassword().equals(entity.getPassword())){
            return ResponseEntity.badRequest().body(Map.of("status","bad_request","message","Mật khẩu xác nhận không trùng khớp"));          
        }
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(entity.getPassword()));
        userRepository.save(user);

        // Xóa token đã sử dụng
        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("status","success","message","Cập nhật mật khẩu thành công "));
    }
}