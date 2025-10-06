package com.payaza.nps.repository;

import com.payaza.nps.model.BankPerformanceDaily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BankPerformanceDaily entity
 * Optimized for bank performance analytics and reporting
 */
@Repository
public interface BankPerformanceDailyRepository extends JpaRepository<BankPerformanceDaily, Long> {
    
    /**
     * Find bank performance by bank code and date
     */
    Optional<BankPerformanceDaily> findByBankCodeAndDate(String bankCode, LocalDate date);
    
    /**
     * Find bank performance by bank code with pagination
     */
    Page<BankPerformanceDaily> findByBankCode(String bankCode, Pageable pageable);
    
    /**
     * Find bank performance by date with pagination
     */
    Page<BankPerformanceDaily> findByDate(LocalDate date, Pageable pageable);
    
    /**
     * Find bank performance between date range
     */
    List<BankPerformanceDaily> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find bank performance by date range with pagination
     */
    Page<BankPerformanceDaily> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find bank performance by bank code and date range
     */
    List<BankPerformanceDaily> findByBankCodeAndDateBetween(String bankCode, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find top performing banks by success rate for a specific date
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date = :date AND b.totalRequests >= :minTransactions ORDER BY b.successRate DESC")
    List<BankPerformanceDaily> findTopPerformingBanksByDate(@Param("date") LocalDate date, @Param("minTransactions") int minTransactions);
    
    /**
     * Find top performing banks by success rate for date range
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate AND b.totalRequests >= :minTransactions ORDER BY b.successRate DESC")
    List<BankPerformanceDaily> findTopPerformingBanksByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("minTransactions") int minTransactions);
    
    /**
     * Find banks with highest transaction volume for a specific date
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date = :date ORDER BY b.totalRequests DESC")
    List<BankPerformanceDaily> findBanksByVolumeForDate(@Param("date") LocalDate date);
    
    /**
     * Find banks with highest transaction volume for date range
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate ORDER BY b.totalRequests DESC")
    List<BankPerformanceDaily> findBanksByVolumeForDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find fastest banks by average processing time for a specific date
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date = :date AND b.totalRequests >= :minTransactions AND b.avgProcessingTimeMs > 0 ORDER BY b.avgProcessingTimeMs ASC")
    List<BankPerformanceDaily> findFastestBanksByDate(@Param("date") LocalDate date, @Param("minTransactions") int minTransactions);
    
    /**
     * Find fastest banks by average processing time for date range
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate AND b.totalRequests >= :minTransactions AND b.avgProcessingTimeMs > 0 ORDER BY b.avgProcessingTimeMs ASC")
    List<BankPerformanceDaily> findFastestBanksByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("minTransactions") int minTransactions);
    
    /**
     * Get bank performance summary for date range
     */
    @Query("SELECT b.bankCode, b.bankName, SUM(b.totalRequests) as totalRequests, SUM(b.successfulRequests) as successfulRequests, " +
           "AVG(b.successRate) as avgSuccessRate, AVG(b.avgProcessingTimeMs) as avgProcessingTime, SUM(b.totalAmount) as totalAmount " +
           "FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate " +
           "GROUP BY b.bankCode, b.bankName ORDER BY totalRequests DESC")
    List<Object[]> getBankPerformanceSummary(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily volume trends for a specific bank
     */
    @Query("SELECT b.date, b.totalRequests, b.successfulRequests, b.successRate FROM BankPerformanceDaily b WHERE b.bankCode = :bankCode AND b.date BETWEEN :startDate AND :endDate ORDER BY b.date ASC")
    List<Object[]> getDailyVolumeTrendsForBank(@Param("bankCode") String bankCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily performance trends for all banks
     */
    @Query("SELECT b.date, SUM(b.totalRequests) as totalRequests, SUM(b.successfulRequests) as successfulRequests, " +
           "AVG(b.successRate) as avgSuccessRate, AVG(b.avgProcessingTimeMs) as avgProcessingTime " +
           "FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate " +
           "GROUP BY b.date ORDER BY b.date ASC")
    List<Object[]> getDailyPerformanceTrends(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly performance summary
     */
    @Query("SELECT YEAR(b.date), MONTH(b.date), SUM(b.totalRequests), SUM(b.successfulRequests), AVG(b.successRate), AVG(b.avgProcessingTimeMs) " +
           "FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(b.date), MONTH(b.date) ORDER BY YEAR(b.date) DESC, MONTH(b.date) DESC")
    List<Object[]> getMonthlyPerformanceSummary(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find banks with declining performance (success rate trending down)
     */
    @Query("SELECT b1.bankCode, b1.bankName, b1.successRate as recentRate, b2.successRate as previousRate " +
           "FROM BankPerformanceDaily b1, BankPerformanceDaily b2 " +
           "WHERE b1.bankCode = b2.bankCode " +
           "AND b1.date = :recentDate AND b2.date = :previousDate " +
           "AND b1.totalRequests >= :minTransactions AND b2.totalRequests >= :minTransactions " +
           "AND b1.successRate < b2.successRate " +
           "ORDER BY (b2.successRate - b1.successRate) DESC")
    List<Object[]> findBanksWithDecliningPerformance(@Param("recentDate") LocalDate recentDate, @Param("previousDate") LocalDate previousDate, @Param("minTransactions") int minTransactions);
    
    /**
     * Find banks with improving performance (success rate trending up)
     */
    @Query("SELECT b1.bankCode, b1.bankName, b1.successRate as recentRate, b2.successRate as previousRate " +
           "FROM BankPerformanceDaily b1, BankPerformanceDaily b2 " +
           "WHERE b1.bankCode = b2.bankCode " +
           "AND b1.date = :recentDate AND b2.date = :previousDate " +
           "AND b1.totalRequests >= :minTransactions AND b2.totalRequests >= :minTransactions " +
           "AND b1.successRate > b2.successRate " +
           "ORDER BY (b1.successRate - b2.successRate) DESC")
    List<Object[]> findBanksWithImprovingPerformance(@Param("recentDate") LocalDate recentDate, @Param("previousDate") LocalDate previousDate, @Param("minTransactions") int minTransactions);
    
    /**
     * Get bank reliability score (combination of success rate and consistency)
     */
    @Query("SELECT b.bankCode, b.bankName, AVG(b.successRate) as avgSuccessRate, " +
           "STDDEV(b.successRate) as successRateStdDev, COUNT(b) as daysWithData " +
           "FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate " +
           "GROUP BY b.bankCode, b.bankName " +
           "HAVING COUNT(b) >= :minDays " +
           "ORDER BY avgSuccessRate DESC, successRateStdDev ASC")
    List<Object[]> getBankReliabilityScores(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("minDays") int minDays);
    
    /**
     * Find banks with high error rates
     */
    @Query("SELECT b FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate AND b.successRate < :threshold ORDER BY b.successRate ASC")
    List<BankPerformanceDaily> findBanksWithHighErrorRates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("threshold") java.math.BigDecimal threshold);
    
    /**
     * Get total transaction volume by date
     */
    @Query("SELECT b.date, SUM(b.totalRequests), SUM(b.successfulRequests), SUM(b.totalAmount) FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate GROUP BY b.date ORDER BY b.date ASC")
    List<Object[]> getDailyTransactionVolumes(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get average daily metrics for all banks
     */
    @Query("SELECT AVG(b.totalRequests), AVG(b.successfulRequests), AVG(b.successRate), AVG(b.avgProcessingTimeMs), AVG(b.totalAmount) FROM BankPerformanceDaily b WHERE b.date BETWEEN :startDate AND :endDate")
    Object[] getAverageDailyMetrics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Count banks with data for a specific date
     */
    long countByDate(LocalDate date);
    
    /**
     * Count total records for date range
     */
    long countByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Delete old performance records (for cleanup)
     */
    void deleteByDateBefore(LocalDate cutoffDate);
}
