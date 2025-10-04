package com.payaza.nps.controller;

import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import com.payaza.nps.repository.PaymentRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling NPS callback notifications
 */
@RestController
@RequestMapping("/api/v1/callbacks")
@CrossOrigin(origins = "*")
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    /**
     * Handle payment status callback from NPS
     */
    @PostMapping("/payment-status")
    public ResponseEntity<String> handlePaymentStatusCallback(@RequestBody Map<String, Object> callbackData) {
        logger.info("Received payment status callback: {}", callbackData);

        try {
            String paymentId = (String) callbackData.get("paymentId");
            String status = (String) callbackData.get("status");
            String responseCode = (String) callbackData.get("responseCode");
            String responseMessage = (String) callbackData.get("responseMessage");
            String npsReference = (String) callbackData.get("npsReference");

            if (paymentId == null) {
                logger.error("Payment ID missing in callback data");
                return ResponseEntity.badRequest().body("Payment ID is required");
            }

            // Find the payment request
            Optional<PaymentRequest> paymentOpt = paymentRequestRepository.findByPaymentId(paymentId);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for callback: {}", paymentId);
                return ResponseEntity.notFound().build();
            }

            PaymentRequest payment = paymentOpt.get();

            // Update payment status
            PaymentStatus paymentStatus = mapStringToStatus(status);
            payment.setStatus(paymentStatus);
            payment.setResponseCode(responseCode);
            payment.setErrorMessage(responseMessage);
            payment.setNpsReference(npsReference);
            payment.setUpdatedAt(LocalDateTime.now());

            if (paymentStatus == PaymentStatus.SUCCESS || paymentStatus == PaymentStatus.FAILED) {
                payment.setProcessedAt(LocalDateTime.now());
            }

            paymentRequestRepository.save(payment);

            logger.info("Payment status updated successfully: {} -> {}", paymentId, paymentStatus);
            return ResponseEntity.ok("Callback processed successfully");

        } catch (Exception e) {
            logger.error("Error processing callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing callback");
        }
    }

    /**
     * Handle webhook verification
     */
    @GetMapping("/webhook/verify")
    public ResponseEntity<String> verifyWebhook(@RequestParam String challenge) {
        logger.info("Webhook verification requested with challenge: {}", challenge);
        return ResponseEntity.ok(challenge);
    }

    /**
     * Map string status to PaymentStatus enum
     */
    private PaymentStatus mapStringToStatus(String status) {
        if (status == null) {
            return PaymentStatus.FAILED;
        }

        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED" -> PaymentStatus.SUCCESS;
            case "PENDING" -> PaymentStatus.PENDING;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            case "FAILED", "ERROR" -> PaymentStatus.FAILED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            case "TIMEOUT" -> PaymentStatus.TIMEOUT;
            case "REJECTED" -> PaymentStatus.REJECTED;
            default -> PaymentStatus.FAILED;
        };
    }
}
