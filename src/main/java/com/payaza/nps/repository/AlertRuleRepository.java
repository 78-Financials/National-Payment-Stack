package com.payaza.nps.repository;

import com.payaza.nps.model.AlertRule;
import com.payaza.nps.model.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AlertRule entities
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    
    /**
     * Find all enabled alert rules
     */
    List<AlertRule> findByEnabledTrue();
    
    /**
     * Find alert rules by severity
     */
    List<AlertRule> findBySeverity(AlertSeverity severity);
    
    /**
     * Find enabled alert rules by severity
     */
    List<AlertRule> findByEnabledTrueAndSeverity(AlertSeverity severity);
    
    /**
     * Find alert rules by metric type
     */
    List<AlertRule> findByMetricType(String metricType);
    
    /**
     * Find enabled alert rules by metric type
     */
    List<AlertRule> findByEnabledTrueAndMetricType(String metricType);
    
    /**
     * Find alert rules by metric name
     */
    List<AlertRule> findByMetricName(String metricName);
    
    /**
     * Find enabled alert rules by metric name
     */
    List<AlertRule> findByEnabledTrueAndMetricName(String metricName);
    
    /**
     * Find alert rules created after a specific date
     */
    List<AlertRule> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Find alert rules updated after a specific date
     */
    List<AlertRule> findByUpdatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Find alert rules by creator
     */
    List<AlertRule> findByCreatedBy(String createdBy);
    
    /**
     * Count enabled alert rules by severity
     */
    @Query("SELECT COUNT(ar) FROM AlertRule ar WHERE ar.enabled = true AND ar.severity = :severity")
    long countEnabledBySeverity(@Param("severity") AlertSeverity severity);
    
    /**
     * Count alert rules by metric type
     */
    @Query("SELECT COUNT(ar) FROM AlertRule ar WHERE ar.enabled = true AND ar.metricType = :metricType")
    long countEnabledByMetricType(@Param("metricType") String metricType);
    
    /**
     * Find alert rules that need evaluation (based on evaluation interval)
     */
    @Query("SELECT ar FROM AlertRule ar WHERE ar.enabled = true " +
           "AND (ar.updatedAt < :lastEvaluation OR ar.updatedAt IS NULL)")
    List<AlertRule> findRulesNeedingEvaluation(@Param("lastEvaluation") LocalDateTime lastEvaluation);
    
    /**
     * Find alert rules with specific notification channels
     */
    @Query("SELECT DISTINCT ar FROM AlertRule ar JOIN ar.notificationChannels nc " +
           "WHERE ar.enabled = true AND nc = :channel")
    List<AlertRule> findByNotificationChannel(@Param("channel") String channel);
    
    /**
     * Find alert rules by condition expression pattern
     */
    @Query("SELECT ar FROM AlertRule ar WHERE ar.enabled = true " +
           "AND ar.conditionExpression LIKE %:pattern%")
    List<AlertRule> findByConditionPattern(@Param("pattern") String pattern);
}
