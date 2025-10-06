1234package com.payaza.nps.model;

/**
 * Enumeration for alert status
 */
public enum AlertStatus {
    ACTIVE("Active", "Alert is active and requires attention"),
    ACKNOWLEDGED("Acknowledged", "Alert has been acknowledged by team member"),
    RESOLVED("Resolved", "Alert has been resolved"),
    SUPPRESSED("Suppressed", "Alert has been temporarily suppressed"),
    EXPIRED("Expired", "Alert has expired due to timeout");
    
    private final String displayName;
    private final String description;
    
    AlertStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this status indicates the alert needs attention
     */
    public boolean requiresAttention() {
        return this == ACTIVE || this == ACKNOWLEDGED;
    }
    
    /**
     * Check if this status indicates the alert is closed
     */
    public boolean isClosed() {
        return this == RESOLVED || this == SUPPRESSED || this == EXPIRED;
    }
}
