package com.payaza.nps.service;

import com.payaza.nps.model.InboundClientSubscription;
import com.payaza.nps.repository.InboundClientSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Inbound Client Subscription Service
 * 
 * Manages client subscriptions for inbound PACS.008 notifications.
 * Only one client can be subscribed at a time for inbound PACS.008 notifications.
 */
@Service
public class InboundClientSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(InboundClientSubscriptionService.class);

    @Autowired
    private InboundClientSubscriptionRepository subscriptionRepository;

    /**
     * Get the currently subscribed client for inbound PACS.008 notifications
     */
    @Cacheable(value = "inboundPacs008Subscriber", key = "'current'")
    public String getInboundPacs008Subscriber() {
        try {
            Optional<InboundClientSubscription> subscription = subscriptionRepository
                .findByMessageTypeAndActiveTrue("INBOUND_PACS008");
            
            if (subscription.isPresent()) {
                String clientId = subscription.get().getClientId();
                logger.debug("Current inbound PACS.008 subscriber: {}", clientId);
                return clientId;
            } else {
                logger.warn("No active subscriber found for inbound PACS.008 notifications");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving inbound PACS.008 subscriber: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Subscribe a client to inbound PACS.008 notifications
     * This will deactivate any existing subscription and activate the new one
     */
    @CacheEvict(value = "inboundPacs008Subscriber", allEntries = true)
    public void subscribeClientToInboundPacs008(String clientId, String updatedBy) {
        try {
            // Deactivate any existing subscription
            List<InboundClientSubscription> existingSubscriptions = subscriptionRepository
                .findByMessageTypeAndActiveTrue("INBOUND_PACS008");
            
            for (InboundClientSubscription existing : existingSubscriptions) {
                existing.setActive(false);
                existing.setUpdatedAt(LocalDateTime.now());
                existing.setUpdatedBy(updatedBy);
                subscriptionRepository.save(existing);
                logger.info("Deactivated existing subscription for client: {}", existing.getClientId());
            }
            
            // Create new subscription
            InboundClientSubscription subscription = new InboundClientSubscription();
            subscription.setClientId(clientId);
            subscription.setMessageType("INBOUND_PACS008");
            subscription.setActive(true);
            subscription.setCreatedAt(LocalDateTime.now());
            subscription.setCreatedBy(updatedBy);
            subscription.setUpdatedAt(LocalDateTime.now());
            subscription.setUpdatedBy(updatedBy);
            
            subscriptionRepository.save(subscription);
            
            logger.info("Client {} subscribed to inbound PACS.008 notifications", clientId);
            
        } catch (Exception e) {
            logger.error("Error subscribing client {} to inbound PACS.008: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Failed to subscribe client to inbound PACS.008", e);
        }
    }

    /**
     * Unsubscribe the current client from inbound PACS.008 notifications
     */
    @CacheEvict(value = "inboundPacs008Subscriber", allEntries = true)
    public void unsubscribeFromInboundPacs008(String updatedBy) {
        try {
            List<InboundClientSubscription> activeSubscriptions = subscriptionRepository
                .findByMessageTypeAndActiveTrue("INBOUND_PACS008");
            
            for (InboundClientSubscription subscription : activeSubscriptions) {
                subscription.setActive(false);
                subscription.setUpdatedAt(LocalDateTime.now());
                subscription.setUpdatedBy(updatedBy);
                subscriptionRepository.save(subscription);
                logger.info("Unsubscribed client {} from inbound PACS.008 notifications", subscription.getClientId());
            }
            
        } catch (Exception e) {
            logger.error("Error unsubscribing from inbound PACS.008: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unsubscribe from inbound PACS.008", e);
        }
    }

    /**
     * Get subscription history for inbound PACS.008
     */
    public List<InboundClientSubscription> getInboundPacs008SubscriptionHistory() {
        try {
            return subscriptionRepository.findByMessageTypeOrderByCreatedAtDesc("INBOUND_PACS008");
        } catch (Exception e) {
            logger.error("Error retrieving inbound PACS.008 subscription history: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Check if a client is currently subscribed to inbound PACS.008
     */
    public boolean isClientSubscribedToInboundPacs008(String clientId) {
        try {
            Optional<InboundClientSubscription> subscription = subscriptionRepository
                .findByClientIdAndMessageTypeAndActiveTrue(clientId, "INBOUND_PACS008");
            return subscription.isPresent();
        } catch (Exception e) {
            logger.error("Error checking client subscription: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get current subscription details
     */
    public Optional<InboundClientSubscription> getCurrentInboundPacs008Subscription() {
        try {
            return subscriptionRepository.findByMessageTypeAndActiveTrue("INBOUND_PACS008");
        } catch (Exception e) {
            logger.error("Error retrieving current subscription: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
