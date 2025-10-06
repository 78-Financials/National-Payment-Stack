package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.NpsXmlDecryptionService;
import com.payaza.nps.service.NpsXmlSignatureService;
import com.payaza.nps.service.NpsXmlEncryptionService;
import com.payaza.nps.service.Acmt024XmlParser;
import com.payaza.nps.service.Pacs002XmlParser;
import com.payaza.nps.service.Pacs008XmlParser;
import com.payaza.nps.service.Pacs028XmlParser;
import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * NPS Callback Controller
 * 
 * Handles incoming asynchronous messages from NIBSS:
 * - ACMT.024 (Identification Verification Report)
 * - PACS.002 (Payment Status Report)
 * - PACS.028 (Payment Status Request Response)
 * 
 * All incoming messages are:
 * 1. Decrypted using our private key
 * 2. Signature verified using NIBSS public key
 * 3. Converted to JSON for internal processing
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
    private Acmt024XmlParser acmt024XmlParser;

    @Autowired
    private Pacs002XmlParser pacs002XmlParser;

    @Autowired
    private Pacs008XmlParser pacs008XmlParser;

    @Autowired
    private Pacs028XmlParser pacs028XmlParser;

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
                    "verified", response.isAccountVerified(),
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
            
            // Process the payment status result
            processPaymentStatusResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.002 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.002 callback: {}", e.getMessage(), e);
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
            
            // Process the payment status request result
            processPaymentStatusRequestResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.028 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.028 callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing PACS.028: " + e.getMessage());
        }
    }

    /**
     * Handle incoming PACS.008 (Payment Request Response) from NIBSS
     */
    @PostMapping("/pacs008")
    @Auditable(action = "PACS008_CALLBACK", resource = "PaymentRequest", actionType = AuditLog.ActionType.API_CALL, message = "PACS.008 payment request callback received from NIBSS")
    public ResponseEntity<String> handlePacs008Callback(@RequestBody String encryptedXml) {
        logger.info("Received PACS.008 callback from NIBSS");
        
        try {
            // Decrypt and verify the incoming message
            String decryptedXml = xmlDecryptionService.decryptAndVerifyXmlDocument(
                encryptedXml, 
                npsConfig.getPrivateKey(), 
                npsConfig.getNpsPublicKey()
            );
            
            logger.debug("Decrypted PACS.008 XML: {}", decryptedXml);
            
            // Parse the decrypted XML and extract relevant information
            Pacs008ResponseDto response = pacs008XmlParser.parsePacs008Xml(decryptedXml);
            
            // Process the payment request result
            processPaymentRequestResult(response);
            
            // Return acknowledgment to NIBSS
            return ResponseEntity.ok("PACS.008 received and processed successfully");
            
        } catch (Exception e) {
            logger.error("Error processing PACS.008 callback: {}", e.getMessage(), e);
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

    private void processPaymentRequestResult(Pacs008ResponseDto response) {
        logger.info("Processing payment request result for message: {}, transaction: {}", 
            response.getMessageId(), response.getTransactionId());
        
        // Log Payment Information
        logger.info("--- Payment Information ---");
        logger.info("Message ID: {}", response.getMessageId());
        logger.info("Instruction ID: {}", response.getInstructionId());
        logger.info("End-to-End ID: {}", response.getEndToEndId());
        logger.info("Transaction ID: {}", response.getTransactionId());
        logger.info("Amount: {} {}", response.getAmount(), response.getCurrency());
        logger.info("Settlement Date: {}", response.getSettlementDate());
        logger.info("Charge Bearer: {}", response.getChargeBearer());
        logger.info("Batch Booking: {}", response.getBatchBooking());
        logger.info("Number of Transactions: {}", response.getNumberOfTransactions());
        logger.info("Settlement Method: {}", response.getSettlementMethod());
        logger.info("Creation DateTime: {}", response.getCreationDateTime());
        
        // Log Agent Information
        logger.info("--- Agent Information ---");
        logger.info("Instructing Agent BICFI: {}", response.getInstgAgentBicfi());
        logger.info("Instructing Agent Member ID: {}", response.getInstgAgentMemberId());
        logger.info("Instructed Agent BICFI: {}", response.getInstdAgentBicfi());
        logger.info("Instructed Agent Member ID: {}", response.getInstdAgentMemberId());
        logger.info("Debtor Agent Member ID: {}", response.getDbtrAgentMemberId());
        logger.info("Creditor Agent Member ID: {}", response.getCdtrAgentMemberId());
        
        // Log Party Information
        logger.info("--- Party Information ---");
        logger.info("Sender Account Name: {}", response.getSenderAccountName());
        logger.info("Sender Account Number: {}", response.getSenderAccountNumber());
        logger.info("Receiver Account Name: {}", response.getReceiverAccountName());
        logger.info("Receiver Account Number: {}", response.getReceiverAccountNumber());
        
        // Log Payment Type Information
        logger.info("--- Payment Type Information ---");
        logger.info("Clearing Channel: {}", response.getClearingChannel());
        logger.info("Service Level: {}", response.getServiceLevel());
        logger.info("Local Instrument: {}", response.getLocalInstrument());
        logger.info("Category Purpose: {}", response.getCategoryPurpose());
        
        // Log Instructions and Remittance
        logger.info("--- Instructions and Remittance ---");
        logger.info("Instructions for Next Agent: {}", response.getInstructionsForNextAgent());
        logger.info("Remittance Information: {}", response.getRemittanceInformation());
        
        // Log Supplementary Data
        logger.info("--- Supplementary Data ---");
        logger.info("Debtor BVN: {}", response.getDebtorBvn());
        logger.info("Debtor Account Designation: {}", response.getDebtorAccountDesignation());
        logger.info("Debtor Account Tier: {}", response.getDebtorAccountTier());
        logger.info("Creditor BVN: {}", response.getCreditorBvn());
        logger.info("Creditor Account Designation: {}", response.getCreditorAccountDesignation());
        logger.info("Creditor Account Tier: {}", response.getCreditorAccountTier());
        logger.info("Transaction Location: {}", response.getTransactionLocation());
        logger.info("Name Enquiry Message ID: {}", response.getNameEnquiryMsgId());
        logger.info("Channel Code: {}", response.getChannelCode());
        logger.info("Risk Rating: {}", response.getRiskRating());
        
        // Here you would integrate with your internal systems
        // For example, update payment records, send notifications, etc.
        logger.info("Payment request received and processed successfully");
    }
}
