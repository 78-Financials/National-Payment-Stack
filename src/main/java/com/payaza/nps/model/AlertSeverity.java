package com.payaza.nps.model;

/**
 * Enumeration for alert severity levels
 */
public enum AlertSeverity {
    CRITICAL("Critical", 1, "#FF0000"),  // Red
    WARNING("Warning", 2, "#FFA500"),    // Orange
    INFO("Information", 3, "#0080FF");   // Blue
    
    private final String displayName;
    private final int priority;
    private final String color;
    
    AlertSeverity(String displayName, int priority, String color) {
        this.displayName = displayName;
        this.priority = priority;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public int getPriority() { return priority; }
    public String getColor() { return color; }
    
    /**
     * Check if this severity is higher priority than another
     */
    public boolean isHigherPriorityThan(AlertSeverity other) {
        return this.priority < other.priority;
    }
    
    /**
     * Check if this severity requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == CRITICAL;
    }
    
    /**
     * Get severity by priority number
     */
    public static AlertSeverity byPriority(int priority) {
        for (AlertSeverity severity : values()) {
            if (severity.priority == priority) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Invalid priority: " + priority);
    }
}
