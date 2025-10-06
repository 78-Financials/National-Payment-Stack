package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
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

    /**
     * Process payment request with client authentication and validation
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('ROLE_CLIENT_BAN') or hasRole('ROLE_CLIENT_FIN') or hasRole('ROLE_CLIENT_PAY')")
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
            
            Pacs008ResponseDto response = pacs008Service.processPaymentRequest(request);
            logger.info("PACS.008 payment request completed for client '{}': {} - {}", 
                       clientId, request.getMessageId(), response.getStatus());
            
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
                    "debtorAccount", request.getDebtorAccount(),
                    "creditorAccount", request.getCreditorAccount(),
                    "status", response.getStatus(),
                    "responseCode", response.getResponseCode()
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 payment request for client '{}': {}", clientId, e.getMessage(), e);
            
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
                    "debtorAccount", request.getDebtorAccount(),
                    "creditorAccount", request.getCreditorAccount()
                )
            );
            
            Pacs008ResponseDto errorResponse = createErrorResponse(request.getMessageId(), request.getTransactionId(), e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
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
}
