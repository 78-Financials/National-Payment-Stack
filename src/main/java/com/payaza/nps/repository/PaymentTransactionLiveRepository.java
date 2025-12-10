package com.payaza.nps.repository;

import com.payaza.nps.model.PaymentTransactionLive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentTransactionLive entity
 * Optimized for real-time queries and dashboard performance
 */
@Repository
public interface PaymentTransactionLiveRepository extends JpaRepository<PaymentTransactionLive, Long> {
    
    /**
     * Find transaction by original message ID (for PACS.002 callback matching)
     */
    Optional<PaymentTransactionLive> findByOriginalMessageId(String originalMessageId);
    
    /**
     * Find transaction by transaction ID
     */
    Optional<PaymentTransactionLive> findByTransactionId(String transactionId);
    
    /**
     * Find transactions by client ID with pagination
     */
    Page<PaymentTransactionLive> findByClientId(String clientId, Pageable pageable);
    
    /**
     * Find transactions by status with pagination
     */
    Page<PaymentTransactionLive> findByStatus(String status, Pageable pageable);
    
    /**
     * Find transactions by client ID and status
     */
    Page<PaymentTransactionLive> findByClientIdAndStatus(String clientId, String status, Pageable pageable);
    
    /**
     * Find transactions created after a specific time
     */
    List<PaymentTransactionLive> findByRequestCreatedAtAfter(LocalDateTime after);
    
    /**
     * Find transactions created between two times
     */
    List<PaymentTransactionLive> findByRequestCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Find recent transactions (for live dashboard)
     */
    List<PaymentTransactionLive> findTop20ByOrderByRequestCreatedAtDesc();
    
    /**
     * Find recent transactions by client
     */
    List<PaymentTransactionLive> findTop10ByClientIdOrderByRequestCreatedAtDesc(String clientId);
    
    /**
     * Find pending transactions older than specified time (for timeout detection)
     */
    List<PaymentTransactionLive> findByStatusAndRequestCreatedAtBefore(String status, LocalDateTime before);
    
    /**
     * Find transactions by creditor bank
     */
    List<PaymentTransactionLive> findByCreditorBank(String creditorBank);
    
    /**
     * Find transactions by debtor bank
     */
    List<PaymentTransactionLive> findByDebtorBank(String debtorBank);
    
    /**
     * Count transactions by status since a specific time
     */
    long countByStatusAndRequestCreatedAtAfter(String status, LocalDateTime after);
    
    /**
     * Count transactions by client since a specific time
     */
    long countByClientIdAndRequestCreatedAtAfter(String clientId, LocalDateTime after);
    
    /**
     * Count total transactions since a specific time
     */
    long countByRequestCreatedAtAfter(LocalDateTime after);
    
    /**
     * Get average processing time since a specific time
     */
    @Query("SELECT AVG(t.processingTimeMs) FROM PaymentTransactionLive t WHERE t.processingTimeMs IS NOT NULL AND t.requestCreatedAt > :since")
    Double getAverageProcessingTimeSince(@Param("since") LocalDateTime since);
    
    /**
     * Get transactions for EOD processing (completed or older than 1 day)
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.status IN ('SUCCESS', 'FAILED', 'TIMEOUT') OR t.requestCreatedAt < :cutoffDate")
    List<PaymentTransactionLive> findTransactionsForEodProcessing(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get transactions by amount range
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.requestCreatedAt DESC")
    Page<PaymentTransactionLive> findByAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                   @Param("maxAmount") java.math.BigDecimal maxAmount, 
                                                   Pageable pageable);
    
    /**
     * Get high-value transactions (above threshold)
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.amount >= :threshold ORDER BY t.amount DESC")
    List<PaymentTransactionLive> findHighValueTransactions(@Param("threshold") java.math.BigDecimal threshold);
    
    /**
     * Get transactions with errors
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.status = 'FAILED' ORDER BY t.requestCreatedAt DESC")
    Page<PaymentTransactionLive> findFailedTransactions(Pageable pageable);
    
    /**
     * Get slow transactions (above threshold processing time)
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.processingTimeMs > :threshold ORDER BY t.processingTimeMs DESC")
    List<PaymentTransactionLive> findSlowTransactions(@Param("threshold") Long thresholdMs);
    
    /**
     * Get transactions by time range with status filter
     */
    @Query("SELECT t FROM PaymentTransactionLive t WHERE t.requestCreatedAt BETWEEN :start AND :end AND t.status = :status ORDER BY t.requestCreatedAt DESC")
    List<PaymentTransactionLive> findByTimeRangeAndStatus(@Param("start") LocalDateTime start, 
                                                         @Param("end") LocalDateTime end, 
                                                         @Param("status") String status);
    
    /**
     * Get daily transaction summary for a client
     */
    @Query("SELECT COUNT(t), SUM(t.amount), AVG(t.processingTimeMs) FROM PaymentTransactionLive t WHERE t.clientId = :clientId AND DATE(t.requestCreatedAt) = DATE(:date)")
    Object[] getDailyClientSummary(@Param("clientId") String clientId, @Param("date") LocalDateTime date);
    
    /**
     * Get bank performance summary for today
     */
    @Query("SELECT t.creditorBank, COUNT(t), SUM(CASE WHEN t.status = 'SUCCESS' THEN 1 ELSE 0 END), AVG(t.processingTimeMs) FROM PaymentTransactionLive t WHERE DATE(t.requestCreatedAt) = DATE(:today) GROUP BY t.creditorBank ORDER BY COUNT(t) DESC")
    List<Object[]> getTodayBankPerformanceSummary(@Param("today") LocalDateTime today);
    
    /**
     * Delete transactions older than specified date
     */
    void deleteByRequestCreatedAtBefore(LocalDateTime cutoffDate);
    
    /**
     * Delete transactions by status older than specified date
     */
    void deleteByStatusAndRequestCreatedAtBefore(String status, LocalDateTime cutoffDate);
}
