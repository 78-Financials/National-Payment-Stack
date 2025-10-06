package com.payaza.nps.security;

import com.payaza.nps.model.InternalClient;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Client Context for accessing current authenticated client
 */
public class ClientContext {
    
    private static final ThreadLocal<InternalClient> currentClient = new ThreadLocal<>();
    
    /**
     * Set current client
     */
    public static void setCurrentClient(InternalClient client) {
        currentClient.set(client);
    }
    
    /**
     * Get current client
     */
    public static InternalClient getCurrentClient() {
        return currentClient.get();
    }
    
    /**
     * Clear current client
     */
    public static void clearCurrentClient() {
        currentClient.remove();
    }
    
    /**
     * Get current client ID
     */
    public static String getCurrentClientId() {
        InternalClient client = getCurrentClient();
        return client != null ? client.getClientId() : null;
    }
}