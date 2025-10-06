package com.payaza.nps.controller;

import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.dto.UserProfileDto;
import com.payaza.nps.dto.ChangePasswordRequestDto;
import com.payaza.nps.service.AuthService;
import com.payaza.nps.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication and User Management Controller
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Login with client credentials
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            logger.info("Login attempt for client: {}", request.getClientId());
            LoginResponseDto response = authService.login(request);
            logger.info("Login successful for client: {}", request.getClientId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for client {}: {}", request.getClientId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Map<String, String> response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENT')")
    public ResponseEntity<UserProfileDto> getProfile() {
        try {
            UserProfileDto profile = userService.getCurrentUserProfile();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Failed to get user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENT')")
    public ResponseEntity<UserProfileDto> updateProfile(@Valid @RequestBody UserProfileDto profile) {
        try {
            UserProfileDto updatedProfile = userService.updateUserProfile(profile);
            logger.info("User profile updated successfully");
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            logger.error("Failed to update user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Change password
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENT')")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        try {
            userService.changePassword(request);
            logger.info("Password changed successfully");
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            logger.error("Failed to change password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        try {
            userService.requestPasswordReset(email);
            logger.info("Password reset requested for email: {}", email);
            return ResponseEntity.ok(Map.of("message", "Password reset instructions sent to your email"));
        } catch (Exception e) {
            logger.error("Failed to request password reset: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            logger.info("Password reset successfully");
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            logger.error("Failed to reset password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Logout
     */
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CLIENT')")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            authService.logout();
            logger.info("User logged out successfully");
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
