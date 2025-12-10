package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Rate Limit Service for API rate limiting
 */
@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    // In-memory storage for demo purposes (in production, use Redis)
    private final Map<String, Map<String, Object>> clientRateLimits = new HashMap<>();
    private final Map<String, Map<String, Object>> clientUsage = new HashMap<>();
    
    /**
     * Update client rate limit configuration
     */
    public void updateClientRateLimit(String clientId, Map<String, Object> rateLimitConfig) {
        clientRateLimits.put(clientId, rateLimitConfig);
        logger.info("Rate limit configuration updated for client: {}", clientId);
    }
    
    /**
     * Get client usage statistics
     */
    public Map<String, Object> getClientUsage(String clientId) {
        Map<String, Object> usage = clientUsage.getOrDefault(clientId, new HashMap<>());
        
        // Add default values if not present
        usage.putIfAbsent("requestsToday", 0);
        usage.putIfAbsent("requestsThisHour", 0);
        usage.putIfAbsent("requestsThisMinute", 0);
        usage.putIfAbsent("lastRequest", LocalDateTime.now());
        usage.putIfAbsent("rateLimitRemaining", 1000);
        
        return usage;
    }
    
    /**
     * Check if client has exceeded rate limit
     */
    public boolean isRateLimitExceeded(String clientId) {
        Map<String, Object> rateLimit = clientRateLimits.get(clientId);
        Map<String, Object> usage = getClientUsage(clientId);
        
        if (rateLimit == null) {
            // Use default rate limits
            return (Integer) usage.get("requestsThisHour") > 100;
        }
        
        int hourlyLimit = (Integer) rateLimit.getOrDefault("hourlyLimit", 100);
        int currentHourlyUsage = (Integer) usage.get("requestsThisHour");
        
        return currentHourlyUsage >= hourlyLimit;
    }
    
    /**
     * Record API request for client
     */
    public void recordRequest(String clientId) {
        Map<String, Object> usage = clientUsage.computeIfAbsent(clientId, k -> new HashMap<>());
        
        // Update counters
        usage.put("requestsToday", (Integer) usage.getOrDefault("requestsToday", 0) + 1);
        usage.put("requestsThisHour", (Integer) usage.getOrDefault("requestsThisHour", 0) + 1);
        usage.put("requestsThisMinute", (Integer) usage.getOrDefault("requestsThisMinute", 0) + 1);
        usage.put("lastRequest", LocalDateTime.now());
        
        // Update rate limit remaining
        Map<String, Object> rateLimit = clientRateLimits.get(clientId);
        int hourlyLimit = rateLimit != null ? (Integer) rateLimit.getOrDefault("hourlyLimit", 100) : 100;
        int currentHourlyUsage = (Integer) usage.get("requestsThisHour");
        usage.put("rateLimitRemaining", Math.max(0, hourlyLimit - currentHourlyUsage));
    }
    
    /**
     * Reset hourly counters (should be called by a scheduled job)
     */
    public void resetHourlyCounters() {
        for (Map<String, Object> usage : clientUsage.values()) {
            usage.put("requestsThisHour", 0);
            usage.put("requestsThisMinute", 0);
        }
        logger.info("Hourly rate limit counters reset");
    }
    
    /**
     * Reset daily counters (should be called by a scheduled job)
     */
    public void resetDailyCounters() {
        for (Map<String, Object> usage : clientUsage.values()) {
            usage.put("requestsToday", 0);
        }
        logger.info("Daily rate limit counters reset");
    }
}
