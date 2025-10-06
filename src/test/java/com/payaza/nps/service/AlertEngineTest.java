package com.payaza.nps.service;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertRule;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.model.AlertStatus;
import com.payaza.nps.repository.AlertRepository;
import com.payaza.nps.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlertEngine
 */
@ExtendWith(MockitoExtension.class)
class AlertEngineTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertHistoryService alertHistoryService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertEngine alertEngine;

    private AlertRule testRule;
    private Map<String, Object> testMetrics;

    @BeforeEach
    void setUp() {
        // Setup test alert rule
        testRule = new AlertRule();
        testRule.setId(1L);
        testRule.setName("High Error Rate");
        testRule.setDescription("Alert when error rate exceeds 5%");
        testRule.setMetricName("error_rate");
        testRule.setCondition("GREATER_THAN");
        testRule.setThreshold(5.0);
        testRule.setSeverity(AlertSeverity.WARNING);
        testRule.setEnabled(true);
        testRule.setEvaluationInterval(300); // 5 minutes

        // Setup test metrics
        testMetrics = new HashMap<>();
        testMetrics.put("error_rate", 8.5);
        testMetrics.put("success_rate", 91.5);
        testMetrics.put("response_time", 1200.0);
        testMetrics.put("transaction_volume", 1500);
    }

    @Test
    void evaluateRules_ShouldTriggerAlert_WhenConditionMet() {
        // Given
        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));
        when(alertRepository.findByRuleIdAndStatus(testRule.getId(), AlertStatus.ACTIVE))
                .thenReturn(null); // No existing active alert

        // When
        alertEngine.evaluateRules(testMetrics);

        // Then
        verify(alertRepository).save(any(Alert.class));
        verify(notificationService).sendAlert(any(Alert.class), anyList());
        verify(alertHistoryService).recordAlertAction(eq(testRule.getId()), any(), anyString(), anyString());
    }

    @Test
    void evaluateRules_ShouldNotTriggerAlert_WhenConditionNotMet() {
        // Given
        testMetrics.put("error_rate", 2.0); // Below threshold
        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));

        // When
        alertEngine.evaluateRules(testMetrics);

        // Then
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).sendAlert(any(Alert.class), anyList());
    }

    @Test
    void evaluateRules_ShouldNotCreateDuplicateAlert_WhenActiveAlertExists() {
        // Given
        Alert existingAlert = new Alert();
        existingAlert.setId(1L);
        existingAlert.setRuleId(testRule.getId());
        existingAlert.setStatus(AlertStatus.ACTIVE);

        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));
        when(alertRepository.findByRuleIdAndStatus(testRule.getId(), AlertStatus.ACTIVE))
                .thenReturn(existingAlert);

        // When
        alertEngine.evaluateRules(testMetrics);

        // Then
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).sendAlert(any(Alert.class), anyList());
    }

    @Test
    void evaluateRules_ShouldResolveAlert_WhenConditionNoLongerMet() {
        // Given
        testMetrics.put("error_rate", 2.0); // Below threshold
        Alert existingAlert = new Alert();
        existingAlert.setId(1L);
        existingAlert.setRuleId(testRule.getId());
        existingAlert.setStatus(AlertStatus.ACTIVE);

        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));
        when(alertRepository.findByRuleIdAndStatus(testRule.getId(), AlertStatus.ACTIVE))
                .thenReturn(existingAlert);

        // When
        alertEngine.evaluateRules(testMetrics);

        // Then
        verify(alertRepository).save(existingAlert);
        assertEquals(AlertStatus.RESOLVED, existingAlert.getStatus());
        verify(alertHistoryService).recordAlertAction(eq(1L), any(), anyString(), anyString());
    }

    @Test
    void evaluateRules_ShouldHandleMultipleRules() {
        // Given
        AlertRule rule2 = new AlertRule();
        rule2.setId(2L);
        rule2.setName("High Response Time");
        rule2.setDescription("Alert when response time exceeds 2000ms");
        rule2.setMetricName("response_time");
        rule2.setCondition("GREATER_THAN");
        rule2.setThreshold(2000.0);
        rule2.setSeverity(AlertSeverity.WARNING);
        rule2.setEnabled(true);

        List<AlertRule> rules = Arrays.asList(testRule, rule2);
        when(alertRuleRepository.findByEnabledTrue()).thenReturn(rules);
        when(alertRepository.findByRuleIdAndStatus(anyLong(), eq(AlertStatus.ACTIVE)))
                .thenReturn(null);

        // When
        alertEngine.evaluateRules(testMetrics);

        // Then
        verify(alertRepository, times(1)).save(any(Alert.class)); // Only first rule should trigger
        verify(notificationService, times(1)).sendAlert(any(Alert.class), anyList());
    }

    @Test
    void evaluateRules_ShouldHandleMissingMetrics() {
        // Given
        Map<String, Object> incompleteMetrics = new HashMap<>();
        incompleteMetrics.put("success_rate", 95.0);
        // Missing error_rate metric

        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));

        // When
        alertEngine.evaluateRules(incompleteMetrics);

        // Then
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).sendAlert(any(Alert.class), anyList());
    }

    @Test
    void evaluateRules_ShouldHandleInvalidMetricValues() {
        // Given
        Map<String, Object> invalidMetrics = new HashMap<>();
        invalidMetrics.put("error_rate", "invalid_value");
        invalidMetrics.put("success_rate", null);

        when(alertRuleRepository.findByEnabledTrue()).thenReturn(Arrays.asList(testRule));

        // When
        alertEngine.evaluateRules(invalidMetrics);

        // Then
        verify(alertRepository, never()).save(any(Alert.class));
        verify(notificationService, never()).sendAlert(any(Alert.class), anyList());
    }

    @Test
    void evaluateCondition_GreaterThan_ShouldWorkCorrectly() {
        // Given
        testRule.setCondition("GREATER_THAN");
        testRule.setThreshold(5.0);

        // When & Then
        assertTrue(alertEngine.evaluateCondition(testRule, 8.5));
        assertFalse(alertEngine.evaluateCondition(testRule, 3.0));
        assertFalse(alertEngine.evaluateCondition(testRule, 5.0));
    }

    @Test
    void evaluateCondition_LessThan_ShouldWorkCorrectly() {
        // Given
        testRule.setCondition("LESS_THAN");
        testRule.setThreshold(90.0);

        // When & Then
        assertTrue(alertEngine.evaluateCondition(testRule, 85.0));
        assertFalse(alertEngine.evaluateCondition(testRule, 95.0));
        assertFalse(alertEngine.evaluateCondition(testRule, 90.0));
    }

    @Test
    void evaluateCondition_Equals_ShouldWorkCorrectly() {
        // Given
        testRule.setCondition("EQUALS");
        testRule.setThreshold(100.0);

        // When & Then
        assertTrue(alertEngine.evaluateCondition(testRule, 100.0));
        assertFalse(alertEngine.evaluateCondition(testRule, 99.9));
        assertFalse(alertEngine.evaluateCondition(testRule, 100.1));
    }

    @Test
    void evaluateCondition_NotEquals_ShouldWorkCorrectly() {
        // Given
        testRule.setCondition("NOT_EQUALS");
        testRule.setThreshold(0.0);

        // When & Then
        assertTrue(alertEngine.evaluateCondition(testRule, 5.0));
        assertFalse(alertEngine.evaluateCondition(testRule, 0.0));
    }

    @Test
    void evaluateCondition_InvalidCondition_ShouldReturnFalse() {
        // Given
        testRule.setCondition("INVALID_CONDITION");
        testRule.setThreshold(5.0);

        // When & Then
        assertFalse(alertEngine.evaluateCondition(testRule, 8.5));
    }
}
