package com.payaza.nps.service;

import com.payaza.nps.dto.PaymentRequestDto;
import com.payaza.nps.dto.PaymentResponseDto;
import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import com.payaza.nps.repository.PaymentRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for payment processing operations
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private NpsApiService npsApiService;


    /**
     * Process a new payment request
     */
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        logger.info("Processing payment request: {}", requestDto.getPaymentId());

        try {
            // Validate the payment request
            validatePaymentRequest(requestDto);

            // Check for duplicate payment ID
            if (paymentRequestRepository.existsByPaymentId(requestDto.getPaymentId())) {
                throw new IllegalArgumentException("Payment ID already exists: " + requestDto.getPaymentId());
            }

            // Create payment request entity
            PaymentRequest paymentRequest = createPaymentRequest(requestDto);
            paymentRequest = paymentRequestRepository.save(paymentRequest);

            // Send to NPS API
            PaymentResponseDto response = npsApiService.sendPaymentRequest(paymentRequest);

            // Update payment request with response
            updatePaymentRequestWithResponse(paymentRequest, response);
            paymentRequestRepository.save(paymentRequest);

            logger.info("Payment processed successfully: {}", requestDto.getPaymentId());
            return response;

        } catch (Exception e) {
            logger.error("Error processing payment: {}", requestDto.getPaymentId(), e);
            return createErrorResponse(requestDto.getPaymentId(), requestDto.getTransactionId(), e.getMessage());
        }
    }

    /**
     * Get payment status by payment ID
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatus(String paymentId) {
        logger.info("Getting payment status for: {}", paymentId);

        Optional<PaymentRequest> paymentOpt = paymentRequestRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }

        PaymentRequest payment = paymentOpt.get();
        return mapToResponseDto(payment);
    }

    /**
     * Get payment status by transaction ID
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatusByTransactionId(String transactionId) {
        logger.info("Getting payment status for transaction: {}", transactionId);

        Optional<PaymentRequest> paymentOpt = paymentRequestRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }

        PaymentRequest payment = paymentOpt.get();
        return mapToResponseDto(payment);
    }

    /**
     * Cancel a payment
     */
    public PaymentResponseDto cancelPayment(String paymentId) {
        logger.info("Cancelling payment: {}", paymentId);

        Optional<PaymentRequest> paymentOpt = paymentRequestRepository.findByPaymentId(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }

        PaymentRequest payment = paymentOpt.get();
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment cannot be cancelled in current status: " + payment.getStatus());
        }

        // Send cancellation request to NPS
        PaymentResponseDto response = npsApiService.cancelPayment(payment);

        // Update payment status
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setErrorMessage(response.getResponseMessage());
        paymentRequestRepository.save(payment);

        return response;
    }

    /**
     * Get all payments by status
     */
    @Transactional(readOnly = true)
    public List<PaymentRequest> getPaymentsByStatus(PaymentStatus status) {
        return paymentRequestRepository.findByStatus(status);
    }

    /**
     * Validate payment request
     */
    private void validatePaymentRequest(PaymentRequestDto requestDto) {
        if (requestDto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (!"NGN".equalsIgnoreCase(requestDto.getCurrency())) {
            throw new IllegalArgumentException("Only NGN currency is supported");
        }

        if (requestDto.getSenderAccount().equals(requestDto.getReceiverAccount())) {
            throw new IllegalArgumentException("Sender and receiver accounts cannot be the same");
        }
    }

    /**
     * Create payment request entity from DTO
     */
    private PaymentRequest createPaymentRequest(PaymentRequestDto requestDto) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentId(requestDto.getPaymentId());
        paymentRequest.setTransactionId(requestDto.getTransactionId());
        paymentRequest.setSenderAccount(requestDto.getSenderAccount());
        paymentRequest.setReceiverAccount(requestDto.getReceiverAccount());
        paymentRequest.setAmount(requestDto.getAmount());
        paymentRequest.setCurrency(requestDto.getCurrency().toUpperCase());
        paymentRequest.setPaymentPurpose(requestDto.getPaymentPurpose());
        paymentRequest.setPaymentType(requestDto.getPaymentType());
        paymentRequest.setReferenceNumber(requestDto.getReferenceNumber());
        paymentRequest.setStatus(PaymentStatus.PENDING);
        paymentRequest.setCreatedAt(LocalDateTime.now());
        return paymentRequest;
    }

    /**
     * Update payment request with API response
     */
    private void updatePaymentRequestWithResponse(PaymentRequest paymentRequest, PaymentResponseDto response) {
        paymentRequest.setNpsReference(response.getNpsReference());
        paymentRequest.setStatus(response.getStatus());
        paymentRequest.setResponseCode(response.getResponseCode());
        paymentRequest.setErrorMessage(response.getResponseMessage());
        paymentRequest.setUpdatedAt(LocalDateTime.now());
        
        if (response.getStatus() == PaymentStatus.SUCCESS || response.getStatus() == PaymentStatus.FAILED) {
            paymentRequest.setProcessedAt(LocalDateTime.now());
        }
    }

    /**
     * Create error response
     */
    private PaymentResponseDto createErrorResponse(String paymentId, String transactionId, String errorMessage) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(paymentId);
        response.setTransactionId(transactionId);
        response.setStatus(PaymentStatus.FAILED);
        response.setResponseCode("ERROR");
        response.setResponseMessage(errorMessage);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    /**
     * Map PaymentRequest to PaymentResponseDto
     */
    private PaymentResponseDto mapToResponseDto(PaymentRequest payment) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(payment.getPaymentId());
        response.setTransactionId(payment.getTransactionId());
        response.setNpsReference(payment.getNpsReference());
        response.setStatus(payment.getStatus());
        response.setResponseCode(payment.getResponseCode());
        response.setResponseMessage(payment.getErrorMessage());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setProcessedAt(payment.getProcessedAt());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
