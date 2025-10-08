package com.payaza.nps.service;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service to manage internal client configurations loaded from database.
 * This registry holds details like API keys, transaction prefixes, and allowed endpoints for each client.
 * It caches clients in memory for performance and refreshes periodically from database.
 */
@Service
public class InternalClientRegistry {

    private static final Logger logger = LoggerFactory.getLogger(InternalClientRegistry.class);

    @Autowired
    private InternalClientRepository clientRepository;

    @Autowired
    private ApiKeyGenerationService apiKeyGenerationService;
    
    @Autowired
    private PasswordService passwordService;

    // Cache for clients (API key -> Client)
    private final Map<String, InternalClient> clientsByApiKey = new HashMap<>();
    private final Map<String, InternalClient> clientsById = new HashMap<>();

    // Default admin client configuration from properties
    @Value("${admin.default-client-id:ADMIN}")
    private String defaultAdminClientId;
    
    @Value("${admin.default-client-name:System Administrator}")
    private String defaultAdminClientName;
    
    @Value("${admin.default-api-key:admin_api_key_99999_secure_default}")
    private String defaultAdminApiKey;
    
    @Value("${admin.default-email:admin@nps.payaza.com}")
    private String defaultAdminEmail;
    
    @Value("${admin.default-password:Admin@123456}")
    private String defaultAdminPassword;
    
    @Value("${admin.default-transaction-prefix:ADM}")
    private String defaultAdminTransactionPrefix;
    
    @Value("${admin.default-allowed-endpoints:pacs008,acmt023,acmt024,pacs002,pacs028}")
    private Set<String> defaultAdminAllowedEndpoints;
    
    @Value("${admin.default-active:true}")
    private Boolean defaultAdminActive;
    
    @Value("${admin.default-rate-limit-per-minute:1000}")
    private Integer defaultAdminRateLimitPerMinute;

    @PostConstruct
    public void initialize() {
        logger.info("Initializing internal client registry from database...");
        
        try {
            // Ensure default admin client exists
            ensureDefaultAdminClient();
            
            // Load all clients from database
            loadClientsFromDatabase();
            
            logger.info("Internal client registry initialized with {} clients", clientsByApiKey.size());
            logger.info("Registered clients: {}", clientsById.keySet());
            
        } catch (Exception e) {
            logger.error("Error initializing client registry: {}", e.getMessage(), e);
            // Continue with empty registry rather than failing startup
        }
    }

    /**
     * Ensure default admin client exists in database
     */
    private void ensureDefaultAdminClient() {
        try {
            if (!clientRepository.existsByClientId(defaultAdminClientId)) {
                logger.info("Creating default admin client: {}", defaultAdminClientId);
                
                InternalClient adminClient = new InternalClient();
                adminClient.setClientId(defaultAdminClientId);
                adminClient.setClientName(defaultAdminClientName);
                adminClient.setEmail(defaultAdminEmail);
                adminClient.setPassword(passwordService.encodePassword(defaultAdminPassword));
                adminClient.setApiKey(defaultAdminApiKey);
                adminClient.setTransactionPrefix(defaultAdminTransactionPrefix);
                adminClient.setAllowedEndpoints(defaultAdminAllowedEndpoints);
                adminClient.setActive(defaultAdminActive);
                adminClient.setRateLimitPerMinute(defaultAdminRateLimitPerMinute);
                adminClient.setDescription("Default system administrator client");
                adminClient.setContactEmail("admin@nps.payaza.com");
                adminClient.setContactPhone("+234-800-000-0000");
                adminClient.setClientType("ADMIN");
                adminClient.setCreatedBy("SYSTEM");
                adminClient.setUpdatedBy("SYSTEM");
                
                clientRepository.save(adminClient);
                logger.info("Default admin client created successfully");
            } else {
                logger.debug("Default admin client already exists");
            }
        } catch (Exception e) {
            logger.error("Error ensuring default admin client: {}", e.getMessage(), e);
        }
    }

    /**
     * Load all clients from database into cache
     */
    private void loadClientsFromDatabase() {
        try {
            List<InternalClient> clients = clientRepository.findByActiveTrue();
            
            // Clear existing cache
            clientsByApiKey.clear();
            clientsById.clear();
            
            // Load clients into cache
            for (InternalClient client : clients) {
                clientsByApiKey.put(client.getApiKey(), client);
                clientsById.put(client.getClientId(), client);
                
                logger.debug("Loaded client: {} ({}), prefix: {}, endpoints: {}", 
                           client.getClientId(), client.getClientName(), 
                           client.getTransactionPrefix(), client.getAllowedEndpoints());
            }
            
            logger.info("Loaded {} active clients from database", clients.size());
            
        } catch (Exception e) {
            logger.error("Error loading clients from database: {}", e.getMessage(), e);
        }
    }

    /**
     * Get client by API key
     */
    public InternalClient getClientByApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.debug("API key is null or empty");
            return null;
        }

        InternalClient client = clientsByApiKey.get(apiKey);
        if (client == null) {
            logger.debug("Client not found for API key: {}", apiKey);
            return null;
        }

        // Update last accessed timestamp (async)
        updateLastAccessedAsync(client.getClientId());

        return client;
    }

    /**
     * Get client by client ID
     */
    public InternalClient getClientByClientId(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            logger.debug("Client ID is null or empty");
            return null;
        }

        return clientsById.get(clientId.toUpperCase());
    }

    /**
     * Get all registered clients
     */
    public List<InternalClient> getAllClients() {
        return clientsByApiKey.values().stream()
                .collect(Collectors.toList());
    }

    /**
     * Get all registered client IDs
     */
    public Set<String> getRegisteredClientIds() {
        return new HashSet<>(clientsById.keySet());
    }

    /**
     * Check if client exists by API key
     */
    public boolean clientExists(String apiKey) {
        return apiKey != null && clientsByApiKey.containsKey(apiKey);
    }

    /**
     * Check if client exists by client ID
     */
    public boolean clientExistsByClientId(String clientId) {
        return clientId != null && clientsById.containsKey(clientId.toUpperCase());
    }

    /**
     * Refresh client cache from database
     * This method can be called manually or scheduled
     */
    public void refreshCache() {
        logger.info("Refreshing client cache from database...");
        loadClientsFromDatabase();
    }

    /**
     * Add client to cache (for real-time updates)
     */
    public void addClientToCache(InternalClient client) {
        if (client != null && client.isActive()) {
            clientsByApiKey.put(client.getApiKey(), client);
            clientsById.put(client.getClientId(), client);
            logger.debug("Added client to cache: {}", client.getClientId());
        }
    }

    /**
     * Remove client from cache
     */
    public void removeClientFromCache(String clientId) {
        InternalClient client = clientsById.remove(clientId);
        if (client != null) {
            clientsByApiKey.remove(client.getApiKey());
            logger.debug("Removed client from cache: {}", clientId);
        }
    }

    /**
     * Update client in cache
     */
    public void updateClientInCache(InternalClient client) {
        if (client != null) {
            if (client.isActive()) {
                clientsByApiKey.put(client.getApiKey(), client);
                clientsById.put(client.getClientId(), client);
                logger.debug("Updated client in cache: {}", client.getClientId());
            } else {
                // Remove if deactivated
                removeClientFromCache(client.getClientId());
            }
        }
    }

    /**
     * Update last accessed timestamp asynchronously
     */
    private void updateLastAccessedAsync(String clientId) {
        try {
            // Use repository method to update timestamp
            clientRepository.updateLastAccessedAt(clientId, LocalDateTime.now());
        } catch (Exception e) {
            logger.debug("Error updating last accessed timestamp for client {}: {}", clientId, e.getMessage());
        }
    }

    /**
     * Scheduled refresh every 5 minutes to sync with database changes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledRefresh() {
        logger.debug("Performing scheduled client cache refresh...");
        refreshCache();
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients", clientsByApiKey.size());
        stats.put("clientIds", clientsById.keySet());
        stats.put("lastRefresh", LocalDateTime.now().toString());
        return stats;
    }
    
    /**
     * Get client by ID
     */
    public InternalClient getClientById(String clientId) {
        return clientsById.get(clientId);
    }
    
    /**
     * Check if client is active
     */
    public boolean isClientActive(String clientId) {
        InternalClient client = clientsById.get(clientId);
        return client != null && client.isActive();
    }
    
    /**
     * Update client in database and cache
     */
    public void updateClient(InternalClient client) {
        if (client != null) {
            clientRepository.save(client);
            updateClientInCache(client);
            logger.info("Updated client: {}", client.getClientId());
        }
    }
    
    /**
     * Get client by contact email
     */
    public InternalClient getClientByEmail(String email) {
        return clientRepository.findByContactEmail(email).orElse(null);
    }
    
    /**
     * Get client by reset token
     */
    public InternalClient getClientByResetToken(String resetToken) {
        return clientRepository.findByPasswordResetToken(resetToken).orElse(null);
    }
}