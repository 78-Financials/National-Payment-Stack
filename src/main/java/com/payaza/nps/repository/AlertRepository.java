package com.payaza.nps.repository;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.model.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Alert entities
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    /**
     * Find alerts by status
     */
    List<Alert> findByStatus(AlertStatus status);
    
    /**
     * Find alerts by severity
     */
    List<Alert> findBySeverity(AlertSeverity severity);
    
    /**
     * Find active alerts
     */
    List<Alert> findByStatusIn(List<AlertStatus> statuses);
    
    /**
     * Find alerts by alert rule ID
     */
    List<Alert> findByAlertRuleId(Long alertRuleId);
    
    /**
     * Find alerts by alert rule ID and status
     */
    List<Alert> findByAlertRuleIdAndStatus(Long alertRuleId, AlertStatus status);
    
    /**
     * Find alerts created after a specific date
     */
    List<Alert> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Find alerts created between dates
     */
    List<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find alerts by severity and status
     */
    List<Alert> findBySeverityAndStatus(AlertSeverity severity, AlertStatus status);
    
    /**
     * Find alerts by severity and status with pagination
     */
    Page<Alert> findBySeverityAndStatus(AlertSeverity severity, AlertStatus status, Pageable pageable);
    
    /**
     * Find alerts by status with pagination
     */
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    
    /**
     * Find alerts by severity with pagination
     */
    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);
    
    /**
     * Find alerts by metric name
     */
    List<Alert> findByMetricName(String metricName);
    
    /**
     * Find alerts by metric type
     */
    List<Alert> findByMetricType(String metricType);
    
    /**
     * Find alerts by metric name and type
     */
    List<Alert> findByMetricNameAndMetricType(String metricName, String metricType);
    
    /**
     * Find recent alerts (last 24 hours)
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);
    
    /**
     * Find active alerts by severity
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ACTIVE' AND a.severity = :severity")
    List<Alert> findActiveAlertsBySeverity(@Param("severity") AlertSeverity severity);
    
    /**
     * Find alerts that need escalation
     */
    @Query("SELECT a FROM Alert a WHERE a.status IN ('ACTIVE', 'ACKNOWLEDGED') " +
           "AND a.escalatedAt IS NULL AND a.createdAt < :escalationTime")
    List<Alert> findAlertsNeedingEscalation(@Param("escalationTime") LocalDateTime escalationTime);
    
    /**
     * Find alerts that need notification (haven't been notified recently)
     */
    @Query("SELECT a FROM Alert a WHERE a.status = 'ACTIVE' " +
           "AND (a.lastNotificationSent IS NULL OR a.lastNotificationSent < :notificationTime)")
    List<Alert> findAlertsNeedingNotification(@Param("notificationTime") LocalDateTime notificationTime);
    
    /**
     * Count alerts by status
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = :status")
    long countByStatus(@Param("status") AlertStatus status);
    
    /**
     * Count alerts by severity and status
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = :severity AND a.status = :status")
    long countBySeverityAndStatus(@Param("severity") AlertSeverity severity, @Param("status") AlertStatus status);
    
    /**
     * Count alerts created today
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE DATE(a.createdAt) = CURRENT_DATE")
    long countToday();
    
    /**
     * Count alerts created today by severity
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE DATE(a.createdAt) = CURRENT_DATE AND a.severity = :severity")
    long countTodayBySeverity(@Param("severity") AlertSeverity severity);
    
    /**
     * Find alerts with high notification count (potential spam)
     */
    @Query("SELECT a FROM Alert a WHERE a.notificationCount > :maxNotifications")
    List<Alert> findAlertsWithHighNotificationCount(@Param("maxNotifications") Integer maxNotifications);
    
    /**
     * Find duplicate alerts (same rule, metric, and recent timestamp)
     */
    @Query("SELECT a FROM Alert a WHERE a.alertRuleId = :alertRuleId " +
           "AND a.metricName = :metricName AND a.metricType = :metricType " +
           "AND a.createdAt >= :since AND a.status = 'ACTIVE'")
    List<Alert> findDuplicateAlerts(@Param("alertRuleId") Long alertRuleId,
                                   @Param("metricName") String metricName,
                                   @Param("metricType") String metricType,
                                   @Param("since") LocalDateTime since);
    
    /**
     * Update alert status
     */
    @Modifying
    @Query("UPDATE Alert a SET a.status = :status, a.updatedAt = :updatedAt WHERE a.id = :id")
    int updateAlertStatus(@Param("id") Long id, @Param("status") AlertStatus status, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * Acknowledge alert
     */
    @Modifying
    @Query("UPDATE Alert a SET a.status = 'ACKNOWLEDGED', a.acknowledgedAt = :acknowledgedAt, " +
           "a.acknowledgedBy = :acknowledgedBy WHERE a.id = :id")
    int acknowledgeAlert(@Param("id") Long id, @Param("acknowledgedAt") LocalDateTime acknowledgedAt, @Param("acknowledgedBy") String acknowledgedBy);
    
    /**
     * Resolve alert
     */
    @Modifying
    @Query("UPDATE Alert a SET a.status = 'RESOLVED', a.resolvedAt = :resolvedAt, " +
           "a.resolvedBy = :resolvedBy WHERE a.id = :id")
    int resolveAlert(@Param("id") Long id, @Param("resolvedAt") LocalDateTime resolvedAt, @Param("resolvedBy") String resolvedBy);
    
    /**
     * Mark alert as escalated
     */
    @Modifying
    @Query("UPDATE Alert a SET a.escalatedAt = :escalatedAt WHERE a.id = :id")
    int markAlertAsEscalated(@Param("id") Long id, @Param("escalatedAt") LocalDateTime escalatedAt);
    
    /**
     * Update notification count
     */
    @Modifying
    @Query("UPDATE Alert a SET a.notificationCount = a.notificationCount + 1, " +
           "a.lastNotificationSent = :lastNotificationSent WHERE a.id = :id")
    int incrementNotificationCount(@Param("id") Long id, @Param("lastNotificationSent") LocalDateTime lastNotificationSent);
    
    /**
     * Find alerts for dashboard summary
     */
    @Query("SELECT a.severity, COUNT(a) FROM Alert a WHERE a.status = 'ACTIVE' GROUP BY a.severity")
    List<Object[]> findActiveAlertCountsBySeverity();
    
    /**
     * Find recent alert trends (last 7 days)
     */
    @Query("SELECT DATE(a.createdAt) as alertDate, a.severity, COUNT(a) " +
           "FROM Alert a WHERE a.createdAt >= :since " +
           "GROUP BY DATE(a.createdAt), a.severity ORDER BY alertDate DESC")
    List<Object[]> findAlertTrends(@Param("since") LocalDateTime since);
}
