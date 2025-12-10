package com.payaza.nps.service;

import com.payaza.nps.model.AuditLog;
import com.payaza.nps.model.BankPerformanceDaily;
import com.payaza.nps.model.PaymentTransactionHistory;
import com.payaza.nps.model.PaymentTransactionLive;
import com.payaza.nps.repository.BankPerformanceDailyRepository;
import com.payaza.nps.repository.PaymentTransactionHistoryRepository;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * End-of-Day Processing Service
 * Handles archival of completed transactions and bank performance aggregation
 * Uses distributed locking to prevent multiple pods from running simultaneously
 */
@Service
public class EndOfDayProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EndOfDayProcessingService.class);
    private static final String EOD_LOCK_NAME = "EOD_PROCESSING_LOCK";
    private static final int EOD_LOCK_DURATION_MINUTES = 120; // 2 hours
    private static final int EOD_LOCK_TIMEOUT_SECONDS = 30;
    
    @Autowired
    private DistributedLockService distributedLockService;
    
    @Autowired
    private PaymentTransactionLiveRepository liveRepository;
    
    @Autowired
    private PaymentTransactionHistoryRepository historyRepository;
    
    @Autowired
    private BankPerformanceDailyRepository bankPerformanceRepository;
    
    @Autowired
    private AuditService auditService;

    /**
     * Scheduled EOD processing - runs every hour to check for EOD operations
     * Only one pod in the cluster will execute this due to distributed locking
     */
    @Scheduled(fixedDelay = 3600000) // Every hour
    public void scheduledEodProcessing() {
        logger.info("Starting scheduled EOD processing check");
        
        distributedLockService.executeWithLock(
            EOD_LOCK_NAME,
            EOD_LOCK_DURATION_MINUTES,
            EOD_LOCK_TIMEOUT_SECONDS,
            () -> {
                try {
                    // Check if we need to run EOD processing
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    
                    // Check if yesterday's data needs processing
                    if (shouldRunEodProcessing(yesterday)) {
                        logger.info("Running EOD processing for date: {}", yesterday);
                        processEndOfDay(yesterday);
                    } else {
                        logger.info("EOD processing not needed for date: {}", yesterday);
                    }
                    
                    // Clean up old live transactions (older than 2 days)
                    cleanupOldLiveTransactions();
                    
                    return "EOD processing completed successfully";
                    
                } catch (Exception e) {
                    logger.error("Error in scheduled EOD processing: {}", e.getMessage(), e);
                    throw new RuntimeException("EOD processing failed", e);
                }
            }
        );
    }

    /**
     * Manual EOD processing trigger
     */
    public String triggerEodProcessing(LocalDate targetDate) {
        logger.info("Manual EOD processing triggered for date: {}", targetDate);
        
        return distributedLockService.executeWithLock(
            EOD_LOCK_NAME,
            EOD_LOCK_DURATION_MINUTES,
            EOD_LOCK_TIMEOUT_SECONDS,
            () -> {
                try {
                    processEndOfDay(targetDate);
                    return String.format("EOD processing completed for date: %s", targetDate);
                } catch (Exception e) {
                    logger.error("Error in manual EOD processing: {}", e.getMessage(), e);
                    throw new RuntimeException("Manual EOD processing failed", e);
                }
            }
        ).orElse("Failed to acquire lock for EOD processing");
    }

    /**
     * Check if EOD processing should run for a specific date
     */
    private boolean shouldRunEodProcessing(LocalDate targetDate) {
        try {
            // Check if bank performance data already exists for this date
            long existingPerformanceCount = bankPerformanceRepository.countByDate(targetDate);
            
            if (existingPerformanceCount > 0) {
                logger.info("Bank performance data already exists for date: {}, skipping EOD", targetDate);
                return false;
            }
            
            // Check if there are transactions to process
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
            
            List<PaymentTransactionLive> transactions = liveRepository.findByRequestCreatedAtBetween(startOfDay, endOfDay);
            long transactionCount = transactions.size();
            
            if (transactionCount == 0) {
                logger.info("No transactions found for date: {}, skipping EOD", targetDate);
                return false;
            }
            
            logger.info("Found {} transactions for EOD processing on date: {}", transactionCount, targetDate);
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking EOD processing requirements: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Process end-of-day operations for a specific date
     */
    @Transactional
    public void processEndOfDay(LocalDate targetDate) {
        logger.info("Starting EOD processing for date: {}", targetDate);
        
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
        
        try {
            // Step 1: Archive completed transactions to history
            int archivedCount = archiveCompletedTransactions(startOfDay, endOfDay);
            
            // Step 2: Generate bank performance metrics
            int performanceRecordsCount = generateBankPerformanceMetrics(targetDate, startOfDay, endOfDay);
            
            // Step 3: Clean up old live transactions for this date
            int cleanedCount = cleanupTransactionsForDate(startOfDay, endOfDay);
            
            // Log success
            auditService.logSystemEvent(
                "EOD_PROCESSING_COMPLETED",
                "EndOfDayProcessing",
                String.format("EOD processing completed for date: %s", targetDate),
                Map.of(
                    "targetDate", targetDate.toString(),
                    "archivedTransactions", archivedCount,
                    "performanceRecordsCreated", performanceRecordsCount,
                    "cleanedTransactions", cleanedCount,
                    "podId", distributedLockService.getPodId()
                )
            );
            
            logger.info("EOD processing completed for date: {} - Archived: {}, Performance records: {}, Cleaned: {}", 
                       targetDate, archivedCount, performanceRecordsCount, cleanedCount);
            
        } catch (Exception e) {
            logger.error("Error in EOD processing for date {}: {}", targetDate, e.getMessage(), e);
            
            auditService.logError(
                "EOD_PROCESSING_FAILED",
                "EndOfDayProcessing",
                AuditLog.ActionType.SYSTEM_EVENT,
                null,
                distributedLockService.getPodId(),
                "EOD processing failed for date: " + targetDate,
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("targetDate", targetDate.toString())
            );
            
            throw e;
        }
    }

    /**
     * Archive completed transactions to history table
     */
    @Transactional
    public int archiveCompletedTransactions(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        logger.info("Archiving completed transactions from {} to {}", startOfDay, endOfDay);
        
        AtomicInteger archivedCount = new AtomicInteger(0);
        
        try {
            // Get completed transactions for the date range
            List<PaymentTransactionLive> completedTransactions = liveRepository
                .findTransactionsForEodProcessing(endOfDay);
            
            logger.info("Found {} completed transactions to archive", completedTransactions.size());
            
            // Process in batches to avoid memory issues
            int batchSize = 1000;
            for (int i = 0; i < completedTransactions.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, completedTransactions.size());
                List<PaymentTransactionLive> batch = completedTransactions.subList(i, endIndex);
                
                // Convert to history records
                List<PaymentTransactionHistory> historyRecords = batch.stream()
                    .map(PaymentTransactionHistory::new)
                    .toList();
                
                // Save to history table
                historyRepository.saveAll(historyRecords);
                
                // Delete from live table
                liveRepository.deleteAll(batch);
                
                archivedCount.addAndGet(batch.size());
                
                logger.info("Archived batch {}-{} of {} transactions", i + 1, endIndex, completedTransactions.size());
            }
            
            logger.info("Successfully archived {} transactions", archivedCount.get());
            return archivedCount.get();
            
        } catch (Exception e) {
            logger.error("Error archiving transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to archive transactions", e);
        }
    }

    /**
     * Generate bank performance metrics for a specific date
     */
    @Transactional
    public int generateBankPerformanceMetrics(LocalDate targetDate, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        logger.info("Generating bank performance metrics for date: {}", targetDate);
        
        try {
            // Get historical transactions for this date
            List<PaymentTransactionHistory> dailyTransactions = historyRepository
                .findByRequestCreatedAtBetween(startOfDay, endOfDay, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
            
            if (dailyTransactions.isEmpty()) {
                logger.info("No historical transactions found for date: {}", targetDate);
                return 0;
            }
            
            // Group by creditor bank
            Map<String, List<PaymentTransactionHistory>> transactionsByBank = dailyTransactions.stream()
                .filter(t -> t.getCreditorBank() != null && !t.getCreditorBank().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(PaymentTransactionHistory::getCreditorBank));
            
            AtomicInteger recordsCreated = new AtomicInteger(0);
            
            // Create performance records for each bank
            transactionsByBank.forEach((bankCode, bankTransactions) -> {
                try {
                    BankPerformanceDaily performance = createBankPerformanceRecord(
                        targetDate, bankCode, bankTransactions
                    );
                    
                    // Save or update performance record
                    bankPerformanceRepository.save(performance);
                    recordsCreated.incrementAndGet();
                    
                } catch (Exception e) {
                    logger.error("Error creating performance record for bank {}: {}", bankCode, e.getMessage());
                }
            });
            
            logger.info("Created {} bank performance records for date: {}", recordsCreated.get(), targetDate);
            return recordsCreated.get();
            
        } catch (Exception e) {
            logger.error("Error generating bank performance metrics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate bank performance metrics", e);
        }
    }

    /**
     * Create a bank performance record from transaction data
     */
    private BankPerformanceDaily createBankPerformanceRecord(
            LocalDate targetDate, String bankCode, List<PaymentTransactionHistory> transactions) {
        
        BankPerformanceDaily performance = new BankPerformanceDaily(bankCode, getBankName(bankCode), targetDate);
        
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        AtomicInteger timeoutRequests = new AtomicInteger(0);
        AtomicLong totalAmount = new AtomicLong(0);
        AtomicLong successfulAmount = new AtomicLong(0);
        AtomicLong totalProcessingTime = new AtomicLong(0);
        AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxProcessingTime = new AtomicLong(0);
        
        // Process each transaction
        transactions.forEach(transaction -> {
            totalRequests.incrementAndGet();
            totalAmount.addAndGet(transaction.getAmount().multiply(new BigDecimal("100")).longValue()); // Convert to cents
            
            if ("SUCCESS".equals(transaction.getFinalStatus())) {
                successfulRequests.incrementAndGet();
                successfulAmount.addAndGet(transaction.getAmount().multiply(new BigDecimal("100")).longValue());
            } else if ("FAILED".equals(transaction.getFinalStatus())) {
                failedRequests.incrementAndGet();
            } else if ("TIMEOUT".equals(transaction.getFinalStatus())) {
                timeoutRequests.incrementAndGet();
            }
            
            if (transaction.getProcessingTimeMs() != null) {
                totalProcessingTime.addAndGet(transaction.getProcessingTimeMs());
                minProcessingTime.set(Math.min(minProcessingTime.get(), transaction.getProcessingTimeMs()));
                maxProcessingTime.set(Math.max(maxProcessingTime.get(), transaction.getProcessingTimeMs()));
            }
        });
        
        // Set calculated values
        performance.setTotalRequests(totalRequests.get());
        performance.setSuccessfulRequests(successfulRequests.get());
        performance.setFailedRequests(failedRequests.get());
        performance.setTimeoutRequests(timeoutRequests.get());
        performance.setTotalAmount(new BigDecimal(totalAmount.get()).divide(new BigDecimal("100")));
        performance.setSuccessfulAmount(new BigDecimal(successfulAmount.get()).divide(new BigDecimal("100")));
        
        if (totalRequests.get() > 0) {
            performance.setAvgProcessingTimeMs(totalProcessingTime.get() / totalRequests.get());
            performance.setMinProcessingTimeMs(minProcessingTime.get() == Long.MAX_VALUE ? null : minProcessingTime.get());
            performance.setMaxProcessingTimeMs(maxProcessingTime.get() == 0 ? null : maxProcessingTime.get());
        }
        
        // Recalculate derived metrics
        performance.recalculateMetrics();
        
        return performance;
    }

    /**
     * Get bank name from bank code (could be enhanced with a bank lookup service)
     */
    private String getBankName(String bankCode) {
        // This could be enhanced with a proper bank lookup service
        Map<String, String> bankNames = Map.of(
            "058", "GTBank",
            "011", "First Bank",
            "044", "Access Bank",
            "033", "UBA",
            "032", "Union Bank",
            "050", "Ecobank",
            "057", "Zenith Bank",
            "214", "First City Monument Bank",
            "070", "Fidelity Bank",
            "030", "Heritage Bank"
        );
        
        return bankNames.getOrDefault(bankCode, "Bank " + bankCode);
    }

    /**
     * Clean up old live transactions
     */
    @Transactional
    public int cleanupOldLiveTransactions() {
        logger.info("Cleaning up old live transactions");
        
        try {
            // Delete transactions older than 2 days
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(2);
            List<PaymentTransactionLive> oldTransactions = liveRepository
                .findByRequestCreatedAtBetween(LocalDateTime.of(2020, 1, 1, 0, 0), cutoffDate);
            
            if (!oldTransactions.isEmpty()) {
                liveRepository.deleteAll(oldTransactions);
                logger.info("Cleaned up {} old live transactions", oldTransactions.size());
                return oldTransactions.size();
            }
            
            return 0;
            
        } catch (Exception e) {
            logger.error("Error cleaning up old live transactions: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Clean up transactions for a specific date range
     */
    @Transactional
    public int cleanupTransactionsForDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        logger.info("Cleaning up transactions for date range: {} to {}", startOfDay, endOfDay);
        
        try {
            // Delete any remaining live transactions for this date
            List<PaymentTransactionLive> remainingTransactions = liveRepository
                .findByRequestCreatedAtBetween(startOfDay, endOfDay);
            
            if (!remainingTransactions.isEmpty()) {
                liveRepository.deleteAll(remainingTransactions);
                logger.info("Cleaned up {} remaining transactions for date range", remainingTransactions.size());
                return remainingTransactions.size();
            }
            
            return 0;
            
        } catch (Exception e) {
            logger.error("Error cleaning up transactions for date range: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get EOD processing status
     */
    public Map<String, Object> getEodProcessingStatus() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            boolean hasPerformanceData = bankPerformanceRepository.countByDate(yesterday) > 0;
            
            return Map.of(
                "lastProcessedDate", yesterday,
                "hasPerformanceData", hasPerformanceData,
                "isLocked", distributedLockService.holdsLock(EOD_LOCK_NAME),
                "podId", distributedLockService.getPodId(),
                "timestamp", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error getting EOD processing status: {}", e.getMessage(), e);
            return Map.of("error", "Failed to get EOD processing status");
        }
    }
}
