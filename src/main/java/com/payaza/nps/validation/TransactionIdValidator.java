package com.payaza.nps.validation;

import com.payaza.nps.security.ClientContext;
import org.springframework.stereotype.Component;

/**
 * Validator for transaction IDs
 */
@Component
public class TransactionIdValidator {
    
    /**
     * Validate transaction ID format and uniqueness
     */
    public void validateTransactionId(String transactionId, String clientId) {
        if (!isValidTransactionId(transactionId)) {
            throw new IllegalArgumentException("Invalid transaction ID format: " + transactionId);
        }
    }
    
    /**
     * Check if transaction ID is valid
     * Valid format: PREFIX-NUMBER (e.g., TST-123456789)
     */
    public boolean isValidTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return false;
        }
        
        // Get the current client's transaction prefix
        String clientPrefix = ClientContext.getCurrentClientPrefix();
        if (clientPrefix == null || clientPrefix.trim().isEmpty()) {
            return false;
        }
        
        // Check if transaction ID starts with the client's prefix followed by a hyphen
        String expectedPrefix = clientPrefix + "-";
        if (!transactionId.startsWith(expectedPrefix)) {
            return false;
        }
        
        // Check if there's a number after the prefix
        String numberPart = transactionId.substring(expectedPrefix.length());
        if (numberPart.isEmpty()) {
            return false;
        }
        
        // Check if the number part contains only digits
        try {
            Long.parseLong(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Generate a new transaction ID
     */
    public String generateTransactionId() {
        String clientPrefix = ClientContext.getCurrentClientPrefix();
        if (clientPrefix == null || clientPrefix.trim().isEmpty()) {
            clientPrefix = "TXN";
        }
        return clientPrefix + "-" + System.currentTimeMillis();
    }
}