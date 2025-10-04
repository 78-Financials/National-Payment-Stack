package com.payaza.nps.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for error responses
 */
public class ErrorResponseDto {

    private String errorCode;
    private String errorMessage;
    private Map<String, String> details;
    private LocalDateTime timestamp;

    // Constructors
    public ErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDto(String errorCode, String errorMessage) {
        this();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResponseDto(String errorCode, String errorMessage, Map<String, String> details, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
