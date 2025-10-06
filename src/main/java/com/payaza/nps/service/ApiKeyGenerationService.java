package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for generating secure API keys
 */
@Service
public class ApiKeyGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyGenerationService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // Character sets for different key types
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHANUMERIC_WITH_SPECIAL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    private static final String HEXADECIMAL = "0123456789ABCDEF";
    
    /**
     * Generate a secure API key with default format
     * Format: {prefix}_{random_string}
     * Example: nps_abc123def456ghi789jkl012mno345pqr678stu901vwx234
     */
    public String generateApiKey(String prefix) {
        return generateApiKey(prefix, 32, KeyType.ALPHANUMERIC);
    }
    
    /**
     * Generate a secure API key with custom length and type
     */
    public String generateApiKey(String prefix, int length, KeyType keyType) {
        String randomPart = generateRandomString(length, keyType);
        String apiKey = prefix + "_" + randomPart;
        
        logger.debug("Generated API key with prefix '{}' and length {}", prefix, apiKey.length());
        return apiKey;
    }
    
    /**
     * Generate a UUID-based API key
     */
    public String generateUuidApiKey() {
        return "nps_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Generate a base64-encoded API key
     */
    public String generateBase64ApiKey(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return "nps_" + encoded;
    }
    
    /**
     * Generate a client-specific API key
     */
    public String generateClientApiKey(String clientId) {
        // Generate a deterministic but secure key based on client ID
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = generateRandomString(16, KeyType.ALPHANUMERIC);
        return clientId.toLowerCase() + "_api_key_" + randomPart;
    }
    
    /**
     * Generate a random string of specified length and type
     */
    private String generateRandomString(int length, KeyType keyType) {
        String charset = getCharsetForKeyType(keyType);
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(charset.length());
            sb.append(charset.charAt(randomIndex));
        }
        
        return sb.toString();
    }
    
    /**
     * Get character set for key type
     */
    private String getCharsetForKeyType(KeyType keyType) {
        switch (keyType) {
            case ALPHANUMERIC:
                return ALPHANUMERIC;
            case ALPHANUMERIC_WITH_SPECIAL:
                return ALPHANUMERIC_WITH_SPECIAL;
            case HEXADECIMAL:
                return HEXADECIMAL;
            default:
                return ALPHANUMERIC;
        }
    }
    
    /**
     * Validate API key format
     */
    public boolean isValidApiKeyFormat(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        // Check minimum length (prefix + underscore + at least 16 characters)
        if (apiKey.length() < 20) {
            return false;
        }
        
        // Check maximum length
        if (apiKey.length() > 100) {
            return false;
        }
        
        // Check if it contains underscore (for prefix separation)
        if (!apiKey.contains("_")) {
            return false;
        }
        
        // Check if it contains only allowed characters
        for (char c : apiKey.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Extract prefix from API key
     */
    public String extractPrefix(String apiKey) {
        if (apiKey == null || !apiKey.contains("_")) {
            return null;
        }
        return apiKey.substring(0, apiKey.indexOf("_"));
    }
    
    /**
     * Key types for generation
     */
    public enum KeyType {
        ALPHANUMERIC,           // A-Z, a-z, 0-9
        ALPHANUMERIC_WITH_SPECIAL, // A-Z, a-z, 0-9, -, _
        HEXADECIMAL            // 0-9, A-F
    }
}
