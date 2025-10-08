package com.payaza.nps.repository;

import com.payaza.nps.model.InternalClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InternalClient entity
 * Provides database operations for client management
 */
@Repository
public interface InternalClientRepository extends JpaRepository<InternalClient, Long> {
    
    /**
     * Find client by API key
     */
    Optional<InternalClient> findByApiKey(String apiKey);
    
    /**
     * Find client by client ID
     */
    Optional<InternalClient> findByClientId(String clientId);
    
    /**
     * Find client by transaction prefix
     */
    Optional<InternalClient> findByTransactionPrefix(String transactionPrefix);
    
    /**
     * Find all active clients
     */
    List<InternalClient> findByActiveTrue();
    
    /**
     * Find all clients by active status
     */
    List<InternalClient> findByActive(Boolean active);
    
    /**
     * Find clients by created by user
     */
    List<InternalClient> findByCreatedBy(String createdBy);
    
    /**
     * Check if client ID exists
     */
    boolean existsByClientId(String clientId);
    
    /**
     * Check if API key exists
     */
    boolean existsByApiKey(String apiKey);
    
    /**
     * Check if transaction prefix exists
     */
    boolean existsByTransactionPrefix(String transactionPrefix);
    
    /**
     * Update last accessed timestamp
     */
    @Modifying
    @Query("UPDATE InternalClient c SET c.lastAccessedAt = :timestamp WHERE c.clientId = :clientId")
    int updateLastAccessedAt(@Param("clientId") String clientId, @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Find clients with specific endpoint access
     */
    @Query("SELECT c FROM InternalClient c JOIN c.allowedEndpoints e WHERE e = :endpoint AND c.active = true")
    List<InternalClient> findActiveClientsWithEndpointAccess(@Param("endpoint") String endpoint);
    
    /**
     * Find clients created after specific date
     */
    List<InternalClient> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Find clients with rate limit above threshold
     */
    List<InternalClient> findByRateLimitPerMinuteGreaterThan(Integer rateLimit);
    
    /**
     * Count active clients
     */
    @Query("SELECT COUNT(c) FROM InternalClient c WHERE c.active = true")
    long countActiveClients();
    
    /**
     * Find clients by name pattern (case insensitive)
     */
    List<InternalClient> findByClientNameContainingIgnoreCase(String namePattern);
    
    /**
     * Find client by contact email
     */
    Optional<InternalClient> findByContactEmail(String contactEmail);
    
    /**
     * Find client by password reset token
     */
    Optional<InternalClient> findByPasswordResetToken(String resetToken);
}
