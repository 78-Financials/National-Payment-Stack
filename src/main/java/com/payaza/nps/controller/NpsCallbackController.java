package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.NpsXmlDecryptionService;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.Acmt024XmlParser;
import com.payaza.nps.service.Pacs002XmlParser;
import com.payaza.nps.service.Pacs028XmlParser;
import com.payaza.nps.service.InboundPacs008Processor;
import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NPS Callback Controller
 * 
 * Unified controller for handling all incoming asynchronous messages from NIBSS:
 * - ACMT.023 (Identification Verification Request) - Inbound requests from NIBSS
 * - ACMT.024 (Identification Verification Report) - Responses to our ACMT.023 requests
 * - PACS.002 (Payment Status Report) - Responses to our PACS.008 requests
 * - PACS.008 (Inbound Payment Request) - Inbound payment requests from NIBSS
 * - PACS.028 (Payment Status Request Response) - Responses to our PACS.028 requests
 * 
 * All incoming messages are:
 * 1. Decrypted using our private key
 * 2. Signature verified using NIBSS public key
 * 3. Processed using appropriate services
 * 4. Logged for audit and monitoring
 */
@RestController
@RequestMapping("/api/v1/nps/callback")
@CrossOrigin(origins = "*")
public class NpsCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(NpsCallbackController.class);

    @Autowired
    private NpsXmlDecryptionService xmlDecryptionService;


    @Autowired
    private NpsConfiguration npsConfig;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PaymentStatusTrackingService statusTrackingService;

    @Autowired
    private Acmt024XmlParser acmt024XmlParser;

    @Autowired
    private Pacs002XmlParser pacs002XmlParser;

    @Autowired
    private Pacs028XmlParser pacs028XmlParser;

    @Autowired
    private InboundPacs008Processor inboundPacs008Processor;

    /**
     * Handle incoming ACMT.023 (Identification Verification Request) from NIBSS
     * These are identification verification requests sent TO us by NIBSS
     */
    @PostMapping("/acmt023")
    @Auditable(action = "ACMT023_CALLBACK", resource = "IdentificationRequest", actionType = AuditLog.ActionType.API_CALL, message = "ACMT.023 identification verification request callback received from NIBSS")
    public ResponseEntity<String> handleAcmt023Callback(@RequestBody String encryptedXml) {
        logger.info("Received ACMT.023 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted ACMT.023 XML: {}", decryptedXml);
            
            // Log successful callback processing
            auditService.logSystemEvent(
                "ACMT023_CALLBACK_PROCESSED",
                "IdentificationRequest",
                "ACMT.023 identification verification request processed successfully from NIBSS",
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            // TODO: Parse and process ACMT.023 request
            // - Parse the decrypted XML (need Acmt023XmlParser)
            // - Process the identification verification request
            // - Generate and send ACMT.024 response back to NIBSS
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("ACMT.023 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.023 callback: {}", e.getMessage(), e);
            
            // Log callback processing error
            auditService.logError(
                "ACMT023_CALLBACK_FAILED",
                "IdentificationRequest",
                AuditLog.ActionType.API_CALL,
                null,
                "NIBSS",
                "Failed to process ACMT.023 callback from NIBSS: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing ACMT.023: " + e.getMessage());
        }
    }

    /**
     * Handle incoming ACMT.024 (Identification Verification Report) from NIBSS
     */
    @PostMapping("/acmt024")
    @Auditable(action = "ACMT024_CALLBACK", resource = "IdentificationReport", actionType = AuditLog.ActionType.API_CALL, message = "ACMT.024 identification verification report callback received from NIBSS")
    public ResponseEntity<String> handleAcmt024Callback(@RequestBody String encryptedXml) {
        logger.info("Received ACMT.024 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted ACMT.024 XML: {}", decryptedXml);
            
            // Parse the decrypted XML and extract relevant information
            Acmt024ResponseDto response = acmt024XmlParser.parseAcmt024Xml(decryptedXml);
            
            // Log successful callback processing
            auditService.logSystemEvent(
                "ACMT024_CALLBACK_PROCESSED",
                "IdentificationVerification",
                "ACMT.024 identification verification report processed successfully from NIBSS",
                Map.of(
                    "messageId", response.getMessageId(),
                    "accountNumber", response.getAccountNumber(),
                    "bankCode", response.getBankCode(),
                    "status", response.getStatus(),
                    "verified", response.getAccountVerified(),
                    "responseCode", response.getResponseCode()
                )
            );
            
            // Process the identification verification result
            processIdentificationVerificationResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("ACMT.024 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing ACMT.024 callback: {}", e.getMessage(), e);
            
            // Log callback processing error
            auditService.logError(
                "ACMT024_CALLBACK_FAILED",
                "IdentificationVerification",
                AuditLog.ActionType.API_CALL,
                null,
                "NIBSS",
                "Failed to process ACMT.024 callback from NIBSS: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing ACMT.024: " + e.getMessage());
        }
    }

    /**
     * Handle incoming PACS.002 (Payment Status Report) from NIBSS
     */
    @PostMapping("/pacs002")
    @Auditable(action = "PACS002_CALLBACK", resource = "PaymentStatusReport", actionType = AuditLog.ActionType.API_CALL, message = "PACS.002 payment status report callback received from NIBSS")
    public ResponseEntity<String> handlePacs002Callback(@RequestBody String encryptedXml) {
        logger.info("Received PACS.002 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted PACS.002 XML: {}", decryptedXml);
            
            // Parse the decrypted XML and extract relevant information
            Pacs002ResponseDto response = pacs002XmlParser.parsePacs002Xml(decryptedXml);
            
            // Update payment transaction status
            statusTrackingService.updatePaymentStatus(response.getOriginalMessageId(), response);
            
            // Log successful callback processing
            auditService.logSystemEvent(
                "PACS002_CALLBACK_PROCESSED",
                "PaymentStatusReport",
                "PACS.002 payment status report processed successfully from NIBSS",
                Map.of(
                    "originalMessageId", response.getOriginalMessageId(),
                    "status", response.getStatus(),
                    "responseCode", response.getResponseCode(),
                    "responseMessage", response.getResponseMessage()
                )
            );
            
            // Process the payment status result
            processPaymentStatusResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.002 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.002 callback: {}", e.getMessage(), e);
            
            // Log callback processing error
            auditService.logError(
                "PACS002_CALLBACK_FAILED",
                "PaymentStatusReport",
                AuditLog.ActionType.API_CALL,
                null,
                "NIBSS",
                "Failed to process PACS.002 callback from NIBSS: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.002: " + e.getMessage());
        }
    }

    /**
     * Handle incoming PACS.028 (Payment Status Request Response) from NIBSS
     */
    @PostMapping("/pacs028")
    @Auditable(action = "PACS028_CALLBACK", resource = "PaymentStatusRequest", actionType = AuditLog.ActionType.API_CALL, message = "PACS.028 payment status request response callback received from NIBSS")
    public ResponseEntity<String> handlePacs028Callback(@RequestBody String encryptedXml) {
        logger.info("Received PACS.028 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted PACS.028 XML: {}", decryptedXml);
            
            // Parse the decrypted XML and extract relevant information
            Pacs028ResponseDto response = pacs028XmlParser.parsePacs028Xml(decryptedXml);
            
            // Log successful callback processing
            auditService.logSystemEvent(
                "PACS028_CALLBACK_PROCESSED",
                "PaymentStatusRequest",
                "PACS.028 payment status request response processed successfully from NIBSS",
                Map.of(
                    "messageId", response.getMessageId(),
                    "originalMessageId", response.getOriginalMessageId(),
                    "responseCode", response.getResponseCode()
                )
            );
            
            // Process the payment status request result
            processPaymentStatusRequestResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.028 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.028 callback: {}", e.getMessage(), e);
            
            // Log callback processing error
            auditService.logError(
                "PACS028_CALLBACK_FAILED",
                "PaymentStatusRequest",
                AuditLog.ActionType.API_CALL,
                null,
                "NIBSS",
                "Failed to process PACS.028 callback from NIBSS: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.028: " + e.getMessage());
        }
    }

    /**
     * Handle incoming PACS.008 (Inbound Payment Request) from NIBSS
     * These are payment requests sent TO us by NIBSS (inbound payments)
     */
    @PostMapping("/pacs008")
    @Auditable(action = "PACS008_CALLBACK", resource = "PaymentRequest", actionType = AuditLog.ActionType.API_CALL, message = "PACS.008 inbound payment request callback received from NIBSS")
    public ResponseEntity<String> handlePacs008Callback(@RequestBody String encryptedXml) {
        logger.info("Received inbound PACS.008 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted PACS.008 XML: {}", decryptedXml);
            
            // Use InboundPacs008Processor for proper inbound payment processing
            // This handles: transaction creation, SQS queuing, client notifications, etc.
            inboundPacs008Processor.processInboundPacs008(decryptedXml);
            
            // Log successful callback processing
            auditService.logSystemEvent(
                "PACS008_CALLBACK_PROCESSED",
                "PaymentRequest",
                "PACS.008 inbound payment request processed successfully from NIBSS",
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.008 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 callback: {}", e.getMessage(), e);
            
            // Log callback processing error
            auditService.logError(
                "PACS008_CALLBACK_FAILED",
                "PaymentRequest",
                AuditLog.ActionType.API_CALL,
                null,
                "NIBSS",
                "Failed to process PACS.008 callback from NIBSS: " + e.getMessage(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                Map.of("encryptedXmlLength", encryptedXml != null ? encryptedXml.length() : 0)
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.008: " + e.getMessage());
        }
    }





    private void processIdentificationVerificationResult(Acmt024ResponseDto response) {
        logger.info("Processing identification verification result for message: {}, status: {}", 
            response.getMessageId(), response.getStatus());
        
        // Log detailed information
        logger.info("Original message  ID: {}", response.getOriginalMessageId());
        logger.info("Verification Result: {}", response.getVerificationResult());
        logger.info("Account Number: {}", response.getAccountNumber());
        logger.info("Account Name: {}", response.getAccountName());
        logger.info("Response Code: {}", response.getResponseCode());
        logger.info("Response Message: {}", response.getResponseMessage());
        logger.info("Verification Status: {}", response.getVerificationStatus());
        logger.info("Account Verified: {}", response.getAccountVerified());
        logger.info("Processed At: {}", response.getProcessedAt());
        logger.info("Created At: {}", response.getCreatedAt());
        
        // Log Supplementary Data
        logger.info("BVN: {}", response.getBvn());
        logger.info("Risk Rating: {}", response.getRiskRating());
        logger.info("Account Designation: {}", response.getAccountDesignation());
        logger.info("Account Tier: {}", response.getAccountTier());
        logger.info("ID Type: {}", response.getIdType());
        
        // Here you would integrate with your internal systems
        // For example, update a database, send notifications, etc.
        
        if ("SUCCESS".equals(response.getStatus())) {
            logger.info("Identification verification successful for account: {} - Name: {}", 
                response.getAccountNumber(), response.getAccountName());
            
            // TODO: Update your internal system with successful verification
            // - Update account verification status in database
            // - Send notification to requesting system
            // - Update audit logs
            
        } else if ("FAILED".equals(response.getStatus())) {
            logger.warn("Identification verification failed for account: {} - Name: {}", 
                response.getAccountNumber(), response.getAccountName());
            
            // TODO: Handle failed verification
            // - Update account verification status in database
            // - Send notification to requesting system
            // - Log for compliance/audit purposes
            
        } else {
            logger.warn("Identification verification status unknown for account: {}", response.getAccountNumber());
            
            // TODO: Handle unknown status
            // - Log for investigation
            // - Possibly retry or escalate
        }
    }

    private void processPaymentStatusResult(Pacs002ResponseDto response) {
        logger.info("Processing payment status result for message: {}, status: {}", 
            response.getMessageId(), response.getStatus());
        
        // Log detailed information
        logger.info("Original Message ID: {}", response.getOriginalMessageId());
        logger.info("Payment Approved: {}", response.getPaymentApproved());
        logger.info("Status ID: {}", response.getStatusId());
        logger.info("Reason Code: {}", response.getReasonCode());
        logger.info("Additional Information: {}", response.getAdditionalInformation());
        logger.info("Settlement Date: {}", response.getSettlementDate());
        logger.info("Response Code: {}", response.getResponseCode());
        logger.info("Response Message: {}", response.getResponseMessage());
        
        // Log Agent Information
        logger.info("--- Agent Information ---");
        logger.info("Instructing Agent BICFI: {}", response.getInstgAgentBicfi());
        logger.info("Instructing Agent Member ID: {}", response.getInstgAgentMemberId());
        logger.info("Instructed Agent BICFI: {}", response.getInstdAgentBicfi());
        logger.info("Instructed Agent Member ID: {}", response.getInstdAgentMemberId());
        
        // Log Original Message Information
        logger.info("--- Original Message Information ---");
        logger.info("Original Message Name ID: {}", response.getOriginalMessageNameId());
        logger.info("Original Creation DateTime: {}", response.getOriginalCreationDateTime());
        logger.info("Creation DateTime: {}", response.getCreationDateTime());
        
        // Here you would integrate with your internal systems
        // For example, update payment records, send notifications, etc.
        
        if (Boolean.TRUE.equals(response.getPaymentApproved())) {
            logger.info("Payment approved and settled for original message: {}", response.getOriginalMessageId());
            
            // TODO: Update your internal system with successful payment
            // - Update payment status to APPROVED in database
            // - Send notification to requesting system
            // - Update account balances
            // - Generate settlement reports
            
        } else if (Boolean.FALSE.equals(response.getPaymentApproved())) {
            logger.warn("Payment declined for original message: {} - Reason: {}", 
                response.getOriginalMessageId(), response.getAdditionalInformation());
            
            // TODO: Handle declined payment
            // - Update payment status to DECLINED in database
            // - Send notification to requesting system
            // - Log for compliance/audit purposes
            // - Handle refund if applicable
            
            // Check specific decline reasons
            if (response.getAdditionalInformation() != null) {
                if (response.getAdditionalInformation().contains("timeout")) {
                    logger.error("Payment timed out - no response from creditor bank");
                    // Handle timeout scenario
                } else if (response.getAdditionalInformation().contains("Wrong account number")) {
                    logger.error("Payment declined - invalid account number");
                    // Handle invalid account scenario
                }
            }
            
        } else {
            logger.warn("Payment status unknown for original message: {}", response.getOriginalMessageId());
            
            // TODO: Handle unknown status
            // - Log for investigation
            // - Possibly retry or escalate
            // - Contact NIBSS support if needed
        }
    }

    private void processPaymentStatusRequestResult(Pacs028ResponseDto response) {
        logger.info("Processing payment status request result for message: {}", response.getMessageId());
        
        // Log detailed information
        logger.info("Original Message ID: {}", response.getOriginalMessageId());
        logger.info("Status Request ID: {}", response.getStatusRequestId());
        logger.info("Original Transaction ID: {}", response.getOriginalTransactionId());
        logger.info("Settlement Date: {}", response.getSettlementDate());
        logger.info("Response Code: {}", response.getResponseCode());
        logger.info("Response Message: {}", response.getResponseMessage());
        
        // Here you would integrate with your internal systems
        // For example, update status request records, send notifications, etc.
        
        logger.info("Payment status request processed for original message: {}", response.getOriginalMessageId());
        
        // TODO: Handle payment status request
        // - Look up the original payment (PACS.008) in your database
        // - Check the current status of the payment
        // - Generate appropriate PACS.002 response message
        // - Send PACS.002 response back to requesting institution
        // - Update status request tracking records
        // - Send notification to relevant parties
        
        // Example integration points:
        // 1. Database lookup: Find original payment by originalMessageId
        // 2. Status check: Determine current payment status
        // 3. Response generation: Create PACS.002 status report
        // 4. Response sending: Send PACS.002 to requesting institution
        // 5. Audit logging: Log the status request for compliance
        
        logger.info("Payment status request processing completed for message: {}", response.getMessageId());
    }
}
