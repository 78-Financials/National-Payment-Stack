package com.payaza.nps.dto;

import java.time.LocalDateTime;

/**
 * DTO for error messages sent to SQS error queue
 */
public class ErrorQueueMessage {
    
    private String transactionId;
    private String xmlMessage;
    private String error;
    private String errorType;
    private LocalDateTime timestamp;
    private String stackTrace;
    private String context;
    private String severity;

    // Constructors
    public ErrorQueueMessage() {
        this.timestamp = LocalDateTime.now();
        this.severity = "ERROR";
    }

    public ErrorQueueMessage(String transactionId, String error, String errorType) {
        this();
        this.transactionId = transactionId;
        this.error = error;
        this.errorType = errorType;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getXmlMessage() {
        return xmlMessage;
    }

    public void setXmlMessage(String xmlMessage) {
        this.xmlMessage = xmlMessage;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "ErrorQueueMessage{" +
                "transactionId='" + transactionId + '\'' +
                ", errorType='" + errorType + '\'' +
                ", error='" + error + '\'' +
                ", severity='" + severity + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
