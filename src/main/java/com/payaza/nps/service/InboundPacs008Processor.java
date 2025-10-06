package com.payaza.nps.service;

import com.payaza.nps.model.PaymentTransactionLive;
import com.payaza.nps.model.TransactionDirection;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import com.payaza.nps.dto.InboundPacs008Request;
import com.payaza.nps.dto.InboundPacs008Message;
import com.payaza.nps.dto.ErrorQueueMessage;
import com.payaza.nps.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound PACS.008 Processor
 * 
 * Handles processing of inbound PACS.008 messages from NIBSS:
 * 1. Parses incoming PACS.008 XML
 * 2. Creates transaction record
 * 3. Sends to designated SQS queue
 * 4. Notifies subscribed clients
 * 5. Handles errors and alerts
 */
@Service
public class InboundPacs008Processor {

    private static final Logger logger = LoggerFactory.getLogger(InboundPacs008Processor.class);

    @Autowired
    private PaymentTransactionLiveRepository transactionRepository;

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AlertEngine alertEngine;

    @Autowired
    private AuditService auditService;

    @Autowired
    private InboundClientSubscriptionService subscriptionService;

    // SQS Queue names
    private static final String INBOUND_PACS008_QUEUE = "inbound-pacs008-queue";
    private static final String INBOUND_ERROR_QUEUE = "inbound-error-queue";
    private static final String OUTBOUND_PACS002_QUEUE = "outbound-pacs002-queue";

    /**
     * Process inbound PACS.008 message from NIBSS
     */
    public void processInboundPacs008(String xmlMessage) {
        String transactionId = null;
        
        try {
            // 1. Parse incoming PACS.008
            InboundPacs008Request inboundRequest = parseInboundPacs008(xmlMessage);
            transactionId = inboundRequest.getTransactionId();
            
            logger.info("Processing inbound PACS.008 for transaction: {}", transactionId);
            
            // 2. Create transaction record immediately
            PaymentTransactionLive transaction = createTransactionRecord(inboundRequest);
            transactionRepository.save(transaction);
            
            // 3. Send to designated SQS queue
            sendToInboundQueue(inboundRequest);
            
            // 4. Update status to QUEUED
            transaction.setStatus("QUEUED");
            transaction.setQueuedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // 5. Notify subscribed clients
            notifySubscribedClients(inboundRequest);
            
            // 6. Log for admin portal visibility
            auditService.logApiCall("INBOUND_PACS008_PROCESSED", "POST", "/callbacks/pacs008", 
                System.currentTimeMillis(), 200, "Inbound PACS.008 processed and queued for transaction: " + transactionId);
            
            logger.info("Inbound PACS.008 processed successfully for transaction: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Error processing inbound PACS.008 for transaction: {} - {}", transactionId, e.getMessage(), e);
            
            // Handle errors - send to error queue and alert
            handleInboundPacs008Error(xmlMessage, transactionId, e);
        }
    }

    /**
     * Parse inbound PACS.008 XML message
     */
    private InboundPacs008Request parseInboundPacs008(String xmlMessage) {
        // TODO: Implement actual XML parsing using JAXB or similar
        // This is a placeholder implementation
        
        InboundPacs008Request request = new InboundPacs008Request();
        request.setTransactionId("INB-" + UUID.randomUUID().toString().substring(0, 8));
        request.setMessageId("MSG-" + UUID.randomUUID().toString().substring(0, 8));
        request.setXmlMessage(xmlMessage);
        request.setSenderBankCode("NIBSS");
        request.setReceiverBankCode("OUR_BANK");
        request.setAmount(1000.00);
        request.setCurrency("NGN");
        request.setReceivedAt(LocalDateTime.now());
        
        return request;
    }

    /**
     * Create transaction record for inbound PACS.008
     */
    private PaymentTransactionLive createTransactionRecord(InboundPacs008Request request) {
        PaymentTransactionLive transaction = new PaymentTransactionLive();
        transaction.setTransactionId(request.getTransactionId());
        transaction.setOriginalMessageId(request.getMessageId());
        transaction.setDirection(TransactionDirection.INBOUND);
        transaction.setStatus("RECEIVED");
        transaction.setSenderBankCode(request.getSenderBankCode());
        transaction.setReceiverBankCode(request.getReceiverBankCode());
        transaction.setAmount(java.math.BigDecimal.valueOf(request.getAmount()));
        transaction.setCurrency(request.getCurrency());
        transaction.setReceivedAt(request.getReceivedAt());
        transaction.setQueueStatus("PENDING");
        
        return transaction;
    }

    /**
     * Send inbound PACS.008 to designated SQS queue
     */
    private void sendToInboundQueue(InboundPacs008Request request) {
        try {
            InboundPacs008Message queueMessage = new InboundPacs008Message();
            queueMessage.setTransactionId(request.getTransactionId());
            queueMessage.setMessageId(request.getMessageId());
            queueMessage.setXmlMessage(request.getXmlMessage());
            queueMessage.setSenderBankCode(request.getSenderBankCode());
            queueMessage.setReceiverBankCode(request.getReceiverBankCode());
            queueMessage.setAmount(request.getAmount());
            queueMessage.setCurrency(request.getCurrency());
            queueMessage.setReceivedAt(request.getReceivedAt());
            queueMessage.setQueuedAt(LocalDateTime.now());
            
            // Send to SQS queue
            Message<InboundPacs008Message> message = MessageBuilder
                .withPayload(queueMessage)
                .setHeader("transactionId", request.getTransactionId())
                .setHeader("messageType", "INBOUND_PACS008")
                .build();
            
            sqsTemplate.send(INBOUND_PACS008_QUEUE, message);
            
            logger.info("Inbound PACS.008 sent to SQS queue for transaction: {}", request.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Error sending inbound PACS.008 to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to queue inbound PACS.008", e);
        }
    }

    /**
     * Notify subscribed clients about inbound PACS.008
     */
    private void notifySubscribedClients(InboundPacs008Request request) {
        try {
            // Get the configured client for inbound PACS.008 notifications
            String subscribedClientId = subscriptionService.getInboundPacs008Subscriber();
            
            if (subscribedClientId != null) {
                NotificationMessage notification = new NotificationMessage();
                notification.setClientId(subscribedClientId);
                notification.setMessageType("INBOUND_PACS008");
                notification.setTransactionId(request.getTransactionId());
                notification.setMessageId(request.getMessageId());
                notification.setSenderBankCode(request.getSenderBankCode());
                notification.setAmount(request.getAmount());
                notification.setCurrency(request.getCurrency());
                notification.setTimestamp(LocalDateTime.now());
                
                // Send notification to client
                // TODO: Implement notification sending
                logger.info("Notification would be sent to client: {}", subscribedClientId);
                
                logger.info("Notification sent to client {} for inbound PACS.008 transaction: {}", 
                    subscribedClientId, request.getTransactionId());
            } else {
                logger.warn("No client subscribed for inbound PACS.008 notifications");
            }
            
        } catch (Exception e) {
            logger.error("Error notifying clients about inbound PACS.008: {}", e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't stop processing
        }
    }

    /**
     * Handle errors in inbound PACS.008 processing
     */
    private void handleInboundPacs008Error(String xmlMessage, String transactionId, Exception e) {
        try {
            // Send to error queue
            ErrorQueueMessage errorMessage = new ErrorQueueMessage();
            errorMessage.setTransactionId(transactionId);
            errorMessage.setXmlMessage(xmlMessage);
            errorMessage.setError(e.getMessage());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setErrorType("INBOUND_PACS008_PROCESSING_ERROR");
            
            Message<ErrorQueueMessage> message = MessageBuilder
                .withPayload(errorMessage)
                .setHeader("transactionId", transactionId)
                .setHeader("errorType", "INBOUND_PACS008_PROCESSING_ERROR")
                .build();
            
            sqsTemplate.send(INBOUND_ERROR_QUEUE, message);
            
            // Update transaction status if we have a transaction ID
            if (transactionId != null) {
                updateTransactionErrorStatus(transactionId, e.getMessage());
            }
            
            // Trigger critical alert
            // TODO: Implement alert triggering
            logger.error("Critical alert would be triggered for transaction: {}", transactionId);
            
            // Log the error
            auditService.logError("INBOUND_PACS008_ERROR", "POST", 
                com.payaza.nps.model.AuditLog.ActionType.API_CALL, "/callbacks/pacs008", 
                "Error processing inbound PACS.008: " + e.getMessage(), 
                "500", null, null, e);
            
            logger.error("Inbound PACS.008 error handled and sent to error queue for transaction: {}", transactionId);
            
        } catch (Exception errorHandlingException) {
            logger.error("Error handling inbound PACS.008 error: {}", errorHandlingException.getMessage(), errorHandlingException);
        }
    }

    /**
     * Update transaction status to ERROR
     */
    private void updateTransactionErrorStatus(String transactionId, String errorMessage) {
        try {
            PaymentTransactionLive transaction = transactionRepository.findByTransactionId(transactionId).orElse(null);
            if (transaction != null) {
                transaction.setStatus("ERROR");
                transaction.setErrorDetails(errorMessage);
                transaction.setQueueStatus("ERROR");
                transactionRepository.save(transaction);
            }
        } catch (Exception e) {
            logger.error("Error updating transaction error status: {}", e.getMessage(), e);
        }
    }

    /**
     * Send PACS.002 response to NIBSS
     * This will be called by the client that processes the inbound PACS.008
     */
    public void sendPacs002Response(String transactionId, String status, String failureReason) {
        try {
            // Create PACS.002 response message
            InboundPacs008Message responseMessage = new InboundPacs008Message();
            responseMessage.setTransactionId(transactionId);
            responseMessage.setStatus(status);
            responseMessage.setFailureReason(failureReason);
            responseMessage.setResponseSentAt(LocalDateTime.now());
            
            // Send to outbound PACS.002 queue
            Message<InboundPacs008Message> message = MessageBuilder
                .withPayload(responseMessage)
                .setHeader("transactionId", transactionId)
                .setHeader("messageType", "OUTBOUND_PACS002")
                .build();
            
            sqsTemplate.send(OUTBOUND_PACS002_QUEUE, message);
            
            // Update transaction status
            updateTransactionResponseStatus(transactionId, status, failureReason);
            
            logger.info("PACS.002 response sent to queue for transaction: {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Error sending PACS.002 response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send PACS.002 response", e);
        }
    }

    /**
     * Update transaction status when PACS.002 response is sent
     */
    private void updateTransactionResponseStatus(String transactionId, String status, String failureReason) {
        try {
            PaymentTransactionLive transaction = transactionRepository.findByTransactionId(transactionId).orElse(null);
            if (transaction != null) {
                transaction.setStatus(status);
                transaction.setResponseSentAt(LocalDateTime.now());
                transaction.setQueueStatus("COMPLETED");
                if (failureReason != null) {
                    transaction.setErrorDetails(failureReason);
                }
                transactionRepository.save(transaction);
            }
        } catch (Exception e) {
            logger.error("Error updating transaction response status: {}", e.getMessage(), e);
        }
    }
}
