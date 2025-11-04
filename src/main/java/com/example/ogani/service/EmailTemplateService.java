package com.example.ogani.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {
    
    public String createForgotPasswordTemplate(String shareholderName, String resetToken, String resetLink) {
        String template = loadTemplate("templates/forgot-password-template.html");
        return template
            .replace("{{SHAREHOLDER_NAME}}", shareholderName)
            .replace("{{RESET_LINK}}", resetLink)
            .replace("{{RESET_TOKEN}}", resetToken);
    }

    public String createPasswordResetSuccessTemplate(String shareholderName, LocalDateTime resetTime) {
        String template = loadTemplate("templates/password-reset-success-template.html");
        return template
            .replace("{{SHAREHOLDER_NAME}}", shareholderName)
            .replace("{{RESET_TIME}}", resetTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    private String loadTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println( e);
            return "";
        }
    }
}