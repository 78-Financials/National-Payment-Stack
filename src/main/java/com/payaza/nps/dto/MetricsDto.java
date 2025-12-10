package com.payaza.nps.dto;

import com.payaza.nps.model.AlertSeverity;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Objects for metrics collection and evaluation
 */
public class MetricsDto {

    /**
     * Transaction-related metrics
     */
    public static class TransactionMetrics {
        private LocalDateTime timestamp;
        private Long totalTransactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private Long timeoutTransactions;
        private Double successRate;
        private Double failureRate;
        private Double timeoutRate;
        private Double averageProcessingTimeMs;
        private Double averageProcessingTimeSeconds;
        private Double maxProcessingTimeMs;
        private Double minProcessingTimeMs;
        private Long highValueTransactions; // > 1M NGN
        private Long totalVolume;
        private Map<String, Long> transactionsByBank;
        private Map<String, Long> transactionsByClient;
        private Map<String, Long> transactionsByCurrency;
        private Map<String, Long> transactionsByHour;

        // Constructors
        public TransactionMetrics() {
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }

        public Long getSuccessfulTransactions() { return successfulTransactions; }
        public void setSuccessfulTransactions(Long successfulTransactions) { this.successfulTransactions = successfulTransactions; }

        public Long getFailedTransactions() { return failedTransactions; }
        public void setFailedTransactions(Long failedTransactions) { this.failedTransactions = failedTransactions; }

        public Long getTimeoutTransactions() { return timeoutTransactions; }
        public void setTimeoutTransactions(Long timeoutTransactions) { this.timeoutTransactions = timeoutTransactions; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Double getFailureRate() { return failureRate; }
        public void setFailureRate(Double failureRate) { this.failureRate = failureRate; }

        public Double getTimeoutRate() { return timeoutRate; }
        public void setTimeoutRate(Double timeoutRate) { this.timeoutRate = timeoutRate; }

        public Double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(Double averageProcessingTimeMs) { this.averageProcessingTimeMs = averageProcessingTimeMs; }

        public Double getAverageProcessingTimeSeconds() { return averageProcessingTimeSeconds; }
        public void setAverageProcessingTimeSeconds(Double averageProcessingTimeSeconds) { this.averageProcessingTimeSeconds = averageProcessingTimeSeconds; }

        public Double getMaxProcessingTimeMs() { return maxProcessingTimeMs; }
        public void setMaxProcessingTimeMs(Double maxProcessingTimeMs) { this.maxProcessingTimeMs = maxProcessingTimeMs; }

        public Double getMinProcessingTimeMs() { return minProcessingTimeMs; }
        public void setMinProcessingTimeMs(Double minProcessingTimeMs) { this.minProcessingTimeMs = minProcessingTimeMs; }

        public Long getHighValueTransactions() { return highValueTransactions; }
        public void setHighValueTransactions(Long highValueTransactions) { this.highValueTransactions = highValueTransactions; }

        public Long getTotalVolume() { return totalVolume; }
        public void setTotalVolume(Long totalVolume) { this.totalVolume = totalVolume; }

        public Map<String, Long> getTransactionsByBank() { return transactionsByBank; }
        public void setTransactionsByBank(Map<String, Long> transactionsByBank) { this.transactionsByBank = transactionsByBank; }

        public Map<String, Long> getTransactionsByClient() { return transactionsByClient; }
        public void setTransactionsByClient(Map<String, Long> transactionsByClient) { this.transactionsByClient = transactionsByClient; }

        public Map<String, Long> getTransactionsByCurrency() { return transactionsByCurrency; }
        public void setTransactionsByCurrency(Map<String, Long> transactionsByCurrency) { this.transactionsByCurrency = transactionsByCurrency; }

        public Map<String, Long> getTransactionsByHour() { return transactionsByHour; }
        public void setTransactionsByHour(Map<String, Long> transactionsByHour) { this.transactionsByHour = transactionsByHour; }

        // Helper methods
        public boolean hasLowSuccessRate(double threshold) {
            return successRate != null && successRate < threshold;
        }

        public boolean hasHighProcessingTime(double thresholdMs) {
            return averageProcessingTimeMs != null && averageProcessingTimeMs > thresholdMs;
        }

        public boolean hasHighVolume(Long threshold) {
            return totalTransactions != null && totalTransactions > threshold;
        }
    }

    /**
     * System health metrics
     */
    public static class SystemMetrics {
        private LocalDateTime timestamp;
        private Double cpuUsage;
        private Double memoryUsage;
        private Integer activeThreads;
        private Integer databaseConnections;
        private Long heapMemoryUsed;
        private Long heapMemoryMax;
        private Double gcTime;
        private Integer httpActiveConnections;
        private Double responseTimeMs;
        private Integer errorRate;
        private Map<String, Object> customMetrics;

        // Constructors
        public SystemMetrics() {
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

        public Double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

        public Integer getActiveThreads() { return activeThreads; }
        public void setActiveThreads(Integer activeThreads) { this.activeThreads = activeThreads; }

        public Integer getDatabaseConnections() { return databaseConnections; }
        public void setDatabaseConnections(Integer databaseConnections) { this.databaseConnections = databaseConnections; }

        public Long getHeapMemoryUsed() { return heapMemoryUsed; }
        public void setHeapMemoryUsed(Long heapMemoryUsed) { this.heapMemoryUsed = heapMemoryUsed; }

        public Long getHeapMemoryMax() { return heapMemoryMax; }
        public void setHeapMemoryMax(Long heapMemoryMax) { this.heapMemoryMax = heapMemoryMax; }

        public Double getGcTime() { return gcTime; }
        public void setGcTime(Double gcTime) { this.gcTime = gcTime; }

        public Integer getHttpActiveConnections() { return httpActiveConnections; }
        public void setHttpActiveConnections(Integer httpActiveConnections) { this.httpActiveConnections = httpActiveConnections; }

        public Double getResponseTimeMs() { return responseTimeMs; }
        public void setResponseTimeMs(Double responseTimeMs) { this.responseTimeMs = responseTimeMs; }

        public Integer getErrorRate() { return errorRate; }
        public void setErrorRate(Integer errorRate) { this.errorRate = errorRate; }

        public Map<String, Object> getCustomMetrics() { return customMetrics; }
        public void setCustomMetrics(Map<String, Object> customMetrics) { this.customMetrics = customMetrics; }

        // Helper methods
        public boolean hasHighCpuUsage(double threshold) {
            return cpuUsage != null && cpuUsage > threshold;
        }

        public boolean hasHighMemoryUsage(double threshold) {
            return memoryUsage != null && memoryUsage > threshold;
        }

        public boolean hasHighErrorRate(int threshold) {
            return errorRate != null && errorRate > threshold;
        }
    }

    /**
     * Bank-specific performance metrics
     */
    public static class BankMetrics {
        private LocalDateTime timestamp;
        private Map<String, BankPerformance> bankPerformances;
        private String worstPerformingBank;
        private String bestPerformingBank;
        private Double overallBankSuccessRate;
        private Integer totalBanks;

        // Constructors
        public BankMetrics() {
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public Map<String, BankPerformance> getBankPerformances() { return bankPerformances; }
        public void setBankPerformances(Map<String, BankPerformance> bankPerformances) { this.bankPerformances = bankPerformances; }

        public String getWorstPerformingBank() { return worstPerformingBank; }
        public void setWorstPerformingBank(String worstPerformingBank) { this.worstPerformingBank = worstPerformingBank; }

        public String getBestPerformingBank() { return bestPerformingBank; }
        public void setBestPerformingBank(String bestPerformingBank) { this.bestPerformingBank = bestPerformingBank; }

        public Double getOverallBankSuccessRate() { return overallBankSuccessRate; }
        public void setOverallBankSuccessRate(Double overallBankSuccessRate) { this.overallBankSuccessRate = overallBankSuccessRate; }

        public Integer getTotalBanks() { return totalBanks; }
        public void setTotalBanks(Integer totalBanks) { this.totalBanks = totalBanks; }
    }

    /**
     * Individual bank performance data
     */
    public static class BankPerformance {
        private String bankCode;
        private String bankName;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageProcessingTimeMs;
        private Long totalVolume;
        private AlertSeverity performanceLevel;

        // Constructors
        public BankPerformance() {}

        public BankPerformance(String bankCode, String bankName) {
            this.bankCode = bankCode;
            this.bankName = bankName;
        }

        // Getters and Setters
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }

        public Long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(Long totalRequests) { this.totalRequests = totalRequests; }

        public Long getSuccessfulRequests() { return successfulRequests; }
        public void setSuccessfulRequests(Long successfulRequests) { this.successfulRequests = successfulRequests; }

        public Long getFailedRequests() { return failedRequests; }
        public void setFailedRequests(Long failedRequests) { this.failedRequests = failedRequests; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(Double averageProcessingTimeMs) { this.averageProcessingTimeMs = averageProcessingTimeMs; }

        public Long getTotalVolume() { return totalVolume; }
        public void setTotalVolume(Long totalVolume) { this.totalVolume = totalVolume; }

        public AlertSeverity getPerformanceLevel() { return performanceLevel; }
        public void setPerformanceLevel(AlertSeverity performanceLevel) { this.performanceLevel = performanceLevel; }

        // Helper methods
        public boolean hasLowSuccessRate(double threshold) {
            return successRate != null && successRate < threshold;
        }

        public boolean hasHighProcessingTime(double thresholdMs) {
            return averageProcessingTimeMs != null && averageProcessingTimeMs > thresholdMs;
        }
    }

    /**
     * Combined metrics for alert evaluation
     */
    public static class CombinedMetrics {
        private TransactionMetrics transactionMetrics;
        private SystemMetrics systemMetrics;
        private BankMetrics bankMetrics;
        private LocalDateTime timestamp;

        // Constructors
        public CombinedMetrics() {
            this.timestamp = LocalDateTime.now();
        }

        public CombinedMetrics(TransactionMetrics transactionMetrics, SystemMetrics systemMetrics, BankMetrics bankMetrics) {
            this.transactionMetrics = transactionMetrics;
            this.systemMetrics = systemMetrics;
            this.bankMetrics = bankMetrics;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public TransactionMetrics getTransactionMetrics() { return transactionMetrics; }
        public void setTransactionMetrics(TransactionMetrics transactionMetrics) { this.transactionMetrics = transactionMetrics; }

        public SystemMetrics getSystemMetrics() { return systemMetrics; }
        public void setSystemMetrics(SystemMetrics systemMetrics) { this.systemMetrics = systemMetrics; }

        public BankMetrics getBankMetrics() { return bankMetrics; }
        public void setBankMetrics(BankMetrics bankMetrics) { this.bankMetrics = bankMetrics; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
