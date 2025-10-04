package com.payaza.nps.service;

import com.payaza.nps.model.iso20022.PaymentStatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for handling NPS payment timeouts
 * Implements timeout handling for payment responses as per NPS specifications
 */
@Service
public class NpsTimeoutService {

    private static final Logger logger = LoggerFactory.getLogger(NpsTimeoutService.class);
    
    // Timeout configuration (in minutes)
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;
    private static final int LATE_RESPONSE_TIMEOUT_MINUTES = 60;

    @Autowired
    private NpsPaymentService paymentService;

    // Track pending payments for timeout monitoring
    private final ConcurrentMap<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    /**
     * Register a pending payment for timeout monitoring
     */
    public void registerPendingPayment(String messageId, String endToEndId, String debtorBankId, String creditorBankId) {
        PendingPayment pendingPayment = new PendingPayment(
                messageId, endToEndId, debtorBankId, creditorBankId, LocalDateTime.now());
        pendingPayments.put(messageId, pendingPayment);
        
        logger.info("Registered pending payment for timeout monitoring: {} (EndToEndId: {})", 
                   messageId, endToEndId);
    }

    /**
     * Remove pending payment from timeout monitoring (when response received)
     */
    public void removePendingPayment(String messageId) {
        PendingPayment removed = pendingPayments.remove(messageId);
        if (removed != null) {
            logger.info("Removed pending payment from timeout monitoring: {} (EndToEndId: {})", 
                       messageId, removed.getEndToEndId());
        }
    }

    /**
     * Scheduled task to monitor payment timeouts
     * Runs every 5 minutes to check for timed out payments
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void monitorPaymentTimeouts() {
        logger.debug("Starting payment timeout monitoring task");

        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
            
            pendingPayments.entrySet().removeIf(entry -> {
                String messageId = entry.getKey();
                PendingPayment pendingPayment = entry.getValue();
                
                if (pendingPayment.getCreatedAt().isBefore(timeoutThreshold)) {
                    logger.warn("Payment timeout detected: {} (EndToEndId: {})", 
                               messageId, pendingPayment.getEndToEndId());
                    
                    // Handle timeout scenario
                    handlePaymentTimeout(pendingPayment);
                    return true; // Remove from map
                }
                return false; // Keep in map
            });

        } catch (Exception e) {
            logger.error("Error during payment timeout monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment timeout scenario
     * This implements the "Timeout Payment - Response Not Received" flow
     */
    private void handlePaymentTimeout(PendingPayment pendingPayment) {
        logger.info("Handling payment timeout for message: {} (EndToEndId: {})", 
                   pendingPayment.getMessageId(), pendingPayment.getEndToEndId());

        try {
            // Create timeout status report (pacs.002/RJCT)
            PaymentStatusReport timeoutReport = createTimeoutStatusReport(pendingPayment);
            
            // Send timeout notification to both banks
            sendTimeoutNotificationToDebtorBank(timeoutReport, pendingPayment);
            sendTimeoutNotificationToCreditorBank(timeoutReport, pendingPayment);
            
            logger.info("Payment timeout handled successfully for message: {}", 
                       pendingPayment.getMessageId());

        } catch (Exception e) {
            logger.error("Error handling payment timeout for message {}: {}", 
                        pendingPayment.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Create timeout status report (pacs.002/RJCT)
     */
    private PaymentStatusReport createTimeoutStatusReport(PendingPayment pendingPayment) {
        PaymentStatusReport report = new PaymentStatusReport();
        PaymentStatusReport.FIToFIPmtStsRpt fiToFIPmtStsRpt = new PaymentStatusReport.FIToFIPmtStsRpt();

        // Set group header
        PaymentStatusReport.GroupHeader53 grpHdr = new PaymentStatusReport.GroupHeader53();
        grpHdr.setMsgId(generateTimeoutMessageId());
        grpHdr.setCreDtTm(LocalDateTime.now().toString());
        grpHdr.setOrgnlMsgId(pendingPayment.getMessageId());
        grpHdr.setOrgnlMsgNmId("pacs.008.001.08");
        fiToFIPmtStsRpt.setGrpHdr(grpHdr);

        // Set original group information
        PaymentStatusReport.OriginalGroupInformation29 orgnlGrpInf = new PaymentStatusReport.OriginalGroupInformation29();
        orgnlGrpInf.setOrgnlMsgId(pendingPayment.getMessageId());
        orgnlGrpInf.setOrgnlMsgNmId("pacs.008.001.08");
        orgnlGrpInf.setGrpSts(PaymentStatusReport.GroupStatus3Code.RJCT);
        fiToFIPmtStsRpt.setOrgnlGrpInfAndSts(orgnlGrpInf);

        // Set transaction information with timeout status
        PaymentStatusReport.PaymentTransaction110 txInf = new PaymentStatusReport.PaymentTransaction110();
        txInf.setStsId(generateTimeoutStatusId());
        txInf.setOrgnlEndToEndId(pendingPayment.getEndToEndId());
        txInf.setTxSts(PaymentStatusReport.TransactionIndividualStatus3Code.RJCT);
        txInf.setAccptncDtTm(LocalDateTime.now().toString());
        
        // Set timeout reason
        PaymentStatusReport.StatusReasonInformation12 stsRsnInf = new PaymentStatusReport.StatusReasonInformation12();
        PaymentStatusReport.StatusReason6Choice rsn = new PaymentStatusReport.StatusReason6Choice();
        rsn.setPrtry("TIMEOUT");
        stsRsnInf.setRsn(rsn);
        stsRsnInf.getAddtlInf().add("Payment timeout - no response received within " + PAYMENT_TIMEOUT_MINUTES + " minutes");
        txInf.getStsRsnInf().add(stsRsnInf);
        
        fiToFIPmtStsRpt.getTxInfAndSts().add(txInf);
        report.setFiToFIPmtStsRpt(fiToFIPmtStsRpt);

        return report;
    }

    /**
     * Send timeout notification to debtor bank
     */
    private void sendTimeoutNotificationToDebtorBank(PaymentStatusReport timeoutReport, PendingPayment pendingPayment) {
        logger.info("Sending timeout notification to debtor bank: {}", pendingPayment.getDebtorBankId());
        // Implementation would send the timeout report to the debtor bank
        // This could be via callback URL, message queue, or direct API call
    }

    /**
     * Send timeout notification to creditor bank
     */
    private void sendTimeoutNotificationToCreditorBank(PaymentStatusReport timeoutReport, PendingPayment pendingPayment) {
        logger.info("Sending timeout notification to creditor bank: {}", pendingPayment.getCreditorBankId());
        // Implementation would send the timeout report to the creditor bank
        // This could be via callback URL, message queue, or direct API call
    }

    /**
     * Handle late response scenario
     * This implements the "Timeout Payment - Late Response Received" flow
     */
    public void handleLateResponse(String originalMessageId, PaymentStatusReport lateResponse) {
        logger.info("Handling late response for message: {}", originalMessageId);

        try {
            // Check if this is a late response for a timed-out payment
            if (pendingPayments.containsKey(originalMessageId)) {
                logger.warn("Late response received for already timed-out payment: {}", originalMessageId);
                
                // Process the late response but mark it as late
                paymentService.processPaymentStatusReport(lateResponse);
                
                // Send late response notification
                sendLateResponseNotification(lateResponse, originalMessageId);
                
                // Remove from pending payments
                removePendingPayment(originalMessageId);
                
            } else {
                // Normal response processing
                paymentService.processPaymentStatusReport(lateResponse);
            }

        } catch (Exception e) {
            logger.error("Error handling late response for message {}: {}", 
                        originalMessageId, e.getMessage(), e);
        }
    }

    /**
     * Send late response notification
     */
    private void sendLateResponseNotification(PaymentStatusReport lateResponse, String originalMessageId) {
        logger.warn("Late response notification for message: {} - Response received after timeout", 
                   originalMessageId);
        // Implementation would send notification about late response
    }

    /**
     * Get pending payments count for monitoring
     */
    public int getPendingPaymentsCount() {
        return pendingPayments.size();
    }

    /**
     * Get pending payment information
     */
    public PendingPayment getPendingPayment(String messageId) {
        return pendingPayments.get(messageId);
    }

    /**
     * Generate timeout message ID
     */
    private String generateTimeoutMessageId() {
        return "TIMEOUT_MSG" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Generate timeout status ID
     */
    private String generateTimeoutStatusId() {
        return "TIMEOUT_STAT" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Data class for tracking pending payments
     */
    public static class PendingPayment {
        private String messageId;
        private String endToEndId;
        private String debtorBankId;
        private String creditorBankId;
        private LocalDateTime createdAt;

        public PendingPayment(String messageId, String endToEndId, String debtorBankId, String creditorBankId, LocalDateTime createdAt) {
            this.messageId = messageId;
            this.endToEndId = endToEndId;
            this.debtorBankId = debtorBankId;
            this.creditorBankId = creditorBankId;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getEndToEndId() { return endToEndId; }
        public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
        
        public String getDebtorBankId() { return debtorBankId; }
        public void setDebtorBankId(String debtorBankId) { this.debtorBankId = debtorBankId; }
        
        public String getCreditorBankId() { return creditorBankId; }
        public void setCreditorBankId(String creditorBankId) { this.creditorBankId = creditorBankId; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
