package com.payaza.nps.validation;

import com.payaza.nps.security.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validator for transaction ID format and client prefix
 */
@Component
public class TransactionIdValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionIdValidator.class);
    
    /**
     * Validates transaction ID format and client prefix
     * 
     * @param transactionId The transaction ID to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            logger.warn("Transaction ID is null or empty");
            return false;
        }
        
        String currentClientPrefix = ClientContext.getCurrentClientPrefix();
        if (currentClientPrefix == null) {
            logger.warn("No client context found for transaction ID validation");
            return false;
        }
        
        // Check if transaction ID starts with client prefix followed by hyphen
        String expectedPrefix = currentClientPrefix + "-";
        if (!transactionId.startsWith(expectedPrefix)) {
            logger.warn("Transaction ID '{}' does not start with expected prefix '{}'", 
                       transactionId, expectedPrefix);
            return false;
        }
        
        // Check length constraints (3-35 characters as per ISO 20022)
        if (transactionId.length() < 3 || transactionId.length() > 35) {
            logger.warn("Transaction ID '{}' length {} is invalid (must be 3-35 characters)", 
                       transactionId, transactionId.length());
            return false;
        }
        
        // Check if it contains only valid characters (alphanumeric and some special chars)
        if (!transactionId.matches("^[A-Za-z0-9_-]+$")) {
            logger.warn("Transaction ID '{}' contains invalid characters", transactionId);
            return false;
        }
        
        // Additional validation: ensure minimum length for hyphen-separated format
        // Format: PREFIX-XXXXX (minimum 6 characters: 3 + 1 + 1 = 5, but we need at least 6)
        int minLength = currentClientPrefix.length() + 2; // prefix + hyphen + at least 1 character
        if (transactionId.length() < minLength) {
            logger.warn("Transaction ID '{}' is too short for prefix format '{}' (minimum {} characters)", 
                       transactionId, expectedPrefix, minLength);
            return false;
        }
        
        logger.debug("Transaction ID '{}' is valid for client prefix '{}'", 
                    transactionId, currentClientPrefix);
        return true;
    }
    
    /**
     * Validates transaction ID with specific client prefix
     * 
     * @param transactionId The transaction ID to validate
     * @param clientPrefix The expected client prefix
     * @return true if valid, false otherwise
     */
    public boolean isValidTransactionId(String transactionId, String clientPrefix) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            logger.warn("Transaction ID is null or empty");
            return false;
        }
        
        if (clientPrefix == null || clientPrefix.trim().isEmpty()) {
            logger.warn("Client prefix is null or empty");
            return false;
        }
        
        // Check if transaction ID starts with client prefix followed by hyphen
        String expectedPrefix = clientPrefix + "-";
        if (!transactionId.startsWith(expectedPrefix)) {
            logger.warn("Transaction ID '{}' does not start with expected prefix '{}'", 
                       transactionId, expectedPrefix);
            return false;
        }
        
        // Check length constraints (3-35 characters as per ISO 20022)
        if (transactionId.length() < 3 || transactionId.length() > 35) {
            logger.warn("Transaction ID '{}' length {} is invalid (must be 3-35 characters)", 
                       transactionId, transactionId.length());
            return false;
        }
        
        // Check if it contains only valid characters (alphanumeric and some special chars)
        if (!transactionId.matches("^[A-Za-z0-9_-]+$")) {
            logger.warn("Transaction ID '{}' contains invalid characters", transactionId);
            return false;
        }
        
        logger.debug("Transaction ID '{}' is valid for client prefix '{}'", 
                    transactionId, clientPrefix);
        return true;
    }
    
    /**
     * Generates a valid transaction ID for the current client
     * 
     * @return A valid transaction ID
     */
    public String generateTransactionId() {
        String currentClientPrefix = ClientContext.getCurrentClientPrefix();
        if (currentClientPrefix == null) {
            throw new IllegalStateException("No client context found for transaction ID generation");
        }
        
        // Generate timestamp-based suffix
        long timestamp = System.currentTimeMillis();
        String suffix = String.valueOf(timestamp);
        
        // Combine prefix with hyphen and timestamp
        String transactionId = currentClientPrefix + "-" + suffix;
        
        // Ensure it doesn't exceed 35 characters
        if (transactionId.length() > 35) {
            int maxSuffixLength = 35 - currentClientPrefix.length() - 1; // -1 for hyphen
            transactionId = currentClientPrefix + "-" + suffix.substring(0, maxSuffixLength);
        }
        
        logger.debug("Generated transaction ID '{}' for client prefix '{}'", 
                    transactionId, currentClientPrefix);
        return transactionId;
    }
}
