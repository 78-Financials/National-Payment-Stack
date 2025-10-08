package com.payaza.nps.service;

import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.dto.ChangePasswordRequestDto;
import com.payaza.nps.dto.PasswordResetRequestDto;
import com.payaza.nps.dto.PasswordResetConfirmDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Service for JWT token management and user authentication
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private InternalClientRepository clientRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private AccountLockoutService accountLockoutService;
    
    /**
     * Authenticate user with email/password and generate JWT token
     */
    public LoginResponseDto login(LoginRequestDto request) {
        logger.info("Authentication attempt for email: {}", request.getEmail());
        
        try {
            // Check if account is locked
            if (accountLockoutService.isAccountLocked(request.getEmail())) {
                logger.warn("Authentication failed for email: {} - Account locked", request.getEmail());
                throw new BadCredentialsException("Account is locked due to too many failed login attempts");
            }
            
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get client details
            Optional<InternalClient> clientOpt = clientRepository.findByEmail(request.getEmail());
            if (clientOpt.isEmpty()) {
                logger.error("Client not found after successful authentication: {}", request.getEmail());
                throw new RuntimeException("Client not found");
            }
            
            InternalClient client = clientOpt.get();
            
            // Record successful login
            accountLockoutService.recordSuccessfulLogin(request.getEmail());
            
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(client);
            String refreshToken = jwtTokenProvider.generateRefreshToken(client);
            LocalDateTime expiresAt = jwtTokenProvider.getExpirationDate(token);
            
            logger.info("Authentication successful for email: {} (clientId: {})", 
                       request.getEmail(), client.getClientId());
            
            return new LoginResponseDto(
                token,
                client.getClientId(),
                client.getClientName(),
                client.getClientType(),
                getClientPermissions(client),
                expiresAt
            );
            
        } catch (BadCredentialsException e) {
            // Record failed login attempt
            accountLockoutService.recordFailedLogin(request.getEmail());
            logger.warn("Authentication failed for email: {} - {}", request.getEmail(), e.getMessage());
            // Re-throw the original exception to preserve the error message
            throw e;
        } catch (AuthenticationException e) {
            // Record failed login attempt
            accountLockoutService.recordFailedLogin(request.getEmail());
            logger.warn("Authentication failed for email: {} - {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
    
    /**
     * Refresh JWT token
     */
    public Map<String, String> refreshToken(String token) {
        logger.debug("Token refresh attempt");
        
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn("Token refresh failed - Invalid token");
            throw new RuntimeException("Invalid or expired token");
        }
        
        String clientId = jwtTokenProvider.getClientIdFromToken(token);
        Optional<InternalClient> clientOpt = clientRepository.findByClientId(clientId);
        
        if (clientOpt.isEmpty() || !clientOpt.get().isActive()) {
            logger.warn("Token refresh failed for client: {} - Client not found or inactive", clientId);
            throw new RuntimeException("Client not found or inactive");
        }
        
        InternalClient client = clientOpt.get();
        String newToken = jwtTokenProvider.generateToken(client);
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationDate(newToken);
        
        logger.debug("Token refresh successful for client: {}", clientId);
        
        return Map.of(
            "token", newToken,
            "expiresAt", expiresAt.toString()
        );
    }
    
    /**
     * Logout user (invalidate token)
     */
    public void logout() {
        // In a real implementation, you would add the token to a blacklist
        // For now, we'll just log the logout
        logger.info("User logged out");
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(ChangePasswordRequestDto request, String email) {
        logger.info("Password change request for email: {}", email);
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            logger.warn("Password change failed - Passwords do not match");
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            logger.warn("Password change failed - User not found: {}", email);
            throw new RuntimeException("User not found");
        }
        
        InternalClient client = clientOpt.get();
        
        // Verify current password
        if (!passwordService.verifyPassword(request.getCurrentPassword(), client.getPassword())) {
            logger.warn("Password change failed - Invalid current password for: {}", email);
            throw new BadCredentialsException("Invalid current password");
        }
        
        // Validate new password strength
        if (!passwordService.isPasswordStrong(request.getNewPassword())) {
            logger.warn("Password change failed - Weak new password for: {}", email);
            throw new IllegalArgumentException("New password does not meet strength requirements");
        }
        
        // Update password
        String encodedPassword = passwordService.encodePassword(request.getNewPassword());
        client.setPassword(encodedPassword);
        clientRepository.save(client);
        
        logger.info("Password changed successfully for email: {}", email);
        return true;
    }
    
    /**
     * Request password reset
     */
    public boolean requestPasswordReset(PasswordResetRequestDto request) {
        logger.info("Password reset request for email: {}", request.getEmail());
        return passwordResetService.initiatePasswordReset(request.getEmail());
    }
    
    /**
     * Confirm password reset
     */
    public boolean confirmPasswordReset(PasswordResetConfirmDto request) {
        logger.info("Password reset confirmation attempt");
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            logger.warn("Password reset failed - Passwords do not match");
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        return passwordResetService.confirmPasswordReset(request.getToken(), request.getNewPassword());
    }
    
    /**
     * Unlock user account (admin operation)
     */
    public boolean unlockAccount(String email) {
        logger.info("Account unlock request for email: {}", email);
        return accountLockoutService.unlockAccount(email);
    }
    
    /**
     * Get client permissions based on client type
     */
    public List<String> getClientPermissions(InternalClient client) {
        String clientType = client.getClientType();
        if (clientType == null) {
            clientType = "BANK";
        }
        
        switch (clientType.toUpperCase()) {
            case "BANK":
                return Arrays.asList(
                    "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
                    "TRANSACTION_HISTORY", "CLIENT_PROFILE"
                );
            case "FIN":
                return Arrays.asList(
                    "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
                    "TRANSACTION_HISTORY", "CLIENT_PROFILE", "ANALYTICS_VIEW"
                );
            case "PAY":
                return Arrays.asList(
                    "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
                    "TRANSACTION_HISTORY", "CLIENT_PROFILE", "ANALYTICS_VIEW", 
                    "WEBHOOK_MANAGE"
                );
            default:
                return Arrays.asList("CLIENT_PROFILE");
        }
    }
}
