package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing alert history and audit trail
 */
@Entity
@Table(name = "alert_history", indexes = {
    @Index(name = "idx_alert_history_alert_id", columnList = "alertId"),
    @Index(name = "idx_alert_history_action", columnList = "action"),
    @Index(name = "idx_alert_history_created", columnList = "createdAt")
})
public class AlertHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Alert ID is required")
    @Column(name = "alert_id", nullable = false)
    private Long alertId;
    
    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AlertAction action;
    
    @Column(name = "old_status", length = 20)
    @Enumerated(EnumType.STRING)
    private AlertStatus oldStatus;
    
    @Column(name = "new_status", length = 20)
    @Enumerated(EnumType.STRING)
    private AlertStatus newStatus;
    
    @Column(name = "user_id", length = 100)
    private String userId;
    
    @Column(name = "details", length = 1000)
    private String details;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public AlertHistory() {}
    
    public AlertHistory(Long alertId, AlertAction action, String userId) {
        this.alertId = alertId;
        this.action = action;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
    
    public AlertHistory(Long alertId, AlertAction action, AlertStatus oldStatus, AlertStatus newStatus, String userId) {
        this.alertId = alertId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public AlertAction getAction() { return action; }
    public void setAction(AlertAction action) { this.action = action; }

    public AlertStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(AlertStatus oldStatus) { this.oldStatus = oldStatus; }

    public AlertStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AlertStatus newStatus) { this.newStatus = newStatus; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "AlertHistory{" +
               "id=" + id +
               ", alertId=" + alertId +
               ", action=" + action +
               ", oldStatus=" + oldStatus +
               ", newStatus=" + newStatus +
               ", userId='" + userId + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
