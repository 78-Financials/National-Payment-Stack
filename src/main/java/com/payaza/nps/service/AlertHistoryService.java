package com.payaza.nps.service;

import com.payaza.nps.model.AlertAction;
import com.payaza.nps.model.AlertHistory;
import com.payaza.nps.model.AlertStatus;
import com.payaza.nps.repository.AlertHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing alert history and audit trail
 */
@Service
public class AlertHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertHistoryService.class);
    
    @Autowired
    private AlertHistoryRepository alertHistoryRepository;
    
    /**
     * Record an alert action in history
     */
    @Transactional
    public void recordAlertAction(Long alertId, AlertAction action, String userId) {
        try {
            AlertHistory history = new AlertHistory(alertId, action, userId);
            alertHistoryRepository.save(history);
            
            logger.debug("Recorded alert action: alertId={}, action={}, userId={}", 
                        alertId, action, userId);
            
        } catch (Exception e) {
            logger.error("Error recording alert action: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Record an alert action with details
     */
    @Transactional
    public void recordAlertAction(Long alertId, AlertAction action, String userId, String details) {
        try {
            AlertHistory history = new AlertHistory(alertId, action, userId);
            history.setDetails(details);
            alertHistoryRepository.save(history);
            
            logger.debug("Recorded alert action: alertId={}, action={}, userId={}, details={}", 
                        alertId, action, userId, details);
            
        } catch (Exception e) {
            logger.error("Error recording alert action: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Record an alert action with status change
     */
    @Transactional
    public void recordAlertAction(Long alertId, AlertAction action, AlertStatus oldStatus, 
                                 AlertStatus newStatus, String userId, String details) {
        try {
            AlertHistory history = new AlertHistory(alertId, action, oldStatus, newStatus, userId);
            history.setDetails(details);
            alertHistoryRepository.save(history);
            
            logger.debug("Recorded alert action: alertId={}, action={}, oldStatus={}, newStatus={}, userId={}", 
                        alertId, action, oldStatus, newStatus, userId);
            
        } catch (Exception e) {
            logger.error("Error recording alert action: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get alert history for a specific alert
     */
    public List<AlertHistory> getAlertHistory(Long alertId) {
        try {
            return alertHistoryRepository.findByAlertIdOrderByCreatedAtDesc(alertId);
        } catch (Exception e) {
            logger.error("Error getting alert history: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get recent alert history
     */
    public List<AlertHistory> getRecentAlertHistory(int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            return alertHistoryRepository.findRecentHistory(since);
        } catch (Exception e) {
            logger.error("Error getting recent alert history: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get alert history by user
     */
    public List<AlertHistory> getAlertHistoryByUser(String userId, int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<AlertHistory> allHistory = alertHistoryRepository.findRecentHistory(since);
            
            return allHistory.stream()
                    .filter(history -> userId.equals(history.getUserId()))
                    .toList();
        } catch (Exception e) {
            logger.error("Error getting alert history by user: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get alert history by action type
     */
    public List<AlertHistory> getAlertHistoryByAction(AlertAction action, int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<AlertHistory> allHistory = alertHistoryRepository.findRecentHistory(since);
            
            return allHistory.stream()
                    .filter(history -> action.equals(history.getAction()))
                    .toList();
        } catch (Exception e) {
            logger.error("Error getting alert history by action: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
