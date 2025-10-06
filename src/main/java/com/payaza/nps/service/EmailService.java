package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Email Service for sending emails
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private NotificationConfigurationService configService;
    
    /**
     * Send API key update email
     */
    public void sendApiKeyUpdate(String email, String newApiKey) {
        try {
            String subject = "NPS API Key Updated";
            String message = String.format(
                "Your NPS API key has been updated.\n\n" +
                "New API Key: %s\n\n" +
                "Please update your applications with the new API key.\n\n" +
                "If you did not request this change, please contact support immediately.",
                newApiKey
            );
            
            sendEmail(email, subject, message);
            logger.info("API key update email sent to: {}", email);
            
        } catch (Exception e) {
            logger.error("Failed to send API key update email to {}: {}", email, e.getMessage(), e);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            String subject = "NPS Password Reset Request";
            String message = String.format(
                "You have requested a password reset for your NPS account.\n\n" +
                "Reset Token: %s\n\n" +
                "This token will expire in 1 hour.\n\n" +
                "If you did not request this reset, please ignore this email.",
                resetToken
            );
            
            sendEmail(email, subject, message);
            logger.info("Password reset email sent to: {}", email);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }
    
    /**
     * Send generic email
     */
    private void sendEmail(String to, String subject, String message) {
        Map<String, Object> emailData = Map.of(
            "to", new String[]{to},
            "subject", subject,
            "message", message,
            "text", message,
            "sender", configService.getEmailSender()
        );
        
        webClient.post()
            .uri(configService.getEmailApiUrl())
            .header("x-api-key", configService.getEmailApiKey())
            .bodyValue(emailData)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
