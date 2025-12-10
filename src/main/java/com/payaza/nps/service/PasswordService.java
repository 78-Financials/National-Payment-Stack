package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for password management operations
 */
@Service
public class PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    
    /**
     * Encode a raw password
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        String encoded = passwordEncoder.encode(rawPassword);
        logger.debug("Password encoded successfully");
        return encoded;
    }
    
    /**
     * Verify a raw password against an encoded password
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            logger.warn("Password verification failed: null password or encoded password");
            return false;
        }
        
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        logger.debug("Password verification result: {}", matches);
        return matches;
    }
    
    /**
     * Generate a secure random password reset token
     */
    public String generateResetToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        logger.debug("Generated password reset token");
        return token;
    }
    
    /**
     * Validate password strength
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        boolean isStrong = hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
        logger.debug("Password strength validation result: {}", isStrong);
        return isStrong;
    }
    
    /**
     * Generate a secure password with specified length
     */
    public String generateSecurePassword(int length) {
        if (length < 8) {
            length = 8; // Use minimum length of 8
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = uppercase + lowercase + digits + specialChars;
        
        // Create an array to build the password
        char[] password = new char[length];
        
        // Ensure at least one character from each required set
        password[0] = uppercase.charAt(secureRandom.nextInt(uppercase.length()));
        password[1] = lowercase.charAt(secureRandom.nextInt(lowercase.length()));
        password[2] = digits.charAt(secureRandom.nextInt(digits.length()));
        password[3] = specialChars.charAt(secureRandom.nextInt(specialChars.length()));
        
        logger.debug("Initial password array: uppercase={}, lowercase={}, digit={}, special={}", 
            password[0], password[1], password[2], password[3]);
        
        // Fill the rest with random characters
        for (int i = 4; i < length; i++) {
            password[i] = allChars.charAt(secureRandom.nextInt(allChars.length()));
        }
        
        // Shuffle the password array to randomize the positions
        for (int i = 0; i < password.length; i++) {
            int j = secureRandom.nextInt(password.length);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }
        
        String generatedPassword = new String(password);
        logger.debug("Generated secure password of length: {}", length);
        return generatedPassword;
    }
}
