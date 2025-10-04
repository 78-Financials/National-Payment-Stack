package com.payaza.nps.model;

/**
 * Enumeration for payment types supported by NPS
 */
public enum PaymentType {
    TRANSFER("Transfer"),
    PAYMENT("Payment"),
    COLLECTION("Collection"),
    REMITTANCE("Remittance"),
    SALARY("Salary"),
    BULK_TRANSFER("Bulk Transfer"),
    BILL_PAYMENT("Bill Payment");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
