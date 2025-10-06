package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Standard error response DTO
 */
public class ErrorResponseDto {
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("clientId")
    private String clientId;
    
    // Constructors
    public ErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponseDto(String error, String message) {
        this();
        this.error = error;
        this.message = message;
    }
    
    public ErrorResponseDto(String error, String message, String path) {
        this(error, message);
        this.path = path;
    }
    
    public ErrorResponseDto(String error, String message, String path, String clientId) {
        this(error, message, path);
        this.clientId = clientId;
    }
    
    // Getters and Setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}