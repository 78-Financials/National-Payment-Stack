package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.dto.Acmt023RequestDto;
import com.payaza.nps.dto.Acmt023ResponseDto;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.SimpleAcmt023Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for ACMT.023 Identification Verification Request
 */
@RestController
@RequestMapping("/api/v1/identification")
@CrossOrigin(origins = "*")
public class Acmt023Controller {

    private static final Logger logger = LoggerFactory.getLogger(Acmt023Controller.class);

    @Autowired
    private SimpleAcmt023Service acmt023Service;

    @Autowired
    private AuditService auditService;

    /**
     * Process identification verification request
     */
    @PostMapping("/verify")
    @Auditable(action = "ACMT023_VERIFY", resource = "IdentificationVerification", actionType = AuditLog.ActionType.API_CALL, message = "ACMT.023 identification verification request processed")
    public ResponseEntity<Acmt023ResponseDto> verifyAccount(
            @Valid @RequestBody Acmt023RequestDto request) {
        
        String clientId = ClientContext.getCurrentClientId();
        logger.info("Received ACMT.023 identification verification request from client '{}': {}", clientId, request.getMessageId());
        
        try {
            Acmt023ResponseDto response = acmt023Service.processIdentificationVerification(request);
            logger.info("ACMT.023 identification verification completed for client '{}': {} - {}", 
                       clientId, request.getMessageId(), response.getStatus());
            
            // Log successful verification
            auditService.logClientAction(
                "ACMT023_VERIFY", 
                "IdentificationVerification", 
                clientId, 
                AuditLog.ActionType.API_CALL,
                "Identification verification completed successfully",
                Map.of(
                    "messageId", request.getMessageId(),
                    "accountNumber", request.getAccountNumber(),
                    "bankCode", request.getBankCode(),
                    "status", response.getStatus(),
                    "verified", response.getAccountVerified()
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.023 identification verification for client '{}': {}", clientId, e.getMessage(), e);
            
            // Log failed verification
            auditService.logError(
                "ACMT023_VERIFY", 
                "IdentificationVerification", 
                AuditLog.ActionType.API_CALL,
                null,
                clientId,
                "Identification verification failed: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of(
                    "messageId", request.getMessageId(),
                    "accountNumber", request.getAccountNumber(),
                    "bankCode", request.getBankCode()
                )
            );
            
            Acmt023ResponseDto errorResponse = createErrorResponse(request.getMessageId(), e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private Acmt023ResponseDto createErrorResponse(String messageId, String errorMessage) {
        Acmt023ResponseDto response = new Acmt023ResponseDto();
        response.setMessageId(messageId);
        response.setResponseCode("99");
        response.setResponseMessage("System error: " + errorMessage);
        response.setStatus("FAILED");
        response.setAccountVerified(false);
        response.setCreatedAt(java.time.LocalDateTime.now());
        return response;
    }
}
