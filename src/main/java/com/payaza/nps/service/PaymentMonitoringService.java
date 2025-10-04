package com.payaza.nps.service;

import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import com.payaza.nps.repository.PaymentRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for monitoring payment operations and system health
 */
@Service
public class PaymentMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMonitoringService.class);

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    /**
     * Monitor pending payments and handle timeouts
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void monitorPendingPayments() {
        logger.info("Starting payment monitoring task");

        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
            List<PaymentRequest> pendingPayments = paymentRequestRepository.findPendingPaymentsOlderThan(cutoffTime);

            if (!pendingPayments.isEmpty()) {
                logger.warn("Found {} pending payments older than 30 minutes", pendingPayments.size());

                for (PaymentRequest payment : pendingPayments) {
                    // Update status to timeout
                    payment.setStatus(PaymentStatus.TIMEOUT);
                    payment.setErrorMessage("Payment timeout - no response from NPS");
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRequestRepository.save(payment);

                    logger.warn("Marked payment {} as timeout", payment.getPaymentId());
                }
            }

        } catch (Exception e) {
            logger.error("Error during payment monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate payment statistics
     */
    public PaymentStatistics generateStatistics() {
        logger.info("Generating payment statistics");

        PaymentStatistics stats = new PaymentStatistics();
        stats.setTotalPayments(paymentRequestRepository.count());
        stats.setSuccessfulPayments(paymentRequestRepository.countByStatus(PaymentStatus.SUCCESS));
        stats.setFailedPayments(paymentRequestRepository.countByStatus(PaymentStatus.FAILED));
        stats.setPendingPayments(paymentRequestRepository.countByStatus(PaymentStatus.PENDING));
        stats.setProcessingPayments(paymentRequestRepository.countByStatus(PaymentStatus.PROCESSING));

        return stats;
    }

    /**
     * Check system health
     */
    public SystemHealth checkSystemHealth() {
        logger.info("Checking system health");

        SystemHealth health = new SystemHealth();
        health.setStatus("UP");
        health.setTimestamp(LocalDateTime.now());

        try {
            // Check database connectivity
            long totalPayments = paymentRequestRepository.count();
            health.setDatabaseStatus("UP");
            health.setTotalPayments(totalPayments);

            // Check for any critical issues
            long failedPayments = paymentRequestRepository.countByStatus(PaymentStatus.FAILED);
            if (failedPayments > 100) { // Threshold for critical alerts
                health.setStatus("DEGRADED");
                health.setMessage("High number of failed payments detected");
            }

        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            health.setStatus("DOWN");
            health.setMessage("Database connectivity issue");
            health.setDatabaseStatus("DOWN");
        }

        return health;
    }

    /**
     * Payment statistics data class
     */
    public static class PaymentStatistics {
        private long totalPayments;
        private long successfulPayments;
        private long failedPayments;
        private long pendingPayments;
        private long processingPayments;

        // Getters and Setters
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

        public long getSuccessfulPayments() { return successfulPayments; }
        public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }

        public long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }

        public long getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }

        public long getProcessingPayments() { return processingPayments; }
        public void setProcessingPayments(long processingPayments) { this.processingPayments = processingPayments; }
    }

    /**
     * System health data class
     */
    public static class SystemHealth {
        private String status;
        private String message;
        private String databaseStatus;
        private LocalDateTime timestamp;
        private long totalPayments;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getDatabaseStatus() { return databaseStatus; }
        public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
    }
}
