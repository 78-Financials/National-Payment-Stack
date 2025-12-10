package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for daily bank performance analytics
 * Pre-aggregated metrics for fast dashboard queries
 */
@Entity
@Table(name = "bank_performance_daily", 
       uniqueConstraints = @UniqueConstraint(name = "uk_bank_date", columnNames = {"bankCode", "date"}),
       indexes = {
           @Index(name = "idx_bank_perf_date", columnList = "date"),
           @Index(name = "idx_bank_perf_bank", columnList = "bankCode"),
           @Index(name = "idx_bank_perf_success_rate", columnList = "successRate"),
           @Index(name = "idx_bank_perf_avg_time", columnList = "avgProcessingTimeMs")
       })
public class BankPerformanceDaily {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Bank code is required")
    @Size(max = 10, message = "Bank code must not exceed 10 characters")
    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;
    
    @NotNull(message = "Date is required")
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    // Transaction counts
    @Column(name = "total_requests", nullable = false)
    private Integer totalRequests = 0;
    
    @Column(name = "successful_requests", nullable = false)
    private Integer successfulRequests = 0;
    
    @Column(name = "failed_requests", nullable = false)
    private Integer failedRequests = 0;
    
    @Column(name = "timeout_requests", nullable = false)
    private Integer timeoutRequests = 0;
    
    // Amount totals
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount must not be negative")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @NotNull(message = "Successful amount is required")
    @DecimalMin(value = "0.00", message = "Successful amount must not be negative")
    @Column(name = "successful_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal successfulAmount = BigDecimal.ZERO;
    
    // Performance metrics
    @Column(name = "avg_processing_time_ms", nullable = false)
    private Long avgProcessingTimeMs = 0L;
    
    @Column(name = "min_processing_time_ms")
    private Long minProcessingTimeMs;
    
    @Column(name = "max_processing_time_ms")
    private Long maxProcessingTimeMs;
    
    // Calculated metrics
    @NotNull(message = "Success rate is required")
    @DecimalMin(value = "0.00", message = "Success rate must not be negative")
    @Column(name = "success_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal successRate = BigDecimal.ZERO;
    
    @NotNull(message = "Average amount is required")
    @DecimalMin(value = "0.00", message = "Average amount must not be negative")
    @Column(name = "avg_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgAmount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public BankPerformanceDaily() {}
    
    public BankPerformanceDaily(String bankCode, String bankName, LocalDate date) {
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.date = date;
        this.totalRequests = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
        this.timeoutRequests = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.successfulAmount = BigDecimal.ZERO;
        this.avgProcessingTimeMs = 0L;
        this.successRate = BigDecimal.ZERO;
        this.avgAmount = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getTotalRequests() { return totalRequests; }
    public void setTotalRequests(Integer totalRequests) { this.totalRequests = totalRequests; }

    public Integer getSuccessfulRequests() { return successfulRequests; }
    public void setSuccessfulRequests(Integer successfulRequests) { this.successfulRequests = successfulRequests; }

    public Integer getFailedRequests() { return failedRequests; }
    public void setFailedRequests(Integer failedRequests) { this.failedRequests = failedRequests; }

    public Integer getTimeoutRequests() { return timeoutRequests; }
    public void setTimeoutRequests(Integer timeoutRequests) { this.timeoutRequests = timeoutRequests; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getSuccessfulAmount() { return successfulAmount; }
    public void setSuccessfulAmount(BigDecimal successfulAmount) { this.successfulAmount = successfulAmount; }

    public Long getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
    public void setAvgProcessingTimeMs(Long avgProcessingTimeMs) { this.avgProcessingTimeMs = avgProcessingTimeMs; }

    public Long getMinProcessingTimeMs() { return minProcessingTimeMs; }
    public void setMinProcessingTimeMs(Long minProcessingTimeMs) { this.minProcessingTimeMs = minProcessingTimeMs; }

    public Long getMaxProcessingTimeMs() { return maxProcessingTimeMs; }
    public void setMaxProcessingTimeMs(Long maxProcessingTimeMs) { this.maxProcessingTimeMs = maxProcessingTimeMs; }

    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }

    public BigDecimal getAvgAmount() { return avgAmount; }
    public void setAvgAmount(BigDecimal avgAmount) { this.avgAmount = avgAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addTransaction(PaymentTransactionHistory transaction) {
        this.totalRequests++;
        this.totalAmount = this.totalAmount.add(transaction.getAmount());
        
        if ("SUCCESS".equals(transaction.getFinalStatus())) {
            this.successfulRequests++;
            this.successfulAmount = this.successfulAmount.add(transaction.getAmount());
        } else if ("FAILED".equals(transaction.getFinalStatus())) {
            this.failedRequests++;
        } else if ("TIMEOUT".equals(transaction.getFinalStatus())) {
            this.timeoutRequests++;
        }
        
        // Update processing time metrics
        if (transaction.getProcessingTimeMs() != null) {
            if (this.minProcessingTimeMs == null || transaction.getProcessingTimeMs() < this.minProcessingTimeMs) {
                this.minProcessingTimeMs = transaction.getProcessingTimeMs();
            }
            if (this.maxProcessingTimeMs == null || transaction.getProcessingTimeMs() > this.maxProcessingTimeMs) {
                this.maxProcessingTimeMs = transaction.getProcessingTimeMs();
            }
        }
        
        // Recalculate derived metrics
        this.recalculateMetrics();
    }

    public void recalculateMetrics() {
        // Calculate success rate
        if (this.totalRequests > 0) {
            this.successRate = BigDecimal.valueOf(this.successfulRequests)
                .divide(BigDecimal.valueOf(this.totalRequests), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        } else {
            this.successRate = BigDecimal.ZERO;
        }
        
        // Calculate average amount
        if (this.totalRequests > 0) {
            this.avgAmount = this.totalAmount
                .divide(BigDecimal.valueOf(this.totalRequests), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.avgAmount = BigDecimal.ZERO;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isHighPerforming() {
        return this.successRate.compareTo(BigDecimal.valueOf(95)) >= 0;
    }

    public boolean isLowPerforming() {
        return this.successRate.compareTo(BigDecimal.valueOf(90)) < 0;
    }

    @Override
    public String toString() {
        return "BankPerformanceDaily{" +
               "id=" + id +
               ", bankCode='" + bankCode + '\'' +
               ", bankName='" + bankName + '\'' +
               ", date=" + date +
               ", totalRequests=" + totalRequests +
               ", successfulRequests=" + successfulRequests +
               ", successRate=" + successRate +
               ", avgProcessingTimeMs=" + avgProcessingTimeMs +
               '}';
    }
}
