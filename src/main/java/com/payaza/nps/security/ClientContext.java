package com.payaza.nps.security;

import com.payaza.nps.model.InternalClient;

/**
 * Thread-local client context for storing current authenticated client
 */
public class ClientContext {
    
    private static final ThreadLocal<InternalClient> currentClient = new ThreadLocal<>();
    
    public static void setCurrentClient(InternalClient client) {
        currentClient.set(client);
    }
    
    public static InternalClient getCurrentClient() {
        return currentClient.get();
    }
    
    public static void clear() {
        currentClient.remove();
    }
    
    public static String getCurrentClientId() {
        InternalClient client = getCurrentClient();
        return client != null ? client.getClientId() : null;
    }
    
    public static String getCurrentClientPrefix() {
        InternalClient client = getCurrentClient();
        return client != null ? client.getTransactionPrefix() : null;
    }
}
