package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity for defining alert rules and conditions
 */
@Entity
@Table(name = "alert_rules", indexes = {
    @Index(name = "idx_alert_rules_enabled", columnList = "enabled"),
    @Index(name = "idx_alert_rules_severity", columnList = "severity"),
    @Index(name = "idx_alert_rules_created", columnList = "createdAt")
})
public class AlertRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotBlank(message = "Condition is required")
    @Size(max = 1000, message = "Condition must not exceed 1000 characters")
    @Column(name = "condition_expression", nullable = false, length = 1000)
    private String conditionExpression; // e.g., "success_rate < 90%"
    
    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AlertSeverity severity;
    
    @ElementCollection
    @CollectionTable(name = "alert_rule_channels", joinColumns = @JoinColumn(name = "alert_rule_id"))
    @Column(name = "channel")
    private List<String> notificationChannels; // email, sms, slack, webhook
    
    @Size(max = 200, message = "Escalation policy must not exceed 200 characters")
    @Column(name = "escalation_policy", length = 200)
    private String escalationPolicy;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "evaluation_interval_seconds", nullable = false)
    private Integer evaluationIntervalSeconds = 60; // How often to check
    
    @Column(name = "suppression_window_seconds", nullable = false)
    private Integer suppressionWindowSeconds = 300; // 5 minutes default
    
    @Column(name = "max_alerts_per_hour")
    private Integer maxAlertsPerHour = 10; // Prevent spam
    
    @Size(max = 100, message = "Metric type must not exceed 100 characters")
    @Column(name = "metric_type", length = 100)
    private String metricType; // transaction, system, bank, client
    
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    @Column(name = "metric_name", length = 100)
    private String metricName; // success_rate, processing_time, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    // Constructors
    public AlertRule() {}
    
    public AlertRule(String name, String description, String conditionExpression, AlertSeverity severity) {
        this.name = name;
        this.description = description;
        this.conditionExpression = conditionExpression;
        this.severity = severity;
        this.enabled = true;
        this.evaluationIntervalSeconds = 60;
        this.suppressionWindowSeconds = 300;
        this.maxAlertsPerHour = 10;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    
    // Convenience methods for tests
    public void setCondition(String condition) { this.conditionExpression = condition; }
    public void setThreshold(double threshold) { 
        // For tests, we'll store threshold in the condition expression
        this.conditionExpression = "threshold < " + threshold;
    }
    public void setEvaluationInterval(int interval) { this.evaluationIntervalSeconds = interval; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public List<String> getNotificationChannels() { return notificationChannels; }
    public void setNotificationChannels(List<String> notificationChannels) { this.notificationChannels = notificationChannels; }

    public String getEscalationPolicy() { return escalationPolicy; }
    public void setEscalationPolicy(String escalationPolicy) { this.escalationPolicy = escalationPolicy; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getEvaluationIntervalSeconds() { return evaluationIntervalSeconds; }
    public void setEvaluationIntervalSeconds(Integer evaluationIntervalSeconds) { this.evaluationIntervalSeconds = evaluationIntervalSeconds; }

    public Integer getSuppressionWindowSeconds() { return suppressionWindowSeconds; }
    public void setSuppressionWindowSeconds(Integer suppressionWindowSeconds) { this.suppressionWindowSeconds = suppressionWindowSeconds; }

    public Integer getMaxAlertsPerHour() { return maxAlertsPerHour; }
    public void setMaxAlertsPerHour(Integer maxAlertsPerHour) { this.maxAlertsPerHour = maxAlertsPerHour; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper methods
    public Duration getEvaluationInterval() {
        return Duration.ofSeconds(evaluationIntervalSeconds);
    }

    public Duration getSuppressionWindow() {
        return Duration.ofSeconds(suppressionWindowSeconds);
    }

    public boolean isCritical() {
        return AlertSeverity.CRITICAL.equals(severity);
    }

    public boolean isWarning() {
        return AlertSeverity.WARNING.equals(severity);
    }

    public boolean isInfo() {
        return AlertSeverity.INFO.equals(severity);
    }

    @Override
    public String toString() {
        return "AlertRule{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", severity=" + severity +
               ", enabled=" + enabled +
               ", conditionExpression='" + conditionExpression + '\'' +
               '}';
    }
}
