package com.payaza.nps.service;

import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Authentication Service for JWT token management
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private InternalClientRegistry clientRegistry;
    
    /**
     * Authenticate user and generate JWT token
     */
    public LoginResponseDto login(LoginRequestDto request) {
        logger.info("Authentication attempt for client: {}", request.getClientId());
        
        // Validate client credentials
        InternalClient client = clientRegistry.getClientByApiKey(request.getApiKey());
        if (client == null || !client.getClientId().equals(request.getClientId())) {
            logger.warn("Authentication failed for client: {} - Invalid credentials", request.getClientId());
            throw new RuntimeException("Invalid client credentials");
        }
        
        if (!client.isActive()) {
            logger.warn("Authentication failed for client: {} - Client inactive", request.getClientId());
            throw new RuntimeException("Client account is inactive");
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(client);
        String refreshToken = jwtTokenProvider.generateRefreshToken(client);
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationDate(token);
        
        // Update last login
        client.setLastActivity(LocalDateTime.now());
        // Note: In a real implementation, you would update the client in the database
        // clientRegistry.updateClient(client);
        
        logger.info("Authentication successful for client: {}", request.getClientId());
        
        return new LoginResponseDto(
            token,
            client.getClientId(),
            client.getClientName(),
            client.getClientType(),
            getClientPermissions(client),
            expiresAt
        );
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
        InternalClient client = clientRegistry.getClientById(clientId);
        
        if (client == null || !client.isActive()) {
            logger.warn("Token refresh failed for client: {} - Client not found or inactive", clientId);
            throw new RuntimeException("Client not found or inactive");
        }
        
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
     * Get client permissions based on client type
     */
    private List<String> getClientPermissions(InternalClient client) {
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
