package com.payaza.nps.service;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for password reset operations
 */
@Service
@Transactional
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private InternalClientRepository clientRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private NotificationService notificationService;
    
    private static final int TOKEN_EXPIRY_HOURS = 24;
    
    /**
     * Initiate password reset process
     */
    public boolean initiatePasswordReset(String email) {
        logger.info("Initiating password reset for email: {}", email);
        
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            // Return true to prevent email enumeration attacks
            return true;
        }
        
        InternalClient client = clientOpt.get();
        
        // Generate reset token
        String resetToken = passwordService.generateResetToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
        
        // Update client with reset token
        client.setPasswordResetToken(resetToken);
        client.setPasswordResetExpires(expiryTime);
        clientRepository.save(client);
        
        // Send notification (email)
        try {
            notificationService.sendPasswordResetEmail(client.getEmail(), resetToken, client.getClientName());
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            // Don't fail the operation if email sending fails
        }
        
        return true;
    }
    
    /**
     * Confirm password reset with token
     */
    public boolean confirmPasswordReset(String token, String newPassword) {
        logger.info("Confirming password reset with token");
        
        Optional<InternalClient> clientOpt = clientRepository.findByPasswordResetToken(token);
        if (clientOpt.isEmpty()) {
            logger.warn("Invalid password reset token provided");
            return false;
        }
        
        InternalClient client = clientOpt.get();
        
        // Check if token is expired
        if (client.getPasswordResetExpires() == null || 
            client.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset token expired for client: {}", client.getEmail());
            return false;
        }
        
        // Validate new password strength
        if (!passwordService.isPasswordStrong(newPassword)) {
            logger.warn("Weak password provided for reset");
            return false;
        }
        
        // Update password
        String encodedPassword = passwordService.encodePassword(newPassword);
        client.setPassword(encodedPassword);
        client.setPasswordResetToken(null);
        client.setPasswordResetExpires(null);
        client.resetLoginAttempts(); // Reset login attempts on successful password reset
        clientRepository.save(client);
        
        logger.info("Password reset completed successfully for client: {}", client.getEmail());
        return true;
    }
    
    /**
     * Validate reset token
     */
    public boolean isValidResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        Optional<InternalClient> clientOpt = clientRepository.findByPasswordResetToken(token);
        if (clientOpt.isEmpty()) {
            return false;
        }
        
        InternalClient client = clientOpt.get();
        return client.getPasswordResetExpires() != null && 
               client.getPasswordResetExpires().isAfter(LocalDateTime.now());
    }
    
    /**
     * Clean up expired reset tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Cleaning up expired password reset tokens");
        
        // This would typically be done with a scheduled job
        // For now, we'll implement a simple cleanup
        LocalDateTime now = LocalDateTime.now();
        
        // Note: This is a simplified approach. In production, you might want to use
        // a batch update query for better performance
        clientRepository.findAll().forEach(client -> {
            if (client.getPasswordResetExpires() != null && 
                client.getPasswordResetExpires().isBefore(now)) {
                client.setPasswordResetToken(null);
                client.setPasswordResetExpires(null);
                clientRepository.save(client);
            }
        });
        
        logger.info("Expired password reset tokens cleaned up");
    }
}
