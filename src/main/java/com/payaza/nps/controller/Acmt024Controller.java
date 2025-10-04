package com.payaza.nps.controller;

import com.payaza.nps.dto.Acmt024RequestDto;
import com.payaza.nps.dto.Acmt024ResponseDto;
import com.payaza.nps.service.SimpleAcmt024Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for ACMT.024 Identification Verification Report
 */
@RestController
@RequestMapping("/api/v1/identification")
@CrossOrigin(origins = "*")
public class Acmt024Controller {

    private static final Logger logger = LoggerFactory.getLogger(Acmt024Controller.class);

    @Autowired
    private SimpleAcmt024Service acmt024Service;

    /**
     * Process identification verification report
     */
    @PostMapping("/report")
    public ResponseEntity<Acmt024ResponseDto> reportVerification(
            @Valid @RequestBody Acmt024RequestDto request) {
        
        logger.info("Received ACMT.024 identification verification report: {}", request.getMessageId());
        
        try {
            Acmt024ResponseDto response = acmt024Service.processIdentificationVerificationReport(request);
            logger.info("ACMT.024 identification verification report completed: {} - {}", 
                       request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.024 identification verification report: {}", e.getMessage(), e);
            Acmt024ResponseDto errorResponse = createErrorResponse(request.getMessageId(), request.getOriginalMessageId(), e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private Acmt024ResponseDto createErrorResponse(String messageId, String originalMessageId, String errorMessage) {
        Acmt024ResponseDto response = new Acmt024ResponseDto();
        response.setMessageId(messageId);
        response.setOriginalMessageId(originalMessageId);
        response.setResponseCode("99");
        response.setResponseMessage("System error: " + errorMessage);
        response.setStatus("FAILED");
        response.setVerificationStatus("FAILED");
        response.setAccountVerified(false);
        response.setCreatedAt(java.time.LocalDateTime.now());
        return response;
    }
}
