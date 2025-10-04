package com.payaza.nps.controller;

import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.service.SimplePacs008Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Process payment request
     */
    @PostMapping("/transfer")
    public ResponseEntity<Pacs008ResponseDto> processPayment(
            @Valid @RequestBody Pacs008RequestDto request) {
        
        logger.info("Received PACS.008 payment request: {}", request.getMessageId());
        
        try {
            Pacs008ResponseDto response = pacs008Service.processPaymentRequest(request);
            logger.info("PACS.008 payment request completed: {} - {}", 
                       request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 payment request: {}", e.getMessage(), e);
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
