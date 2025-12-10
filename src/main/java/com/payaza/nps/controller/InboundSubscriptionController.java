package com.payaza.nps.controller;

import com.payaza.nps.model.InboundClientSubscription;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.service.InboundClientSubscriptionService;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.dto.InboundSubscriptionRequestDto;
import com.payaza.nps.dto.InboundSubscriptionResponseDto;
import com.payaza.nps.dto.QueueStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Admin Controller for managing inbound client subscriptions
 */
@RestController
@RequestMapping("/admin/inbound-subscriptions")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class InboundSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(InboundSubscriptionController.class);

    @Autowired
    private InboundClientSubscriptionService subscriptionService;

    @Autowired
    private AuditService auditService;

    /**
     * Get current inbound PACS.008 subscriber
     */
    @GetMapping("/pacs008/current")
    public ResponseEntity<InboundSubscriptionResponseDto> getCurrentInboundPacs008Subscriber() {
        try {
            Optional<InboundClientSubscription> subscription = subscriptionService
                .getCurrentInboundPacs008Subscription();
            
            if (subscription.isPresent()) {
                InboundSubscriptionResponseDto response = new InboundSubscriptionResponseDto();
                response.setClientId(subscription.get().getClientId());
                response.setMessageType(subscription.get().getMessageType());
                response.setActive(subscription.get().getActive());
                response.setCreatedAt(subscription.get().getCreatedAt());
                response.setCreatedBy(subscription.get().getCreatedBy());
                response.setUpdatedAt(subscription.get().getUpdatedAt());
                response.setUpdatedBy(subscription.get().getUpdatedBy());
                response.setNotes(subscription.get().getNotes());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving current inbound PACS.008 subscriber: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Subscribe a client to inbound PACS.008 notifications
     */
    @PostMapping("/pacs008/subscribe")
    public ResponseEntity<String> subscribeClientToInboundPacs008(
            @Valid @RequestBody InboundSubscriptionRequestDto request) {
        try {
            // Get current user from security context (placeholder)
            String currentUser = "admin"; // TODO: Get from SecurityContext
            
            subscriptionService.subscribeClientToInboundPacs008(request.getClientId(), currentUser);
            
            // Log admin action
            auditService.logAdminAction("SUBSCRIBE_INBOUND_PACS008", 
                "Client " + request.getClientId() + " subscribed to inbound PACS.008 notifications", 
                currentUser, AuditLog.ActionType.CREATE, "Client subscription created", request);
            
            logger.info("Client {} subscribed to inbound PACS.008 notifications by {}", 
                request.getClientId(), currentUser);
            
            return ResponseEntity.ok("Client subscribed successfully");
            
        } catch (Exception e) {
            logger.error("Error subscribing client to inbound PACS.008: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error subscribing client: " + e.getMessage());
        }
    }

    /**
     * Unsubscribe current client from inbound PACS.008 notifications
     */
    @PostMapping("/pacs008/unsubscribe")
    public ResponseEntity<String> unsubscribeFromInboundPacs008() {
        try {
            // Get current user from security context (placeholder)
            String currentUser = "admin"; // TODO: Get from SecurityContext
            
            subscriptionService.unsubscribeFromInboundPacs008(currentUser);
            
            // Log admin action
            auditService.logAdminAction("UNSUBSCRIBE_INBOUND_PACS008", 
                "Unsubscribed from inbound PACS.008 notifications", 
                currentUser, AuditLog.ActionType.DELETE, "Client subscription removed", null);
            
            logger.info("Unsubscribed from inbound PACS.008 notifications by {}", currentUser);
            
            return ResponseEntity.ok("Unsubscribed successfully");
            
        } catch (Exception e) {
            logger.error("Error unsubscribing from inbound PACS.008: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error unsubscribing: " + e.getMessage());
        }
    }

    /**
     * Get subscription history for inbound PACS.008
     */
    @GetMapping("/pacs008/history")
    public ResponseEntity<List<InboundSubscriptionResponseDto>> getInboundPacs008SubscriptionHistory() {
        try {
            List<InboundClientSubscription> subscriptions = subscriptionService
                .getInboundPacs008SubscriptionHistory();
            
            List<InboundSubscriptionResponseDto> responses = subscriptions.stream()
                .map(subscription -> {
                    InboundSubscriptionResponseDto response = new InboundSubscriptionResponseDto();
                    response.setClientId(subscription.getClientId());
                    response.setMessageType(subscription.getMessageType());
                    response.setActive(subscription.getActive());
                    response.setCreatedAt(subscription.getCreatedAt());
                    response.setCreatedBy(subscription.getCreatedBy());
                    response.setUpdatedAt(subscription.getUpdatedAt());
                    response.setUpdatedBy(subscription.getUpdatedBy());
                    response.setNotes(subscription.getNotes());
                    return response;
                })
                .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error retrieving subscription history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if a client is subscribed to inbound PACS.008
     */
    @GetMapping("/pacs008/check/{clientId}")
    public ResponseEntity<Boolean> isClientSubscribedToInboundPacs008(@PathVariable String clientId) {
        try {
            boolean isSubscribed = subscriptionService.isClientSubscribedToInboundPacs008(clientId);
            return ResponseEntity.ok(isSubscribed);
            
        } catch (Exception e) {
            logger.error("Error checking client subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get queue status for monitoring
     */
    @GetMapping("/queue-status")
    public ResponseEntity<QueueStatusDto> getQueueStatus() {
        try {
            // TODO: Implement actual queue status retrieval from SQS
            QueueStatusDto status = new QueueStatusDto();
            status.setInboundQueueSize(0L); // Placeholder
            status.setErrorQueueSize(0L); // Placeholder
            status.setOutboundQueueSize(0L); // Placeholder
            status.setLastCheckedAt(java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Error retrieving queue status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
