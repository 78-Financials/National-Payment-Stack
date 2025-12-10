package com.payaza.nps.repository;

import com.payaza.nps.model.InboundClientSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing inbound client subscriptions
 */
@Repository
public interface InboundClientSubscriptionRepository extends JpaRepository<InboundClientSubscription, Long> {

    /**
     * Find active subscriptions for a specific message type
     */
    List<InboundClientSubscription> findByMessageTypeAndActiveTrue(String messageType);

    /**
     * Find subscription history for a specific message type ordered by creation date
     */
    List<InboundClientSubscription> findByMessageTypeOrderByCreatedAtDesc(String messageType);

    /**
     * Find active subscription for a specific client and message type
     */
    Optional<InboundClientSubscription> findByClientIdAndMessageTypeAndActiveTrue(String clientId, String messageType);

    /**
     * Find all active subscriptions for a client
     */
    List<InboundClientSubscription> findByClientIdAndActiveTrue(String clientId);

    /**
     * Find all subscriptions for a client (active and inactive)
     */
    List<InboundClientSubscription> findByClientIdOrderByCreatedAtDesc(String clientId);

    /**
     * Count active subscriptions for a specific message type
     */
    @Query("SELECT COUNT(s) FROM InboundClientSubscription s WHERE s.messageType = :messageType AND s.active = true")
    Long countActiveSubscriptionsByMessageType(@Param("messageType") String messageType);

    /**
     * Find subscriptions created by a specific user
     */
    List<InboundClientSubscription> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find subscriptions updated by a specific user
     */
    List<InboundClientSubscription> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);
}
