package com.payaza.nps.repository;

import com.payaza.nps.model.AlertHistory;
import com.payaza.nps.model.AlertAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AlertHistory entities
 */
@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    
    /**
     * Find alert history by alert ID
     */
    List<AlertHistory> findByAlertId(Long alertId);
    
    /**
     * Find alert history by action
     */
    List<AlertHistory> findByAction(AlertAction action);
    
    /**
     * Find alert history by user ID
     */
    List<AlertHistory> findByUserId(String userId);
    
    /**
     * Find alert history created after a specific date
     */
    List<AlertHistory> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Find alert history created between dates
     */
    List<AlertHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find alert history by alert ID and action
     */
    List<AlertHistory> findByAlertIdAndAction(Long alertId, AlertAction action);
    
    /**
     * Find recent alert history (last 24 hours)
     */
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.createdAt >= :since ORDER BY ah.createdAt DESC")
    List<AlertHistory> findRecentHistory(@Param("since") LocalDateTime since);
    
    /**
     * Find alert history by alert ID ordered by creation time
     */
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.alertId = :alertId ORDER BY ah.createdAt DESC")
    List<AlertHistory> findByAlertIdOrderByCreatedAtDesc(@Param("alertId") Long alertId);
    
    /**
     * Count alert actions by type
     */
    @Query("SELECT ah.action, COUNT(ah) FROM AlertHistory ah WHERE ah.createdAt >= :since GROUP BY ah.action")
    List<Object[]> countActionsByType(@Param("since") LocalDateTime since);
    
    /**
     * Find user activity (actions by user)
     */
    @Query("SELECT ah.userId, ah.action, COUNT(ah) FROM AlertHistory ah " +
           "WHERE ah.createdAt >= :since AND ah.userId IS NOT NULL " +
           "GROUP BY ah.userId, ah.action ORDER BY COUNT(ah) DESC")
    List<Object[]> findUserActivity(@Param("since") LocalDateTime since);
    
    /**
     * Find alert resolution times
     */
    @Query("SELECT ah.alertId, MIN(ah.createdAt) as created, MAX(ah.createdAt) as resolved " +
           "FROM AlertHistory ah WHERE ah.action = 'RESOLVED' AND ah.createdAt >= :since " +
           "GROUP BY ah.alertId")
    List<Object[]> findAlertResolutionTimes(@Param("since") LocalDateTime since);
}
