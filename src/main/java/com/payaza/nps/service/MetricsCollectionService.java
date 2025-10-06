package com.payaza.nps.service;

import com.payaza.nps.dto.MetricsDto;
import com.payaza.nps.model.PaymentTransactionLive;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import com.payaza.nps.repository.BankPerformanceDailyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for collecting real-time metrics from various sources
 */
@Service
public class MetricsCollectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollectionService.class);
    
    @Autowired
    private PaymentTransactionLiveRepository liveRepository;
    
    @Autowired
    private BankPerformanceDailyRepository bankPerformanceRepository;
    
    @Autowired
    private AlertEngine alertEngine;
    
    // System metrics
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    /**
     * Collect metrics every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    public void collectAndEvaluateMetrics() {
        try {
            logger.debug("Starting metrics collection");
            
            // Collect transaction metrics
            MetricsDto.TransactionMetrics transactionMetrics = collectTransactionMetrics();
            
            // Collect system metrics
            MetricsDto.SystemMetrics systemMetrics = collectSystemMetrics();
            
            // Collect bank metrics
            MetricsDto.BankMetrics bankMetrics = collectBankMetrics();
            
            // Combine all metrics
            MetricsDto.CombinedMetrics combinedMetrics = new MetricsDto.CombinedMetrics(
                transactionMetrics, systemMetrics, bankMetrics);
            
            // Evaluate alert rules
            alertEngine.evaluateRules(combinedMetrics);
            
            logger.debug("Metrics collection completed successfully");
            
        } catch (Exception e) {
            logger.error("Error collecting metrics: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Collect transaction-related metrics
     */
    public MetricsDto.TransactionMetrics collectTransactionMetrics() {
        MetricsDto.TransactionMetrics metrics = new MetricsDto.TransactionMetrics();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            // Get transaction counts from live repository
            long totalTransactions = liveRepository.count();
            long successfulTransactions = liveRepository.countByStatusAndRequestCreatedAtAfter("SUCCESS", oneHourAgo);
            long failedTransactions = liveRepository.countByStatusAndRequestCreatedAtAfter("FAILED", oneHourAgo);
            long timeoutTransactions = liveRepository.countByStatusAndRequestCreatedAtAfter("TIMEOUT", oneHourAgo);
            long pendingTransactions = liveRepository.countByStatusAndRequestCreatedAtAfter("PENDING", oneHourAgo);
            
            // Calculate rates
            double successRate = totalTransactions > 0 ? 
                (double) successfulTransactions / totalTransactions * 100 : 0.0;
            double failureRate = totalTransactions > 0 ? 
                (double) failedTransactions / totalTransactions * 100 : 0.0;
            double timeoutRate = totalTransactions > 0 ? 
                (double) timeoutTransactions / totalTransactions * 100 : 0.0;
            
            // Get recent transactions for processing time analysis
            List<PaymentTransactionLive> recentTransactions = liveRepository.findByRequestCreatedAtBetween(
                oneHourAgo, now);
            
            double avgProcessingTime = 0.0;
            double maxProcessingTime = 0.0;
            double minProcessingTime = Double.MAX_VALUE;
            long highValueCount = 0;
            long totalVolume = 0;
            
            for (PaymentTransactionLive transaction : recentTransactions) {
                if (transaction.getProcessingTimeMs() != null) {
                    Double processingTime = transaction.getProcessingTimeMs().doubleValue();
                    avgProcessingTime += processingTime;
                    maxProcessingTime = Math.max(maxProcessingTime, processingTime);
                    minProcessingTime = Math.min(minProcessingTime, processingTime);
                }
            }
            
            if (!recentTransactions.isEmpty()) {
                avgProcessingTime = avgProcessingTime / recentTransactions.size();
            }
            if (minProcessingTime == Double.MAX_VALUE) {
                minProcessingTime = 0.0;
            }
            
            // Set metrics
            metrics.setTotalTransactions(totalTransactions);
            metrics.setSuccessfulTransactions(successfulTransactions);
            metrics.setFailedTransactions(failedTransactions);
            metrics.setTimeoutTransactions(timeoutTransactions);
            metrics.setSuccessRate(successRate);
            metrics.setFailureRate(failureRate);
            metrics.setTimeoutRate(timeoutRate);
            metrics.setAverageProcessingTimeMs(avgProcessingTime);
            metrics.setAverageProcessingTimeSeconds(avgProcessingTime / 1000.0);
            metrics.setMaxProcessingTimeMs(maxProcessingTime);
            metrics.setMinProcessingTimeMs(minProcessingTime);
            metrics.setHighValueTransactions(highValueCount);
            metrics.setTotalVolume(totalVolume);
            
            // Set distribution maps (simplified for now)
            metrics.setTransactionsByBank(new HashMap<>());
            metrics.setTransactionsByClient(new HashMap<>());
            metrics.setTransactionsByCurrency(new HashMap<>());
            metrics.setTransactionsByHour(new HashMap<>());
            
            logger.debug("Transaction metrics collected: total={}, success_rate={}%", 
                        totalTransactions, String.format("%.2f", successRate));
            
        } catch (Exception e) {
            logger.error("Error collecting transaction metrics: {}", e.getMessage(), e);
        }
        
        return metrics;
    }
    
    /**
     * Collect system health metrics
     */
    public MetricsDto.SystemMetrics collectSystemMetrics() {
        MetricsDto.SystemMetrics metrics = new MetricsDto.SystemMetrics();
        
        try {
            // Memory metrics
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsage = heapMax > 0 ? (double) heapUsed / heapMax * 100 : 0.0;
            
            // Thread metrics
            int activeThreads = threadBean.getThreadCount();
            
            // CPU usage (simplified - would need more sophisticated monitoring)
            double cpuUsage = 0.0; // Placeholder - would use OperatingSystemMXBean or similar
            
            // Database connections (simplified)
            int databaseConnections = 0; // Placeholder - would get from connection pool
            
            // HTTP metrics (simplified)
            int httpActiveConnections = 0; // Placeholder - would get from web server
            double responseTimeMs = 0.0; // Placeholder
            
            // Error rate
            long totalRequests = requestCount.get();
            long errors = errorCount.get();
            int errorRate = totalRequests > 0 ? (int) (errors * 100 / totalRequests) : 0;
            
            // Set metrics
            metrics.setCpuUsage(cpuUsage);
            metrics.setMemoryUsage(memoryUsage);
            metrics.setActiveThreads(activeThreads);
            metrics.setDatabaseConnections(databaseConnections);
            metrics.setHeapMemoryUsed(heapUsed);
            metrics.setHeapMemoryMax(heapMax);
            metrics.setHttpActiveConnections(httpActiveConnections);
            metrics.setResponseTimeMs(responseTimeMs);
            metrics.setErrorRate(errorRate);
            
            // Custom metrics
            Map<String, Object> customMetrics = new HashMap<>();
            customMetrics.put("gc_collections", 0); // Placeholder
            customMetrics.put("uptime_ms", System.currentTimeMillis());
            metrics.setCustomMetrics(customMetrics);
            
            logger.debug("System metrics collected: memory={}%, threads={}, errors={}", 
                        String.format("%.2f", memoryUsage), activeThreads, errorRate);
            
        } catch (Exception e) {
            logger.error("Error collecting system metrics: {}", e.getMessage(), e);
        }
        
        return metrics;
    }
    
    /**
     * Collect bank performance metrics
     */
    public MetricsDto.BankMetrics collectBankMetrics() {
        MetricsDto.BankMetrics metrics = new MetricsDto.BankMetrics();
        
        try {
            // Get today's bank performance data
            List<com.payaza.nps.model.BankPerformanceDaily> bankPerformances = 
                bankPerformanceRepository.findByDateBetween(java.time.LocalDate.now(), java.time.LocalDate.now());
            
            Map<String, MetricsDto.BankPerformance> bankPerformanceMap = new HashMap<>();
            String worstBank = null;
            String bestBank = null;
            double worstRate = 100.0;
            double bestRate = 0.0;
            double totalSuccessRate = 0.0;
            int bankCount = 0;
            
            for (com.payaza.nps.model.BankPerformanceDaily bp : bankPerformances) {
                MetricsDto.BankPerformance performance = new MetricsDto.BankPerformance(
                    bp.getBankCode(), bp.getBankCode()); // Using bankCode as name for now
                
                performance.setTotalRequests(bp.getTotalRequests().longValue());
                performance.setSuccessfulRequests(bp.getSuccessfulRequests().longValue());
                performance.setFailedRequests(bp.getFailedRequests().longValue());
                performance.setSuccessRate(bp.getSuccessRate().doubleValue());
                performance.setTotalVolume(bp.getTotalAmount().longValue());
                
                // Set performance level based on success rate
                if (bp.getSuccessRate().doubleValue() >= 98.0) {
                    performance.setPerformanceLevel(com.payaza.nps.model.AlertSeverity.INFO);
                } else if (bp.getSuccessRate().doubleValue() >= 95.0) {
                    performance.setPerformanceLevel(com.payaza.nps.model.AlertSeverity.WARNING);
                } else {
                    performance.setPerformanceLevel(com.payaza.nps.model.AlertSeverity.CRITICAL);
                }
                
                bankPerformanceMap.put(bp.getBankCode(), performance);
                
                // Track worst and best performing banks
                double successRate = bp.getSuccessRate().doubleValue();
                if (successRate < worstRate) {
                    worstRate = successRate;
                    worstBank = bp.getBankCode();
                }
                if (successRate > bestRate) {
                    bestRate = successRate;
                    bestBank = bp.getBankCode();
                }
                
                totalSuccessRate += successRate;
                bankCount++;
            }
            
            double overallSuccessRate = bankCount > 0 ? totalSuccessRate / bankCount : 0.0;
            
            metrics.setBankPerformances(bankPerformanceMap);
            metrics.setWorstPerformingBank(worstBank);
            metrics.setBestPerformingBank(bestBank);
            metrics.setOverallBankSuccessRate(overallSuccessRate);
            metrics.setTotalBanks(bankCount);
            
            logger.debug("Bank metrics collected: {} banks, overall_success_rate={}%", 
                        bankCount, String.format("%.2f", overallSuccessRate));
            
        } catch (Exception e) {
            logger.error("Error collecting bank metrics: {}", e.getMessage(), e);
        }
        
        return metrics;
    }
    
    /**
     * Increment request counter
     */
    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }
    
    /**
     * Increment error counter
     */
    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }
    
    /**
     * Get current request count
     */
    public long getRequestCount() {
        return requestCount.get();
    }
    
    /**
     * Get current error count
     */
    public long getErrorCount() {
        return errorCount.get();
    }
    
    /**
     * Reset counters (useful for testing)
     */
    public void resetCounters() {
        requestCount.set(0);
        errorCount.set(0);
    }
}
