package com.payaza.nps.model;

/**
 * Enumeration for payment status in NPS processing
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SUCCESS("Success"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    TIMEOUT("Timeout"),
    REJECTED("Rejected");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
