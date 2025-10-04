package com.payaza.nps.controller;

import com.payaza.nps.dto.Pacs028RequestDto;
import com.payaza.nps.dto.Pacs028ResponseDto;
import com.payaza.nps.service.SimplePacs028Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for PACS.028 Payment Status Request
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class Pacs028Controller {

    private static final Logger logger = LoggerFactory.getLogger(Pacs028Controller.class);

    @Autowired
    private SimplePacs028Service pacs028Service;

    /**
     * Process payment status request
     */
    @PostMapping("/status-request")
    public ResponseEntity<Pacs028ResponseDto> requestPaymentStatus(
            @Valid @RequestBody Pacs028RequestDto request) {
        
        logger.info("Received PACS.028 payment status request: {}", request.getMessageId());
        
        try {
            Pacs028ResponseDto response = pacs028Service.processPaymentStatusRequest(request);
            logger.info("PACS.028 payment status request completed: {} - {}", 
                       request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.028 payment status request: {}", e.getMessage(), e);
            Pacs028ResponseDto errorResponse = createErrorResponse(request.getMessageId(), request.getOriginalMessageId(), e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private Pacs028ResponseDto createErrorResponse(String messageId, String originalMessageId, String errorMessage) {
        Pacs028ResponseDto response = new Pacs028ResponseDto();
        response.setMessageId(messageId);
        response.setOriginalMessageId(originalMessageId);
        response.setResponseCode("99");
        response.setResponseMessage("System error: " + errorMessage);
        response.setStatus("FAILED");
        response.setCreatedAt(java.time.LocalDateTime.now());
        return response;
    }
}
