package com.payaza.nps.controller;

import com.payaza.nps.dto.PaymentRequestJsonDto;
import com.payaza.nps.dto.PaymentResponseJsonDto;
import com.payaza.nps.dto.PaymentStatusRequestJsonDto;
import com.payaza.nps.model.iso20022.PaymentRequest;
import com.payaza.nps.model.iso20022.PaymentStatusReport;
import com.payaza.nps.model.iso20022.PaymentStatusRequest;
import com.payaza.nps.service.NpsMessageConverter;
import com.payaza.nps.service.NpsPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;

/**
 * REST Controller for NPS Payment operations
 * Handles pacs.008 (Payment Request), pacs.002 (Payment Status Report), and pacs.028 (Payment Status Request)
 */
@RestController
@RequestMapping("/api/v1/nps/payments")
@CrossOrigin(origins = "*")
public class NpsPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(NpsPaymentController.class);

    @Autowired
    private NpsPaymentService paymentService;

    @Autowired
    private NpsMessageConverter messageConverter;

    /**
     * Step 2: Submit Payment Request (pacs.008) to NPS
     * This endpoint accepts JSON requests and converts them to ISO 20022 internally
     */
    @PostMapping("/submit")
    public ResponseEntity<String> submitPaymentRequest(@Valid @RequestBody PaymentRequestJsonDto jsonRequest) {
        logger.info("Received JSON payment request for end-to-end ID: {}", jsonRequest.getEndToEndId());
        
        try {
            // Convert JSON to ISO 20022 XML
            PaymentRequest iso20022Request = messageConverter.convertToPaymentRequest(jsonRequest);
            
            // Submit to NPS (asynchronous processing)
            String response = paymentService.submitPaymentRequest(iso20022Request);
            
            logger.info("Payment request submitted successfully to NPS for end-to-end ID: {}", jsonRequest.getEndToEndId());
            return ResponseEntity.ok("Payment request submitted successfully. " +
                                   "Payment status will be received via callback.");
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error submitting payment request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit payment request");
        }
    }

    /**
     * Step 5: Receive Payment Status Report (pacs.002) from NPS
     * This is the callback endpoint that NPS calls with payment status results
     * Handles ACSC (Accepted), RJCT (Rejected), and NAUT (Not Authorized) statuses
     */
    @PostMapping("/callback/status-report")
    public ResponseEntity<String> receivePaymentStatusReport(@RequestBody PaymentStatusReport report) {
        logger.info("Received payment status report from NPS: {}", 
                   report.getFiToFIPmtStsRpt().getGrpHdr().getMsgId());
        
        try {
            // Process the payment status report
            paymentService.processPaymentStatusReport(report);
            
            logger.info("Payment status report processed successfully");
            return ResponseEntity.ok("Payment status report received and processed");
            
        } catch (Exception e) {
            logger.error("Error processing payment status report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process payment status report");
        }
    }

    /**
     * Step 1: Request Payment Status (pacs.028) from NPS
     * Can be sent by either Payment Sender or Payment Receiver
     * Accepts JSON requests and converts them to ISO 20022 internally
     */
    @PostMapping("/status-request")
    public ResponseEntity<PaymentResponseJsonDto> requestPaymentStatus(@Valid @RequestBody PaymentStatusRequestJsonDto jsonRequest) {
        logger.info("Received JSON payment status request for original message: {}", jsonRequest.getOriginalMessageId());
        
        try {
            // Convert JSON to ISO 20022 XML
            PaymentStatusRequest iso20022Request = messageConverter.convertToPaymentStatusRequest(jsonRequest);
            
            // Request status from NPS
            PaymentStatusReport statusReport = paymentService.requestPaymentStatus(iso20022Request);
            
            // Convert ISO 20022 response to JSON
            PaymentResponseJsonDto jsonResponse = messageConverter.convertFromPaymentStatusReport(statusReport);
            
            logger.info("Payment status request completed successfully");
            return ResponseEntity.ok(jsonResponse);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment status request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error requesting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create and submit payment request for credit transfer
     * Simplified endpoint for creating payment requests using JSON
     */
    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@Valid @RequestBody PaymentRequestJsonDto jsonRequest) {
        logger.info("Creating payment request for end-to-end ID: {}", jsonRequest.getEndToEndId());
        
        try {
            // Convert JSON to ISO 20022 XML
            PaymentRequest iso20022Request = messageConverter.convertToPaymentRequest(jsonRequest);
            
            // Submit to NPS
            String response = paymentService.submitPaymentRequest(iso20022Request);
            
            logger.info("Payment request created and submitted successfully for end-to-end ID: {}", jsonRequest.getEndToEndId());
            return ResponseEntity.ok("Payment request created and submitted successfully. " +
                                   "Payment status will be received via callback.");
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request parameters: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating payment request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create payment request");
        }
    }

    /**
     * Create and submit payment status request
     * Simplified endpoint for requesting payment status using JSON
     */
    @PostMapping("/request-status")
    public ResponseEntity<PaymentResponseJsonDto> createPaymentStatusRequest(@Valid @RequestBody PaymentStatusRequestJsonDto jsonRequest) {
        logger.info("Creating payment status request for original message: {}", jsonRequest.getOriginalMessageId());
        
        try {
            // Convert JSON to ISO 20022 XML
            PaymentStatusRequest iso20022Request = messageConverter.convertToPaymentStatusRequest(jsonRequest);
            
            // Request status from NPS
            PaymentStatusReport statusReport = paymentService.requestPaymentStatus(iso20022Request);
            
            // Convert ISO 20022 response to JSON
            PaymentResponseJsonDto jsonResponse = messageConverter.convertFromPaymentStatusReport(statusReport);
            
            logger.info("Payment status request created and submitted successfully");
            return ResponseEntity.ok(jsonResponse);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid payment status request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating payment status request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for payment service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("NPS Payment Service is running");
    }

    /**
     * Get supported payment message types
     */
    @GetMapping("/message-types")
    public ResponseEntity<PaymentMessageTypes> getSupportedMessageTypes() {
        PaymentMessageTypes messageTypes = new PaymentMessageTypes();
        messageTypes.addMessageType("pacs.008.001.08", "Payment Request");
        messageTypes.addMessageType("pacs.002.001.10", "Payment Status Report");
        messageTypes.addMessageType("pacs.028.001.04", "Payment Status Request");
        
        return ResponseEntity.ok(messageTypes);
    }

    /**
     * Data class for supported message types
     */
    public static class PaymentMessageTypes {
        private java.util.Map<String, String> messageTypes = new java.util.HashMap<>();

        public void addMessageType(String messageType, String description) {
            messageTypes.put(messageType, description);
        }

        public java.util.Map<String, String> getMessageTypes() {
            return messageTypes;
        }

        public void setMessageTypes(java.util.Map<String, String> messageTypes) {
            this.messageTypes = messageTypes;
        }
    }
}
