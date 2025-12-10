package com.payaza.nps.model;

/**
 * Enumeration for alert actions
 */
public enum AlertAction {
    CREATED("Created", "Alert was created"),
    ACKNOWLEDGED("Acknowledged", "Alert was acknowledged"),
    RESOLVED("Resolved", "Alert was resolved"),
    SUPPRESSED("Suppressed", "Alert was suppressed"),
    ESCALATED("Escalated", "Alert was escalated"),
    NOTIFICATION_SENT("Notification Sent", "Notification was sent"),
    NOTIFICATION_FAILED("Notification Failed", "Notification failed to send"),
    EXPIRED("Expired", "Alert expired"),
    REACTIVATED("Reactivated", "Alert was reactivated");
    
    private final String displayName;
    private final String description;
    
    AlertAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
