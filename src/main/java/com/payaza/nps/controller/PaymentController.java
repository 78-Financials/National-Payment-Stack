package com.payaza.nps.controller;

import com.payaza.nps.dto.PaymentRequestDto;
import com.payaza.nps.dto.PaymentResponseDto;
import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import com.payaza.nps.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for payment operations
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Process a new payment
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        logger.info("Received payment request: {}", requestDto.getPaymentId());
        
        try {
            PaymentResponseDto response = paymentService.processPayment(requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(requestDto.getPaymentId(), "INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(requestDto.getPaymentId(), "INTERNAL_ERROR", "Internal server error"));
        }
    }

    /**
     * Get payment status by payment ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable String paymentId) {
        logger.info("Getting payment status for: {}", paymentId);
        
        try {
            PaymentResponseDto response = paymentService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(paymentId, "INTERNAL_ERROR", "Internal server error"));
        }
    }

    /**
     * Get payment status by transaction ID
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponseDto> getPaymentStatusByTransactionId(@PathVariable String transactionId) {
        logger.info("Getting payment status for transaction: {}", transactionId);
        
        try {
            PaymentResponseDto response = paymentService.getPaymentStatusByTransactionId(transactionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Transaction not found: {}", transactionId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(transactionId, "INTERNAL_ERROR", "Internal server error"));
        }
    }

    /**
     * Cancel a payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable String paymentId) {
        logger.info("Cancelling payment: {}", paymentId);
        
        try {
            PaymentResponseDto response = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Payment not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("Payment cannot be cancelled: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(paymentId, "CANCEL_FAILED", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(paymentId, "INTERNAL_ERROR", "Internal server error"));
        }
    }

    /**
     * Get payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentRequest>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        logger.info("Getting payments by status: {}", status);
        
        try {
            List<PaymentRequest> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("Error getting payments by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("NPS Payment Service is running");
    }

    /**
     * Create error response
     */
    private PaymentResponseDto createErrorResponse(String paymentId, String errorCode, String errorMessage) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(paymentId);
        response.setStatus(PaymentStatus.FAILED);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        return response;
    }
}
