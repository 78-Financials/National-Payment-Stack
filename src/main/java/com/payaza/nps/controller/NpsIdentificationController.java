package com.payaza.nps.controller;

import com.payaza.nps.dto.IdentificationVerificationRequestJsonDto;
import com.payaza.nps.dto.IdentificationVerificationResponseJsonDto;
import com.payaza.nps.model.iso20022.IdentificationVerificationRequest;
import com.payaza.nps.model.iso20022.IdentificationVerificationReport;
import com.payaza.nps.service.NpsIdentificationService;
import com.payaza.nps.service.NpsMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for NPS Identification Verification operations
 * Handles acmt.023 (Identification Verification Request) and acmt.024 (Identification Verification Report)
 */
@RestController
@RequestMapping("/api/v1/nps/identification")
@CrossOrigin(origins = "*")
public class NpsIdentificationController {

    private static final Logger logger = LoggerFactory.getLogger(NpsIdentificationController.class);

    @Autowired
    private NpsIdentificationService identificationService;

    @Autowired
    private NpsMessageConverter messageConverter;

    /**
     * Step 2: Submit Identification Verification Request (acmt.023) to NPS
     * This endpoint accepts JSON requests and converts them to ISO 20022 internally
     */
    @PostMapping("/verify")
    public ResponseEntity<String> submitIdentificationVerificationRequest(
            @Valid @RequestBody IdentificationVerificationRequestJsonDto jsonRequest) {
        
        logger.info("Received JSON identification verification request for account: {}", jsonRequest.getAccountId());
        
        try {
            // Convert JSON to ISO 20022 XML
            IdentificationVerificationRequest iso20022Request = messageConverter.convertToIdentificationVerificationRequest(jsonRequest);
            
            // Submit to NPS (asynchronous processing)
            String response = identificationService.sendIdentificationVerificationRequest(iso20022Request);
            
            logger.info("Identification verification request submitted successfully to NPS for account: {}", jsonRequest.getAccountId());
            return ResponseEntity.ok("Identification verification request submitted successfully. " +
                                   "Verification results will be received via callback.");
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid identification verification request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error submitting identification verification request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit identification verification request");
        }
    }

    /**
     * Step 5: Receive Identification Verification Report (acmt.024) from NPS
     * This is the callback endpoint that NPS calls with verification results
     * This implements the asynchronous processing model
     */
    @PostMapping("/callback/report")
    public ResponseEntity<String> receiveIdentificationVerificationReport(
            @RequestBody IdentificationVerificationReport report) {
        
        logger.info("Received identification verification report from NPS: {}", 
                   report.getIdVrfctnRpt().getMsgHdr().getMsgId());
        
        try {
            // Process the verification report
            identificationService.processIdentificationVerificationReport(report);
            
            logger.info("Identification verification report processed successfully");
            return ResponseEntity.ok("Identification verification report received and processed");
            
        } catch (Exception e) {
            logger.error("Error processing identification verification report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process identification verification report");
        }
    }

    /**
     * Create and submit identification verification request for account verification
     * Simplified endpoint for creating verification requests using JSON
     */
    @PostMapping("/verify-account")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody IdentificationVerificationRequestJsonDto jsonRequest) {
        logger.info("Creating identification verification request for account: {}", jsonRequest.getAccountId());
        
        try {
            // Convert JSON to ISO 20022 XML
            IdentificationVerificationRequest iso20022Request = messageConverter.convertToIdentificationVerificationRequest(jsonRequest);
            
            // Submit to NPS
            String response = identificationService.sendIdentificationVerificationRequest(iso20022Request);
            
            logger.info("Account verification request submitted successfully for account: {}", jsonRequest.getAccountId());
            return ResponseEntity.ok("Account verification request submitted successfully. " +
                                   "Verification results will be received via callback.");
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid account verification request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating account verification request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create account verification request");
        }
    }

    /**
     * Health check endpoint for identification verification service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("NPS Identification Verification Service is running");
    }

    /**
     * Test endpoint for encryption modes
     */
    @PostMapping("/test-encryption")
    public ResponseEntity<String> testEncryption() {
        try {
            identificationService.testEncryptionModes();
            return ResponseEntity.ok("Encryption modes tested successfully");
        } catch (Exception e) {
            logger.error("Encryption test failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Encryption test failed: " + e.getMessage());
        }
    }
}
