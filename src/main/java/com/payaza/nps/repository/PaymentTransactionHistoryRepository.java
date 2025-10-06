package com.payaza.nps.repository;

import com.payaza.nps.model.PaymentTransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PaymentTransactionHistory entity
 * Optimized for historical analytics and reporting
 */
@Repository
public interface PaymentTransactionHistoryRepository extends JpaRepository<PaymentTransactionHistory, Long> {
    
    /**
     * Find transactions by client ID with pagination
     */
    Page<PaymentTransactionHistory> findByClientId(String clientId, Pageable pageable);
    
    /**
     * Find transactions by final status with pagination
     */
    Page<PaymentTransactionHistory> findByFinalStatus(String finalStatus, Pageable pageable);
    
    /**
     * Find transactions by client ID and final status
     */
    Page<PaymentTransactionHistory> findByClientIdAndFinalStatus(String clientId, String finalStatus, Pageable pageable);
    
    /**
     * Find transactions created between two dates
     */
    Page<PaymentTransactionHistory> findByRequestCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Find transactions by creditor bank
     */
    Page<PaymentTransactionHistory> findByCreditorBank(String creditorBank, Pageable pageable);
    
    /**
     * Find transactions by debtor bank
     */
    Page<PaymentTransactionHistory> findByDebtorBank(String debtorBank, Pageable pageable);
    
    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM PaymentTransactionHistory t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.requestCreatedAt DESC")
    Page<PaymentTransactionHistory> findByAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                                      @Param("maxAmount") java.math.BigDecimal maxAmount, 
                                                      Pageable pageable);
    
    /**
     * Get daily transaction counts by status
     */
    @Query("SELECT DATE(t.requestCreatedAt), t.finalStatus, COUNT(t) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY DATE(t.requestCreatedAt), t.finalStatus ORDER BY DATE(t.requestCreatedAt) DESC")
    List<Object[]> getDailyTransactionCountsByStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get monthly transaction volume
     */
    @Query("SELECT YEAR(t.requestCreatedAt), MONTH(t.requestCreatedAt), COUNT(t), SUM(t.amount) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY YEAR(t.requestCreatedAt), MONTH(t.requestCreatedAt) ORDER BY YEAR(t.requestCreatedAt) DESC, MONTH(t.requestCreatedAt) DESC")
    List<Object[]> getMonthlyTransactionVolume(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get bank performance summary for date range
     */
    @Query("SELECT t.creditorBank, COUNT(t), SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END), AVG(t.processingTimeMs), SUM(t.amount) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY t.creditorBank ORDER BY COUNT(t) DESC")
    List<Object[]> getBankPerformanceSummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get client activity summary for date range
     */
    @Query("SELECT t.clientId, COUNT(t), SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END), SUM(t.amount), AVG(t.processingTimeMs) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY t.clientId ORDER BY COUNT(t) DESC")
    List<Object[]> getClientActivitySummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get success rate by bank for date range
     */
    @Query("SELECT t.creditorBank, COUNT(t) as total, SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END) as successful, (SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(t)) as successRate FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY t.creditorBank HAVING COUNT(t) >= :minTransactions ORDER BY successRate DESC")
    List<Object[]> getBankSuccessRates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("minTransactions") long minTransactions);
    
    /**
     * Get average processing time by bank
     */
    @Query("SELECT t.creditorBank, AVG(t.processingTimeMs) as avgTime, MIN(t.processingTimeMs) as minTime, MAX(t.processingTimeMs) as maxTime FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end AND t.processingTimeMs IS NOT NULL GROUP BY t.creditorBank ORDER BY avgTime ASC")
    List<Object[]> getBankProcessingTimes(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get transaction volume by hour of day
     */
    @Query("SELECT HOUR(t.requestCreatedAt), COUNT(t), AVG(t.processingTimeMs) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY HOUR(t.requestCreatedAt) ORDER BY HOUR(t.requestCreatedAt)")
    List<Object[]> getHourlyTransactionPatterns(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get transaction volume by day of week
     */
    @Query("SELECT DAYOFWEEK(t.requestCreatedAt), COUNT(t), SUM(t.amount) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY DAYOFWEEK(t.requestCreatedAt) ORDER BY DAYOFWEEK(t.requestCreatedAt)")
    List<Object[]> getWeeklyTransactionPatterns(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get error analysis for date range
     */
    @Query("SELECT t.errorCategory, COUNT(t), t.finalStatus FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end AND t.finalStatus = 'FAILED' GROUP BY t.errorCategory, t.finalStatus ORDER BY COUNT(t) DESC")
    List<Object[]> getErrorAnalysis(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get top performing banks by success rate and volume
     */
    @Query("SELECT t.creditorBank, COUNT(t) as volume, SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END) as successful, (SUM(CASE WHEN t.finalStatus = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(t)) as successRate, AVG(t.processingTimeMs) as avgTime FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY t.creditorBank HAVING COUNT(t) >= :minVolume ORDER BY successRate DESC, volume DESC")
    List<Object[]> getTopPerformingBanks(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("minVolume") long minVolume);
    
    /**
     * Get currency distribution
     */
    @Query("SELECT t.currency, COUNT(t), SUM(t.amount) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end GROUP BY t.currency ORDER BY COUNT(t) DESC")
    List<Object[]> getCurrencyDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get amount distribution (by ranges)
     */
    @Query("SELECT CASE " +
           "WHEN t.amount < 1000 THEN '< 1K' " +
           "WHEN t.amount < 10000 THEN '1K-10K' " +
           "WHEN t.amount < 100000 THEN '10K-100K' " +
           "WHEN t.amount < 1000000 THEN '100K-1M' " +
           "ELSE '> 1M' END as amountRange, " +
           "COUNT(t), SUM(t.amount) " +
           "FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end " +
           "GROUP BY amountRange ORDER BY COUNT(t) DESC")
    List<Object[]> getAmountDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Count transactions by date range and status
     */
    long countByRequestCreatedAtBetweenAndFinalStatus(LocalDateTime start, LocalDateTime end, String finalStatus);
    
    /**
     * Count total transactions by date range
     */
    long countByRequestCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Get total amount by date range
     */
    @Query("SELECT SUM(t.amount) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end")
    java.math.BigDecimal getTotalAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get average processing time by date range
     */
    @Query("SELECT AVG(t.processingTimeMs) FROM PaymentTransactionHistory t WHERE t.requestCreatedAt BETWEEN :start AND :end AND t.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTimeByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Delete old historical records (for cleanup)
     */
    void deleteByRequestCreatedAtBefore(LocalDateTime cutoffDate);
}
