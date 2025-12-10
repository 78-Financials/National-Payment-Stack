package com.payaza.nps.service;

import com.payaza.nps.dto.ChangePasswordRequestDto;
import com.payaza.nps.dto.UserProfileDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Service for profile management
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private InternalClientRegistry clientRegistry;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Get current user profile
     */
    public UserProfileDto getCurrentUserProfile() {
        InternalClient currentClient = ClientContext.getCurrentClient();
        if (currentClient == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        return convertToUserProfileDto(currentClient);
    }
    
    /**
     * Update user profile
     */
    public UserProfileDto updateUserProfile(UserProfileDto profileDto) {
        InternalClient currentClient = ClientContext.getCurrentClient();
        if (currentClient == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        // Update client information
        currentClient.setClientName(profileDto.getClientName());
        currentClient.setContactEmail(profileDto.getEmail());
        currentClient.setContactPhone(profileDto.getPhone());
        currentClient.setUpdatedAt(LocalDateTime.now());
        
        // Save updated client
        clientRegistry.updateClient(currentClient);
        
        logger.info("User profile updated for client: {}", currentClient.getClientId());
        
        return convertToUserProfileDto(currentClient);
    }
    
    /**
     * Change user password
     */
    public void changePassword(ChangePasswordRequestDto request) {
        InternalClient currentClient = ClientContext.getCurrentClient();
        if (currentClient == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        // Validate current password (if stored)
        // For now, we'll skip this validation since we're using API keys
        
        // Generate new API key (in a real implementation, you might want to hash passwords)
        String newApiKey = generateApiKey();
        currentClient.setApiKey(newApiKey);
        currentClient.setUpdatedAt(LocalDateTime.now());
        
        clientRegistry.updateClient(currentClient);
        
        // Send new API key via email
        emailService.sendApiKeyUpdate(currentClient.getContactEmail(), newApiKey);
        
        logger.info("Password/API key changed for client: {}", currentClient.getClientId());
    }
    
    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        InternalClient client = clientRegistry.getClientByEmail(email);
        if (client == null) {
            // Don't reveal if email exists or not for security
            logger.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        // Generate reset token
        String resetToken = generateResetToken();
        client.setPasswordResetToken(resetToken);
        client.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        client.setUpdatedAt(LocalDateTime.now());
        
        clientRegistry.updateClient(client);
        
        // Send reset email
        emailService.sendPasswordResetEmail(email, resetToken);
        
        logger.info("Password reset requested for client: {}", client.getClientId());
    }
    
    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        InternalClient client = clientRegistry.getClientByResetToken(token);
        if (client == null) {
            throw new RuntimeException("Invalid or expired reset token");
        }
        
        if (client.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        // Generate new API key
        String newApiKey = generateApiKey();
        client.setApiKey(newApiKey);
        client.setPasswordResetToken(null);
        client.setPasswordResetExpires(null);
        client.setUpdatedAt(LocalDateTime.now());
        
        clientRegistry.updateClient(client);
        
        // Send new API key via email
        emailService.sendApiKeyUpdate(client.getContactEmail(), newApiKey);
        
        logger.info("Password reset completed for client: {}", client.getClientId());
    }
    
    /**
     * Convert InternalClient to UserProfileDto
     */
    private UserProfileDto convertToUserProfileDto(InternalClient client) {
        UserProfileDto profile = new UserProfileDto();
        profile.setId(client.getClientId());
        profile.setClientName(client.getClientName());
        profile.setEmail(client.getContactEmail());
        profile.setPhone(client.getContactPhone());
        profile.setClientType(client.getClientType());
        profile.setActive(client.isActive());
        profile.setLastLogin(client.getLastActivity());
        profile.setCreatedAt(client.getCreatedAt());
        profile.setUpdatedAt(client.getUpdatedAt());
        
        return profile;
    }
    
    /**
     * Generate new API key
     */
    private String generateApiKey() {
        return "ak_live_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Generate password reset token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
