package com.payaza.nps.controller;

import com.payaza.nps.dto.Acmt023RequestDto;
import com.payaza.nps.dto.Acmt023ResponseDto;
import com.payaza.nps.service.SimpleAcmt023Service;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Process identification verification request
     */
    @PostMapping("/verify")
    public ResponseEntity<Acmt023ResponseDto> verifyAccount(
            @Valid @RequestBody Acmt023RequestDto request) {
        
        logger.info("Received ACMT.023 identification verification request: {}", request.getMessageId());
        
        try {
            Acmt023ResponseDto response = acmt023Service.processIdentificationVerification(request);
            logger.info("ACMT.023 identification verification completed: {} - {}", 
                       request.getMessageId(), response.getStatus());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.023 identification verification: {}", e.getMessage(), e);
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
