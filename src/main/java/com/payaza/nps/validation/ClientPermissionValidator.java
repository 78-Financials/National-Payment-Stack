package com.payaza.nps.validation;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.InternalClientRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator for client permissions and endpoint access
 */
@Component
public class ClientPermissionValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientPermissionValidator.class);
    
    @Autowired
    private InternalClientRegistry clientRegistry;
    
    /**
     * Validates if the current client has permission to access the specified endpoint
     * 
     * @param endpoint The endpoint to check (e.g., "pacs008", "acmt023")
     * @return true if client has permission, false otherwise
     */
    public boolean hasPermission(String endpoint) {
        String currentClientId = ClientContext.getCurrentClientId();
        if (currentClientId == null) {
            logger.warn("No client context found for permission validation");
            return false;
        }
        
        InternalClient client = clientRegistry.getClientById(currentClientId);
        if (client == null) {
            logger.warn("Client '{}' not found in registry", currentClientId);
            return false;
        }
        
        if (!client.isActive()) {
            logger.warn("Client '{}' is inactive", currentClientId);
            return false;
        }
        
        boolean hasPermission = client.hasPermission(endpoint.toLowerCase());
        if (hasPermission) {
            logger.debug("Client '{}' has permission to access endpoint '{}'", currentClientId, endpoint);
        } else {
            logger.warn("Client '{}' does not have permission to access endpoint '{}'", currentClientId, endpoint);
        }
        
        return hasPermission;
    }
    
    /**
     * Validates if a specific client has permission to access the specified endpoint
     * 
     * @param clientId The client ID to check
     * @param endpoint The endpoint to check
     * @return true if client has permission, false otherwise
     */
    public boolean hasPermission(String clientId, String endpoint) {
        InternalClient client = clientRegistry.getClientById(clientId);
        if (client == null) {
            logger.warn("Client '{}' not found in registry", clientId);
            return false;
        }
        
        if (!client.isActive()) {
            logger.warn("Client '{}' is inactive", clientId);
            return false;
        }
        
        boolean hasPermission = client.hasPermission(endpoint.toLowerCase());
        if (hasPermission) {
            logger.debug("Client '{}' has permission to access endpoint '{}'", clientId, endpoint);
        } else {
            logger.warn("Client '{}' does not have permission to access endpoint '{}'", clientId, endpoint);
        }
        
        return hasPermission;
    }
    
    /**
     * Gets the current client information
     * 
     * @return The current client or null if not authenticated
     */
    public InternalClient getCurrentClient() {
        return ClientContext.getCurrentClient();
    }
    
    /**
     * Gets the current client ID
     * 
     * @return The current client ID or null if not authenticated
     */
    public String getCurrentClientId() {
        return ClientContext.getCurrentClientId();
    }
    
    /**
     * Checks if the current client is active
     * 
     * @return true if client is active, false otherwise
     */
    public boolean isCurrentClientActive() {
        String currentClientId = ClientContext.getCurrentClientId();
        if (currentClientId == null) {
            return false;
        }
        
        return clientRegistry.isClientActive(currentClientId);
    }
}
