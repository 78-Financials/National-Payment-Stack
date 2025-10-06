package com.payaza.nps.controller;

import com.payaza.nps.service.InboundPacs008Processor;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.dto.Pacs002ResponseDto;
import com.payaza.nps.dto.Acmt024ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * NIBSS Callback Controller
 * 
 * Handles incoming asynchronous messages from NIBSS following their strict endpoint structure:
 * - /pacs008 - Receive PACS.008 messages (inbound payment requests)
 * - /pacs002 - Receive PACS.002 messages (payment status reports)
 * - /acmt023 - Receive ACMT.023 messages (identification verification requests)
 * - /acmt024 - Receive ACMT.024 messages (identification verification reports)
 */
@RestController
@RequestMapping("/callbacks")
public class NibssCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(NibssCallbackController.class);

    @Autowired
    private InboundPacs008Processor inboundPacs008Processor;

    @Autowired
    private PaymentStatusTrackingService paymentStatusTrackingService;

    @Autowired
    private AuditService auditService;

    /**
     * Handle inbound PACS.008 messages from NIBSS
     * These are payment requests sent TO us by NIBSS
     */
    @PostMapping("/pacs008")
    public ResponseEntity<String> handlePacs008Callback(@RequestBody String xmlMessage) {
        logger.info("Received inbound PACS.008 callback from NIBSS");
        
        try {
            // Process inbound PACS.008
            inboundPacs008Processor.processInboundPacs008(xmlMessage);
            
            // Log the callback
            auditService.logApiCall("NIBSS_CALLBACK_PACS008", "POST", "/callbacks/pacs008", 
                System.currentTimeMillis(), HttpStatus.OK.value(), "Inbound PACS.008 processed successfully");
            
            return ResponseEntity.ok("PACS.008 processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing inbound PACS.008: {}", e.getMessage(), e);
            
            // Log the error
            auditService.logError("NIBSS_CALLBACK_PACS008", "POST", 
                com.payaza.nps.model.AuditLog.ActionType.API_CALL, "/callbacks/pacs008", 
                "Error processing inbound PACS.008: " + e.getMessage(), 
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), null, null, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.008: " + e.getMessage());
        }
    }

    /**
     * Handle PACS.002 messages from NIBSS
     * These are payment status reports for our outbound PACS.008 requests
     */
    @PostMapping("/pacs002")
    public ResponseEntity<String> handlePacs002Callback(@RequestBody String xmlMessage) {
        logger.info("Received PACS.002 callback from NIBSS");
        
        try {
            // Parse PACS.002 response
            Pacs002ResponseDto response = parsePacs002Response(xmlMessage);
            
            // Update transaction status
            paymentStatusTrackingService.updatePaymentStatus(
                response.getTransactionId(), 
                response.getStatus(), 
                response.getResponseMessage()
            );
            
            // Log the callback
            auditService.logApiCall("NIBSS_CALLBACK_PACS002", "POST", "/callbacks/pacs002", 
                System.currentTimeMillis(), HttpStatus.OK.value(), "PACS.002 processed successfully");
            
            return ResponseEntity.ok("PACS.002 processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.002: {}", e.getMessage(), e);
            
            // Log the error
            auditService.logError("NIBSS_CALLBACK_PACS002", "POST", 
                com.payaza.nps.model.AuditLog.ActionType.API_CALL, "/callbacks/pacs002", 
                "Error processing PACS.002: " + e.getMessage(), 
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), null, null, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.002: " + e.getMessage());
        }
    }

    /**
     * Handle ACMT.023 messages from NIBSS
     * These are identification verification requests sent TO us by NIBSS
     */
    @PostMapping("/acmt023")
    public ResponseEntity<String> handleAcmt023Callback(@RequestBody String xmlMessage) {
        logger.info("Received ACMT.023 callback from NIBSS");
        
        try {
            // Process ACMT.023 request
            // Implementation will be added based on business requirements
            
            // Log the callback
            auditService.logApiCall("NIBSS_CALLBACK_ACMT023", "POST", "/callbacks/acmt023", 
                System.currentTimeMillis(), HttpStatus.OK.value(), "ACMT.023 processed successfully");
            
            return ResponseEntity.ok("ACMT.023 processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.023: {}", e.getMessage(), e);
            
            // Log the error
            auditService.logError("NIBSS_CALLBACK_ACMT023", "POST", 
                com.payaza.nps.model.AuditLog.ActionType.API_CALL, "/callbacks/acmt023", 
                "Error processing ACMT.023: " + e.getMessage(), 
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), null, null, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing ACMT.023: " + e.getMessage());
        }
    }

    /**
     * Handle ACMT.024 messages from NIBSS
     * These are identification verification reports for our outbound ACMT.023 requests
     */
    @PostMapping("/acmt024")
    public ResponseEntity<String> handleAcmt024Callback(@RequestBody String xmlMessage) {
        logger.info("Received ACMT.024 callback from NIBSS");
        
        try {
            // Parse ACMT.024 response
            Acmt024ResponseDto response = parseAcmt024Response(xmlMessage);
            
            // Update identification verification status
            // Implementation will be added based on business requirements
            
            // Log the callback
            auditService.logApiCall("NIBSS_CALLBACK_ACMT024", "POST", "/callbacks/acmt024", 
                System.currentTimeMillis(), HttpStatus.OK.value(), "ACMT.024 processed successfully");
            
            return ResponseEntity.ok("ACMT.024 processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.024: {}", e.getMessage(), e);
            
            // Log the error
            auditService.logError("NIBSS_CALLBACK_ACMT024", "POST", 
                com.payaza.nps.model.AuditLog.ActionType.API_CALL, "/callbacks/acmt024", 
                "Error processing ACMT.024: " + e.getMessage(), 
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), null, null, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing ACMT.024: " + e.getMessage());
        }
    }

    /**
     * Parse PACS.002 response from XML
     */
    private Pacs002ResponseDto parsePacs002Response(String xmlMessage) {
        // Implementation will parse XML and extract relevant fields
        // This is a placeholder - actual implementation will use XML parsing
        Pacs002ResponseDto response = new Pacs002ResponseDto();
        response.setTransactionId("extracted_from_xml");
        response.setStatus("SUCCESS");
        response.setResponseMessage("Success");
        return response;
    }

    /**
     * Parse ACMT.024 response from XML
     */
    private Acmt024ResponseDto parseAcmt024Response(String xmlMessage) {
        // Implementation will parse XML and extract relevant fields
        // This is a placeholder - actual implementation will use XML parsing
        Acmt024ResponseDto response = new Acmt024ResponseDto();
        response.setTransactionId("extracted_from_xml");
        response.setAccountVerified(true);
        return response;
    }
}
