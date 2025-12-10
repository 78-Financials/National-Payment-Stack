package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.SimplePacs008Service;
import com.payaza.nps.validation.ClientPermissionValidator;
import com.payaza.nps.validation.TransactionIdValidator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for PACS.008 Payment Request
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class Pacs008Controller {

    private static final Logger logger = LoggerFactory.getLogger(Pacs008Controller.class);

    @Autowired
    private SimplePacs008Service pacs008Service;
    
    @Autowired
    private ClientPermissionValidator permissionValidator;
    
    @Autowired
    private TransactionIdValidator transactionIdValidator;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PaymentStatusTrackingService statusTrackingService;

    @Autowired
    private PaymentTransactionLiveRepository paymentTransactionRepository;

    /**
     * Process payment request with client authentication and validation
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('ROLE_CLIENT_BANK') or hasRole('ROLE_CLIENT_FIN') or hasRole('ROLE_CLIENT_PAY')")
    @Auditable(action = "PACS008_TRANSFER", resource = "PaymentTransfer", actionType = AuditLog.ActionType.API_CALL, message = "PACS.008 payment transfer request processed")
    public ResponseEntity<Pacs008ResponseDto> processPayment(
            @Valid @RequestBody Pacs008RequestDto request) {
        
        String clientId = ClientContext.getCurrentClientId();
        logger.info("Received PACS.008 payment request from client '{}': {}", clientId, request.getMessageId());
        
        try {
            // Validate client permissions
            if (!permissionValidator.hasPermission("pacs008")) {
                auditService.logFailure(
                    "PACS008_TRANSFER", 
                    "PaymentTransfer", 
                    AuditLog.ActionType.API_CALL,
                    null,
                    clientId,
                    "Client does not have permission to access PACS.008 endpoint",
                    "PERMISSION_DENIED",
                    Map.of("clientId", clientId, "endpoint", "pacs008")
                );
                throw new IllegalArgumentException("Client '" + clientId + "' does not have permission to access PACS.008 endpoint");
            }
            
            // Validate transaction ID format and prefix
            if (!transactionIdValidator.isValidTransactionId(request.getTransactionId())) {
                auditService.logFailure(
                    "PACS008_TRANSFER", 
                    "PaymentTransfer", 
                    AuditLog.ActionType.API_CALL,
                    null,
                    clientId,
                    "Invalid transaction ID format",
                    "INVALID_TRANSACTION_ID",
                    Map.of("transactionId", request.getTransactionId(), "expectedPrefix", ClientContext.getCurrentClientPrefix())
                );
                throw new IllegalArgumentException("Transaction ID '" + request.getTransactionId() + 
                    "' is invalid. Must start with client prefix followed by hyphen (e.g., '" + ClientContext.getCurrentClientPrefix() + "-123456789')");
            }
            
            // Check for duplicate transaction ID
            if (paymentTransactionRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
                auditService.logFailure(
                    "PACS008_TRANSFER", 
                    "PaymentTransfer", 
                    AuditLog.ActionType.API_CALL,
                    null,
                    clientId,
                    "Duplicate transaction ID",
                    "DUPLICATE_TRANSACTION_ID",
                    Map.of("transactionId", request.getTransactionId())
                );
                throw new IllegalArgumentException("Transaction ID '" + request.getTransactionId() + "' already exists");
            }
            
            Pacs008ResponseDto response = pacs008Service.processPaymentRequest(request);
            logger.info("PACS.008 payment request completed for client '{}': {} - {}", 
                       clientId, request.getMessageId(), response.getStatus());
            
            // Track the payment transaction for status monitoring (only for successful service calls)
            statusTrackingService.trackNewPayment(request, clientId);
            
            // Log successful payment request
            auditService.logClientAction(
                "PACS008_TRANSFER", 
                "PaymentTransfer", 
                clientId, 
                AuditLog.ActionType.API_CALL,
                "Payment transfer request processed successfully",
                Map.of(
                    "messageId", request.getMessageId(),
                    "transactionId", request.getTransactionId(),
                    "amount", request.getAmount(),
                    "currency", request.getCurrency(),
                    "debtorAccount", request.getSenderAccountNumber(),
                    "creditorAccount", request.getReceiverAccountNumber(),
                    "status", response.getStatus(),
                    "responseCode", response.getResponseCode()
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 payment request for client '{}': {}", clientId, e.getMessage(), e);
            
            // Determine if this is a client error (400) or system error (500)
            boolean isClientError = isClientError(e);
            
            // Only track the failed payment transaction if it was a system error (not client errors)
            // Client errors should not be tracked as they represent invalid requests
            if (!isClientError) {
                try {
                    statusTrackingService.trackNewPayment(request, clientId);
                    statusTrackingService.markTransactionAsFailed(request.getTransactionId(), e.getMessage(), "SYSTEM_ERROR");
                } catch (Exception trackingError) {
                    logger.warn("Failed to track error transaction: {}", trackingError.getMessage());
                }
            }
            
            // Log failed payment request
            auditService.logError(
                "PACS008_TRANSFER", 
                "PaymentTransfer", 
                AuditLog.ActionType.API_CALL,
                null,
                clientId,
                "Payment transfer request failed: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of(
                    "messageId", request.getMessageId(),
                    "transactionId", request.getTransactionId(),
                    "amount", request.getAmount(),
                    "currency", request.getCurrency(),
                    "debtorAccount", request.getSenderAccountNumber(),
                    "creditorAccount", request.getReceiverAccountNumber()
                )
            );
            
            Pacs008ResponseDto errorResponse = createErrorResponse(request.getMessageId(), request.getTransactionId(), e.getMessage());
            
            // Return appropriate status code based on error type
            if (isClientError) {
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }

    private Pacs008ResponseDto createErrorResponse(String messageId, String transactionId, String errorMessage) {
        Pacs008ResponseDto response = new Pacs008ResponseDto();
        response.setMessageId(messageId);
        response.setTransactionId(transactionId);
        response.setResponseCode("99");
        response.setResponseMessage("System error: " + errorMessage);
        response.setStatus("FAILED");
        response.setCreatedAt(java.time.LocalDateTime.now());
        return response;
    }
    
    /**
     * Check if the exception is a validation error that should not be tracked
     */
    private boolean isValidationError(Exception e) {
        // Check for common validation error patterns
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        // Check for validation constraint violations
        if (e instanceof jakarta.validation.ConstraintViolationException) {
            return true;
        }
        
        // Check for validation error messages
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("validation") ||
               lowerMessage.contains("constraint") ||
               lowerMessage.contains("invalid") ||
               lowerMessage.contains("must be") ||
               lowerMessage.contains("required") ||
               lowerMessage.contains("not blank") ||
               lowerMessage.contains("not null") ||
               lowerMessage.contains("size") ||
               lowerMessage.contains("pattern") ||
               lowerMessage.contains("decimal") ||
               lowerMessage.contains("amount must be greater than 0");
    }
    
    /**
     * Check if the exception is a client error (400) vs system error (500)
     */
    private boolean isClientError(Exception e) {
        // Check for validation constraint violations (handled by GlobalExceptionHandler)
        if (e instanceof jakarta.validation.ConstraintViolationException) {
            return true;
        }
        
        // Check for IllegalArgumentException (client errors like invalid transaction ID, duplicate transaction ID)
        if (e instanceof IllegalArgumentException) {
            return true;
        }
        
        // Check for permission errors
        if (e instanceof org.springframework.security.access.AccessDeniedException) {
            return true;
        }
        
        // All other exceptions are considered system errors
        return false;
    }
}
