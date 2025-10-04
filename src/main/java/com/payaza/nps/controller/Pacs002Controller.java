package com.payaza.nps.controller;

import com.payaza.nps.dto.Pacs002RequestDto;
import com.payaza.nps.dto.Pacs002ResponseDto;
import com.payaza.nps.service.SimplePacs002Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for PACS.002 Payment Status Report
 */
@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class Pacs002Controller {

    private static final Logger logger = LoggerFactory.getLogger(Pacs002Controller.class);

    @Autowired
    private SimplePacs002Service pacs002Service;

    /**
     * Process payment status report
     */
    @PostMapping("/status-report")
    public ResponseEntity<Pacs002ResponseDto> processStatusReport(
            @Valid @RequestBody Pacs002RequestDto request) {
        
        logger.info("Received PACS.002 payment status report: {}", request.getMessageId());
        
        try {
            Pacs002ResponseDto response = pacs002Service.processPaymentStatusReport(request);
            logger.info("PACS.002 payment status report completed: {} - {}", 
                       request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PACS.002 payment status report: {}", e.getMessage(), e);
            Pacs002ResponseDto errorResponse = createErrorResponse(request.getMessageId(), request.getOriginalMessageId(), e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private Pacs002ResponseDto createErrorResponse(String messageId, String originalMessageId, String errorMessage) {
        Pacs002ResponseDto response = new Pacs002ResponseDto();
        response.setMessageId(messageId);
        response.setOriginalMessageId(originalMessageId);
        response.setResponseCode("99");
        response.setResponseMessage("System error: " + errorMessage);
        response.setStatus("FAILED");
        response.setPaymentStatus("FAILED");
        response.setCreatedAt(java.time.LocalDateTime.now());
        return response;
    }
}
