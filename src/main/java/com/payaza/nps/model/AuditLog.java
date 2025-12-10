package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for storing audit logs of all system actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_client_id", columnList = "clientId"),
    @Index(name = "idx_audit_log_user_id", columnList = "userId")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Timestamp is required")
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @NotBlank(message = "Action is required")
    @Size(max = 100, message = "Action must not exceed 100 characters")
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @NotBlank(message = "Resource is required")
    @Size(max = 200, message = "Resource must not exceed 200 characters")
    @Column(name = "resource", nullable = false, length = 200)
    private String resource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    @Size(max = 50, message = "User ID must not exceed 50 characters")
    @Column(name = "user_id", length = 50)
    private String userId;
    
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    @Column(name = "client_id", length = 50)
    private String clientId;
    
    @Size(max = 100, message = "IP address must not exceed 100 characters")
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
    
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_id", length = 100)
    private String requestId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuditStatus status;
    
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(name = "message", length = 1000)
    private String message;
    
    @Size(max = 5000, message = "Details must not exceed 5000 characters")
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Size(max = 100, message = "Error code must not exceed 100 characters")
    @Column(name = "error_code", length = 100)
    private String errorCode;
    
    @Size(max = 1000, message = "Error message must not exceed 1000 characters")
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(String action, String resource, ActionType actionType, String userId, String clientId) {
        this.action = action;
        this.resource = resource;
        this.actionType = actionType;
        this.userId = userId;
        this.clientId = clientId;
        this.status = AuditStatus.SUCCESS;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public AuditStatus getStatus() { return status; }
    public void setStatus(AuditStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public void setRequestInfo(Map<String, Object> requestInfo) {
        // Convert Map to JSON string for storage
        if (requestInfo != null && !requestInfo.isEmpty()) {
            this.details = requestInfo.toString(); // Simple conversion, can be enhanced with JSON library
        }
    }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    // Enums
    public enum ActionType {
        CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, 
        API_CALL, SYSTEM_EVENT, SECURITY_EVENT, 
        ADMIN_ACTION, CLIENT_ACTION, ERROR
    }

    public enum AuditStatus {
        SUCCESS, FAILURE, ERROR, WARNING
    }

    // Utility methods
    public void setSuccess(String message) {
        this.status = AuditStatus.SUCCESS;
        this.message = message;
    }

    public void setFailure(String message, String errorCode) {
        this.status = AuditStatus.FAILURE;
        this.message = message;
        this.errorCode = errorCode;
    }

    public void setError(String message, String errorCode, String errorMessage) {
        this.status = AuditStatus.ERROR;
        this.message = message;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
               "id=" + id +
               ", timestamp=" + timestamp +
               ", action='" + action + '\'' +
               ", resource='" + resource + '\'' +
               ", actionType=" + actionType +
               ", userId='" + userId + '\'' +
               ", clientId='" + clientId + '\'' +
               ", status=" + status +
               ", message='" + message + '\'' +
               '}';
    }
}
