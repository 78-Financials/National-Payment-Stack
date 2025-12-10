package com.payaza.nps.service;

import com.payaza.nps.model.BankPerformanceDaily;
import com.payaza.nps.repository.BankPerformanceDailyRepository;
import com.payaza.nps.repository.PaymentTransactionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for historical analytics and reporting
 * Provides comprehensive analysis of payment transaction data over time
 */
@Service
public class HistoricalAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoricalAnalyticsService.class);

    @Autowired
    private PaymentTransactionHistoryRepository historyRepository;
    
    @Autowired
    private BankPerformanceDailyRepository bankPerformanceRepository;

    /**
     * Get transaction volume trends over time
     */
    public List<Object[]> getTransactionVolumeTrends(LocalDate startDate, LocalDate endDate, String period) {
        logger.info("Getting transaction volume trends from {} to {} for period: {}", startDate, endDate, period);
        
        try {
            switch (period.toUpperCase()) {
                case "DAILY":
                    return getDailyTransactionVolume(startDate, endDate);
                case "WEEKLY":
                    return getWeeklyTransactionVolume(startDate, endDate);
                case "MONTHLY":
                    return getMonthlyTransactionVolume(startDate, endDate);
                default:
                    throw new IllegalArgumentException("Invalid period: " + period + ". Must be DAILY, WEEKLY, or MONTHLY");
            }
        } catch (Exception e) {
            logger.error("Error getting transaction volume trends: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get daily transaction volume
     */
    private List<Object[]> getDailyTransactionVolume(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        return historyRepository.getDailyTransactionCountsByStatus(start, end);
    }

    /**
     * Get weekly transaction volume
     */
    private List<Object[]> getWeeklyTransactionVolume(LocalDate startDate, LocalDate endDate) {
        // Implementation for weekly aggregation
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        return historyRepository.getWeeklyTransactionPatterns(start, end);
    }

    /**
     * Get monthly transaction volume
     */
    private List<Object[]> getMonthlyTransactionVolume(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        return historyRepository.getMonthlyTransactionVolume(start, end);
    }

    /**
     * Get bank performance analysis for date range
     */
    public List<Object[]> getBankPerformanceAnalysis(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting bank performance analysis from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getBankPerformanceSummary(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting bank performance analysis: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get bank success rates for date range
     */
    public List<Object[]> getBankSuccessRates(LocalDate startDate, LocalDate endDate, long minTransactions) {
        logger.info("Getting bank success rates from {} to {} with min transactions: {}", startDate, endDate, minTransactions);
        
        try {
            return historyRepository.getBankSuccessRates(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59),
                minTransactions
            );
        } catch (Exception e) {
            logger.error("Error getting bank success rates: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get bank processing time analysis
     */
    public List<Object[]> getBankProcessingTimes(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting bank processing times from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getBankProcessingTimes(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting bank processing times: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get top performing banks
     */
    public List<Object[]> getTopPerformingBanks(LocalDate startDate, LocalDate endDate, long minVolume) {
        logger.info("Getting top performing banks from {} to {} with min volume: {}", startDate, endDate, minVolume);
        
        try {
            return historyRepository.getTopPerformingBanks(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59),
                minVolume
            );
        } catch (Exception e) {
            logger.error("Error getting top performing banks: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get client activity analysis
     */
    public List<Object[]> getClientActivityAnalysis(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting client activity analysis from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getClientActivitySummary(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting client activity analysis: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get error analysis for date range
     */
    public List<Object[]> getErrorAnalysis(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting error analysis from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getErrorAnalysis(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting error analysis: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get hourly transaction patterns
     */
    public List<Object[]> getHourlyTransactionPatterns(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting hourly transaction patterns from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getHourlyTransactionPatterns(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting hourly transaction patterns: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get currency distribution
     */
    public List<Object[]> getCurrencyDistribution(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting currency distribution from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getCurrencyDistribution(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting currency distribution: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get amount distribution by ranges
     */
    public List<Object[]> getAmountDistribution(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting amount distribution from {} to {}", startDate, endDate);
        
        try {
            return historyRepository.getAmountDistribution(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59)
            );
        } catch (Exception e) {
            logger.error("Error getting amount distribution: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get comprehensive dashboard metrics for date range
     */
    public Map<String, Object> getDashboardMetrics(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting dashboard metrics from {} to {}", startDate, endDate);
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            
            // Get basic counts
            long totalTransactions = historyRepository.countByRequestCreatedAtBetween(start, end);
            long successfulTransactions = historyRepository.countByRequestCreatedAtBetweenAndFinalStatus(start, end, "SUCCESS");
            long failedTransactions = historyRepository.countByRequestCreatedAtBetweenAndFinalStatus(start, end, "FAILED");
            long timeoutTransactions = historyRepository.countByRequestCreatedAtBetweenAndFinalStatus(start, end, "TIMEOUT");
            
            // Get total amounts
            BigDecimal totalAmount = historyRepository.getTotalAmountByDateRange(start, end);
            if (totalAmount == null) {
                totalAmount = BigDecimal.ZERO;
            }
            Double avgProcessingTime = historyRepository.getAverageProcessingTimeByDateRange(start, end);
            
            // Calculate derived metrics
            double successRate = totalTransactions > 0 ? (double) successfulTransactions / totalTransactions * 100 : 0.0;
            double failureRate = totalTransactions > 0 ? (double) failedTransactions / totalTransactions * 100 : 0.0;
            double timeoutRate = totalTransactions > 0 ? (double) timeoutTransactions / totalTransactions * 100 : 0.0;
            
            return Map.of(
                "dateRange", Map.of(
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
                ),
                "transactionMetrics", Map.of(
                    "totalTransactions", totalTransactions,
                    "successfulTransactions", successfulTransactions,
                    "failedTransactions", failedTransactions,
                    "timeoutTransactions", timeoutTransactions,
                    "successRate", Math.round(successRate * 100.0) / 100.0,
                    "failureRate", Math.round(failureRate * 100.0) / 100.0,
                    "timeoutRate", Math.round(timeoutRate * 100.0) / 100.0
                ),
                "financialMetrics", Map.of(
                    "totalAmount", totalAmount,
                    "averageTransactionAmount", totalTransactions > 0 ? 
                        totalAmount.divide(BigDecimal.valueOf(totalTransactions), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO
                ),
                "performanceMetrics", Map.of(
                    "averageProcessingTimeMs", avgProcessingTime != null ? avgProcessingTime : 0.0,
                    "averageProcessingTimeSeconds", avgProcessingTime != null ? avgProcessingTime / 1000.0 : 0.0
                )
            );
            
        } catch (Exception e) {
            logger.error("Error getting dashboard metrics: {}", e.getMessage(), e);
            return Map.of("error", "Failed to retrieve dashboard metrics");
        }
    }

    /**
     * Get bank performance trends over time
     */
    public List<BankPerformanceDaily> getBankPerformanceTrends(String bankCode, LocalDate startDate, LocalDate endDate) {
        logger.info("Getting bank performance trends for bank {} from {} to {}", bankCode, startDate, endDate);
        
        try {
            return bankPerformanceRepository.findByBankCodeAndDateBetween(bankCode, startDate, endDate);
        } catch (Exception e) {
            logger.error("Error getting bank performance trends: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get daily performance trends for all banks
     */
    public List<Object[]> getDailyPerformanceTrends(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting daily performance trends from {} to {}", startDate, endDate);
        
        try {
            return bankPerformanceRepository.getDailyPerformanceTrends(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error getting daily performance trends: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get monthly performance summary
     */
    public List<Object[]> getMonthlyPerformanceSummary(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting monthly performance summary from {} to {}", startDate, endDate);
        
        try {
            return bankPerformanceRepository.getMonthlyPerformanceSummary(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error getting monthly performance summary: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get banks with declining performance
     */
    public List<Object[]> getBanksWithDecliningPerformance(LocalDate recentDate, LocalDate previousDate, int minTransactions) {
        logger.info("Getting banks with declining performance between {} and {}", recentDate, previousDate);
        
        try {
            return bankPerformanceRepository.findBanksWithDecliningPerformance(recentDate, previousDate, minTransactions);
        } catch (Exception e) {
            logger.error("Error getting banks with declining performance: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get banks with improving performance
     */
    public List<Object[]> getBanksWithImprovingPerformance(LocalDate recentDate, LocalDate previousDate, int minTransactions) {
        logger.info("Getting banks with improving performance between {} and {}", recentDate, previousDate);
        
        try {
            return bankPerformanceRepository.findBanksWithImprovingPerformance(recentDate, previousDate, minTransactions);
        } catch (Exception e) {
            logger.error("Error getting banks with improving performance: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get bank reliability scores
     */
    public List<Object[]> getBankReliabilityScores(LocalDate startDate, LocalDate endDate, int minDays) {
        logger.info("Getting bank reliability scores from {} to {} with min days: {}", startDate, endDate, minDays);
        
        try {
            return bankPerformanceRepository.getBankReliabilityScores(startDate, endDate, minDays);
        } catch (Exception e) {
            logger.error("Error getting bank reliability scores: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get banks with high error rates
     */
    public List<BankPerformanceDaily> getBanksWithHighErrorRates(LocalDate startDate, LocalDate endDate, BigDecimal threshold) {
        logger.info("Getting banks with high error rates from {} to {} above threshold: {}", startDate, endDate, threshold);
        
        try {
            return bankPerformanceRepository.findBanksWithHighErrorRates(startDate, endDate, threshold);
        } catch (Exception e) {
            logger.error("Error getting banks with high error rates: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get transaction volume by date
     */
    public List<Object[]> getTransactionVolumesByDate(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting transaction volumes by date from {} to {}", startDate, endDate);
        
        try {
            return bankPerformanceRepository.getDailyTransactionVolumes(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error getting transaction volumes by date: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get average daily metrics
     */
    public Map<String, Object> getAverageDailyMetrics(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting average daily metrics from {} to {}", startDate, endDate);
        
        try {
            Object[] metrics = bankPerformanceRepository.getAverageDailyMetrics(startDate, endDate);
            
            if (metrics != null && metrics.length >= 5) {
                return Map.of(
                    "averageTotalRequests", metrics[0],
                    "averageSuccessfulRequests", metrics[1],
                    "averageSuccessRate", metrics[2],
                    "averageProcessingTimeMs", metrics[3],
                    "averageTotalAmount", metrics[4]
                );
            } else {
                return Map.of("error", "Insufficient data for metrics calculation");
            }
            
        } catch (Exception e) {
            logger.error("Error getting average daily metrics: {}", e.getMessage(), e);
            return Map.of("error", "Failed to retrieve average daily metrics");
        }
    }
}
