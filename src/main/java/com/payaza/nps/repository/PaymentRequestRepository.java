package com.payaza.nps.repository;

import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentRequest entity
 */
@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    /**
     * Find payment request by payment ID
     */
    Optional<PaymentRequest> findByPaymentId(String paymentId);

    /**
     * Find payment request by transaction ID
     */
    Optional<PaymentRequest> findByTransactionId(String transactionId);

    /**
     * Find payment request by NPS reference
     */
    Optional<PaymentRequest> findByNpsReference(String npsReference);

    /**
     * Find payments by status
     */
    List<PaymentRequest> findByStatus(PaymentStatus status);

    /**
     * Find payments by status and date range
     */
    @Query("SELECT p FROM PaymentRequest p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentRequest> findByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find pending payments older than specified minutes
     */
    @Query("SELECT p FROM PaymentRequest p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<PaymentRequest> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Find payments by sender account
     */
    List<PaymentRequest> findBySenderAccount(String senderAccount);

    /**
     * Find payments by receiver account
     */
    List<PaymentRequest> findByReceiverAccount(String receiverAccount);

    /**
     * Check if payment ID exists
     */
    boolean existsByPaymentId(String paymentId);

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);
}
