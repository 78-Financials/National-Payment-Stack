package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity for storing triggered alerts
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alerts_rule_id", columnList = "alertRuleId"),
    @Index(name = "idx_alerts_severity", columnList = "severity"),
    @Index(name = "idx_alerts_status", columnList = "status"),
    @Index(name = "idx_alerts_created", columnList = "createdAt"),
    @Index(name = "idx_alerts_rule_status", columnList = "alertRuleId, status")
})
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Alert rule ID is required")
    @Column(name = "alert_rule_id", nullable = false)
    private Long alertRuleId;
    
    @NotBlank(message = "Alert name is required")
    @Size(max = 100, message = "Alert name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(name = "message", length = 1000)
    private String message;
    
    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ACTIVE;
    
    @ElementCollection
    @CollectionTable(name = "alert_channels", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "channel")
    private List<String> notificationChannels;
    
    @Column(name = "metric_value")
    private Double metricValue; // The actual value that triggered the alert
    
    @Column(name = "threshold_value")
    private Double thresholdValue; // The threshold that was exceeded
    
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    @Column(name = "metric_name", length = 100)
    private String metricName;
    
    @Size(max = 100, message = "Metric type must not exceed 100 characters")
    @Column(name = "metric_type", length = 100)
    private String metricType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @Size(max = 100, message = "Acknowledged by must not exceed 100 characters")
    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Size(max = 100, message = "Resolved by must not exceed 100 characters")
    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;
    
    @Column(name = "last_notification_sent")
    private LocalDateTime lastNotificationSent;
    
    @Column(name = "notification_count", nullable = false)
    private Integer notificationCount = 0;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    @Size(max = 1000, message = "Context must not exceed 1000 characters")
    @Column(name = "context", length = 1000)
    private String context; // JSON string with additional context
    
    // Constructors
    public Alert() {}
    
    public Alert(Long alertRuleId, String name, String message, AlertSeverity severity) {
        this.alertRuleId = alertRuleId;
        this.name = name;
        this.message = message;
        this.severity = severity;
        this.status = AlertStatus.ACTIVE;
        this.notificationCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlertRuleId() { return alertRuleId; }
    public void setAlertRuleId(Long alertRuleId) { this.alertRuleId = alertRuleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public List<String> getNotificationChannels() { return notificationChannels; }
    public void setNotificationChannels(List<String> notificationChannels) { this.notificationChannels = notificationChannels; }

    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }

    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getLastNotificationSent() { return lastNotificationSent; }
    public void setLastNotificationSent(LocalDateTime lastNotificationSent) { this.lastNotificationSent = lastNotificationSent; }

    public Integer getNotificationCount() { return notificationCount; }
    public void setNotificationCount(Integer notificationCount) { this.notificationCount = notificationCount; }

    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    // Helper methods
    public boolean isActive() {
        return AlertStatus.ACTIVE.equals(status);
    }

    public boolean isAcknowledged() {
        return AlertStatus.ACKNOWLEDGED.equals(status);
    }

    public boolean isResolved() {
        return AlertStatus.RESOLVED.equals(status);
    }

    public boolean isSuppressed() {
        return AlertStatus.SUPPRESSED.equals(status);
    }

    public void acknowledge(String acknowledgedBy) {
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = acknowledgedBy;
    }

    public void resolve(String resolvedBy) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }

    public void suppress() {
        this.status = AlertStatus.SUPPRESSED;
    }

    public void incrementNotificationCount() {
        this.notificationCount++;
        this.lastNotificationSent = LocalDateTime.now();
    }

    public boolean shouldEscalate(LocalDateTime now, java.time.Duration escalationWindow) {
        if (escalatedAt != null) {
            return false; // Already escalated
        }
        
        return now.isAfter(createdAt.plus(escalationWindow));
    }

    @Override
    public String toString() {
        return "Alert{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", severity=" + severity +
               ", status=" + status +
               ", createdAt=" + createdAt +
               '}';
    }
}
