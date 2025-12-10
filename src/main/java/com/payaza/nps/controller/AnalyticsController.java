package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.model.PaymentTransactionLive;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.HistoricalAnalyticsService;
import com.payaza.nps.service.EndOfDayProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for payment analytics and dashboard
 */
@RestController
@RequestMapping("/api/v1/admin/analytics")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private PaymentStatusTrackingService statusTrackingService;

    @Autowired
    private HistoricalAnalyticsService historicalAnalyticsService;

    @Autowired
    private EndOfDayProcessingService eodProcessingService;

    /**
     * Get real-time dashboard metrics
     */
    @GetMapping("/dashboard/realtime")
    @Auditable(action = "VIEW_REALTIME_DASHBOARD", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed real-time dashboard")
    public ResponseEntity<Map<String, Object>> getRealtimeDashboard() {
        try {
            logger.info("Fetching real-time dashboard metrics");
            
            Map<String, Object> metrics = statusTrackingService.getTransactionStatistics();
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Error fetching real-time dashboard metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get live transactions for dashboard
     */
    @GetMapping("/dashboard/live-transactions")
    @Auditable(action = "VIEW_LIVE_TRANSACTIONS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed live transactions")
    public ResponseEntity<List<PaymentTransactionLive>> getLiveTransactions() {
        try {
            logger.info("Fetching live transactions for dashboard");
            
            List<PaymentTransactionLive> transactions = statusTrackingService.getLiveTransactions();
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            logger.error("Error fetching live transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get today's bank performance summary
     */
    @GetMapping("/dashboard/bank-performance")
    @Auditable(action = "VIEW_BANK_PERFORMANCE", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed bank performance summary")
    public ResponseEntity<List<Object[]>> getBankPerformanceSummary() {
        try {
            logger.info("Fetching today's bank performance summary");
            
            List<Object[]> bankPerformance = statusTrackingService.getTodayBankPerformanceSummary();
            
            return ResponseEntity.ok(bankPerformance);
            
        } catch (Exception e) {
            logger.error("Error fetching bank performance summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get high-value transactions
     */
    @GetMapping("/transactions/high-value")
    @Auditable(action = "VIEW_HIGH_VALUE_TRANSACTIONS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed high-value transactions")
    public ResponseEntity<List<PaymentTransactionLive>> getHighValueTransactions(
            @RequestParam(defaultValue = "100000") BigDecimal threshold) {
        try {
            logger.info("Fetching high-value transactions above threshold: {}", threshold);
            
            List<PaymentTransactionLive> transactions = statusTrackingService.getHighValueTransactions(threshold);
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            logger.error("Error fetching high-value transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get slow transactions
     */
    @GetMapping("/transactions/slow")
    @Auditable(action = "VIEW_SLOW_TRANSACTIONS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed slow transactions")
    public ResponseEntity<List<PaymentTransactionLive>> getSlowTransactions(
            @RequestParam(defaultValue = "5000") long thresholdMs) {
        try {
            logger.info("Fetching slow transactions above threshold: {}ms", thresholdMs);
            
            List<PaymentTransactionLive> transactions = statusTrackingService.getSlowTransactions(thresholdMs);
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            logger.error("Error fetching slow transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get failed transactions
     */
    @GetMapping("/transactions/failed")
    @Auditable(action = "VIEW_FAILED_TRANSACTIONS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed failed transactions")
    public ResponseEntity<List<PaymentTransactionLive>> getFailedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("Fetching failed transactions (page: {}, size: {})", page, size);
            
            List<PaymentTransactionLive> transactions = statusTrackingService.getFailedTransactions(page, size);
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            logger.error("Error fetching failed transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get transactions for a specific client
     */
    @GetMapping("/client/{clientId}/transactions")
    @Auditable(action = "VIEW_CLIENT_TRANSACTIONS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed client transactions")
    public ResponseEntity<List<PaymentTransactionLive>> getClientTransactions(@PathVariable String clientId) {
        try {
            logger.info("Fetching transactions for client: {}", clientId);
            
            List<PaymentTransactionLive> transactions = statusTrackingService.getRecentTransactionsForClient(clientId);
            
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            logger.error("Error fetching client transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/transaction/{transactionId}")
    @Auditable(action = "VIEW_TRANSACTION_DETAILS", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed transaction details")
    public ResponseEntity<PaymentTransactionLive> getTransactionDetails(@PathVariable String transactionId) {
        try {
            logger.info("Fetching transaction details for ID: {}", transactionId);
            
            return statusTrackingService.getTransactionByTransactionId(transactionId)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            logger.error("Error fetching transaction details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get system health metrics
     */
    @GetMapping("/system/health")
    @Auditable(action = "VIEW_SYSTEM_HEALTH", resource = "Analytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed system health metrics")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            logger.info("Fetching system health metrics");
            
            Map<String, Object> healthMetrics = statusTrackingService.getTransactionStatistics();
            
            // Add additional health indicators
            healthMetrics.put("systemStatus", "HEALTHY");
            healthMetrics.put("lastUpdated", System.currentTimeMillis());
            
            return ResponseEntity.ok(healthMetrics);
            
        } catch (Exception e) {
            logger.error("Error fetching system health metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== HISTORICAL ANALYTICS ENDPOINTS ====================

    /**
     * Get transaction volume trends
     */
    @GetMapping("/historical/volume-trends")
    @Auditable(action = "VIEW_VOLUME_TRENDS", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed transaction volume trends")
    public ResponseEntity<List<Object[]>> getVolumeTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String period) {
        try {
            logger.info("Fetching volume trends from {} to {} for period: {}", startDate, endDate, period);
            
            List<Object[]> trends = historicalAnalyticsService.getTransactionVolumeTrends(startDate, endDate, period);
            
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            logger.error("Error fetching volume trends: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get bank performance analysis
     */
    @GetMapping("/historical/bank-performance")
    @Auditable(action = "VIEW_BANK_PERFORMANCE_HISTORICAL", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed historical bank performance")
    public ResponseEntity<List<Object[]>> getBankPerformanceAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching bank performance analysis from {} to {}", startDate, endDate);
            
            List<Object[]> performance = historicalAnalyticsService.getBankPerformanceAnalysis(startDate, endDate);
            
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            logger.error("Error fetching bank performance analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get bank success rates
     */
    @GetMapping("/historical/bank-success-rates")
    @Auditable(action = "VIEW_BANK_SUCCESS_RATES", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed bank success rates")
    public ResponseEntity<List<Object[]>> getBankSuccessRates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "100") long minTransactions) {
        try {
            logger.info("Fetching bank success rates from {} to {} with min transactions: {}", startDate, endDate, minTransactions);
            
            List<Object[]> successRates = historicalAnalyticsService.getBankSuccessRates(startDate, endDate, minTransactions);
            
            return ResponseEntity.ok(successRates);
            
        } catch (Exception e) {
            logger.error("Error fetching bank success rates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top performing banks
     */
    @GetMapping("/historical/top-performing-banks")
    @Auditable(action = "VIEW_TOP_PERFORMING_BANKS", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed top performing banks")
    public ResponseEntity<List<Object[]>> getTopPerformingBanks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "50") long minVolume) {
        try {
            logger.info("Fetching top performing banks from {} to {} with min volume: {}", startDate, endDate, minVolume);
            
            List<Object[]> topBanks = historicalAnalyticsService.getTopPerformingBanks(startDate, endDate, minVolume);
            
            return ResponseEntity.ok(topBanks);
            
        } catch (Exception e) {
            logger.error("Error fetching top performing banks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get client activity analysis
     */
    @GetMapping("/historical/client-activity")
    @Auditable(action = "VIEW_CLIENT_ACTIVITY", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed client activity analysis")
    public ResponseEntity<List<Object[]>> getClientActivityAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching client activity analysis from {} to {}", startDate, endDate);
            
            List<Object[]> clientActivity = historicalAnalyticsService.getClientActivityAnalysis(startDate, endDate);
            
            return ResponseEntity.ok(clientActivity);
            
        } catch (Exception e) {
            logger.error("Error fetching client activity analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get error analysis
     */
    @GetMapping("/historical/error-analysis")
    @Auditable(action = "VIEW_ERROR_ANALYSIS", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed error analysis")
    public ResponseEntity<List<Object[]>> getErrorAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching error analysis from {} to {}", startDate, endDate);
            
            List<Object[]> errorAnalysis = historicalAnalyticsService.getErrorAnalysis(startDate, endDate);
            
            return ResponseEntity.ok(errorAnalysis);
            
        } catch (Exception e) {
            logger.error("Error fetching error analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get hourly transaction patterns
     */
    @GetMapping("/historical/hourly-patterns")
    @Auditable(action = "VIEW_HOURLY_PATTERNS", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed hourly transaction patterns")
    public ResponseEntity<List<Object[]>> getHourlyPatterns(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching hourly patterns from {} to {}", startDate, endDate);
            
            List<Object[]> hourlyPatterns = historicalAnalyticsService.getHourlyTransactionPatterns(startDate, endDate);
            
            return ResponseEntity.ok(hourlyPatterns);
            
        } catch (Exception e) {
            logger.error("Error fetching hourly patterns: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get currency distribution
     */
    @GetMapping("/historical/currency-distribution")
    @Auditable(action = "VIEW_CURRENCY_DISTRIBUTION", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed currency distribution")
    public ResponseEntity<List<Object[]>> getCurrencyDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching currency distribution from {} to {}", startDate, endDate);
            
            List<Object[]> currencyDistribution = historicalAnalyticsService.getCurrencyDistribution(startDate, endDate);
            
            return ResponseEntity.ok(currencyDistribution);
            
        } catch (Exception e) {
            logger.error("Error fetching currency distribution: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get amount distribution
     */
    @GetMapping("/historical/amount-distribution")
    @Auditable(action = "VIEW_AMOUNT_DISTRIBUTION", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed amount distribution")
    public ResponseEntity<List<Object[]>> getAmountDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching amount distribution from {} to {}", startDate, endDate);
            
            List<Object[]> amountDistribution = historicalAnalyticsService.getAmountDistribution(startDate, endDate);
            
            return ResponseEntity.ok(amountDistribution);
            
        } catch (Exception e) {
            logger.error("Error fetching amount distribution: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comprehensive dashboard metrics for date range
     */
    @GetMapping("/historical/dashboard-metrics")
    @Auditable(action = "VIEW_HISTORICAL_DASHBOARD", resource = "HistoricalAnalytics", actionType = AuditLog.ActionType.READ, message = "Admin viewed historical dashboard metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Fetching dashboard metrics from {} to {}", startDate, endDate);
            
            Map<String, Object> metrics = historicalAnalyticsService.getDashboardMetrics(startDate, endDate);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== EOD PROCESSING ENDPOINTS ====================

    /**
     * Get EOD processing status
     */
    @GetMapping("/eod/status")
    @Auditable(action = "VIEW_EOD_STATUS", resource = "EodProcessing", actionType = AuditLog.ActionType.READ, message = "Admin viewed EOD processing status")
    public ResponseEntity<Map<String, Object>> getEodProcessingStatus() {
        try {
            logger.info("Fetching EOD processing status");
            
            Map<String, Object> status = eodProcessingService.getEodProcessingStatus();
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Error fetching EOD processing status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Manually trigger EOD processing
     */
    @PostMapping("/eod/trigger")
    @Auditable(action = "TRIGGER_EOD_PROCESSING", resource = "EodProcessing", actionType = AuditLog.ActionType.SYSTEM_EVENT, message = "Admin triggered manual EOD processing")
    public ResponseEntity<Map<String, Object>> triggerEodProcessing(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        try {
            logger.info("Manual EOD processing triggered for date: {}", targetDate);
            
            String result = eodProcessingService.triggerEodProcessing(targetDate);
            
            return ResponseEntity.ok(Map.of(
                "message", result,
                "targetDate", targetDate.toString(),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            logger.error("Error triggering EOD processing: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to trigger EOD processing: " + e.getMessage()
            ));
        }
    }
}
