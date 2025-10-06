package com.payaza.nps.repository;

import com.payaza.nps.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by client ID
     */
    Page<AuditLog> findByClientId(String clientId, Pageable pageable);
    
    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find audit logs by action type
     */
    Page<AuditLog> findByActionType(AuditLog.ActionType actionType, Pageable pageable);
    
    /**
     * Find audit logs by status
     */
    Page<AuditLog> findByStatus(AuditLog.AuditStatus status, Pageable pageable);
    
    /**
     * Find audit logs by timestamp range
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Find audit logs by client ID and timestamp range
     */
    Page<AuditLog> findByClientIdAndTimestampBetween(String clientId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Find audit logs by action pattern
     */
    Page<AuditLog> findByActionContainingIgnoreCase(String actionPattern, Pageable pageable);
    
    /**
     * Find audit logs by resource pattern
     */
    Page<AuditLog> findByResourceContainingIgnoreCase(String resourcePattern, Pageable pageable);
    
    /**
     * Find recent audit logs for a client
     */
    @Query("SELECT a FROM AuditLog a WHERE a.clientId = :clientId ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentByClientId(@Param("clientId") String clientId, Pageable pageable);
    
    /**
     * Count audit logs by action type
     */
    long countByActionType(AuditLog.ActionType actionType);
    
    /**
     * Count audit logs by status
     */
    long countByStatus(AuditLog.AuditStatus status);
    
    /**
     * Count audit logs by client ID
     */
    long countByClientId(String clientId);
    
    /**
     * Find audit logs with errors
     */
    @Query("SELECT a FROM AuditLog a WHERE a.status = 'ERROR' ORDER BY a.timestamp DESC")
    Page<AuditLog> findErrors(Pageable pageable);
    
    /**
     * Find audit logs for failed operations
     */
    @Query("SELECT a FROM AuditLog a WHERE a.status IN ('FAILURE', 'ERROR') ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailures(Pageable pageable);
    
    /**
     * Find audit logs by request ID
     */
    List<AuditLog> findByRequestId(String requestId);
    
    /**
     * Find audit logs by session ID
     */
    Page<AuditLog> findBySessionId(String sessionId, Pageable pageable);
    
    /**
     * Find audit logs with high execution time
     */
    @Query("SELECT a FROM AuditLog a WHERE a.executionTimeMs > :threshold ORDER BY a.executionTimeMs DESC")
    Page<AuditLog> findSlowOperations(@Param("threshold") Long thresholdMs, Pageable pageable);
    
    /**
     * Get audit statistics for a date range
     */
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.actionType")
    List<Object[]> getActionTypeStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get audit statistics by status for a date range
     */
    @Query("SELECT a.status, COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.status")
    List<Object[]> getStatusStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get top clients by audit log count
     */
    @Query("SELECT a.clientId, COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.clientId ORDER BY COUNT(a) DESC")
    List<Object[]> getTopClientsByActivity(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    /**
     * Delete old audit logs
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);
    
    /**
     * Count total audit logs for a client
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.clientId = :clientId AND a.timestamp BETWEEN :start AND :end")
    long countByClientIdAndTimestampBetween(@Param("clientId") String clientId, 
                                           @Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);
}
