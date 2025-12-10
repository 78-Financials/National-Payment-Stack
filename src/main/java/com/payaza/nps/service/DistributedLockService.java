package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed Lock Service for Kubernetes pod coordination
 * Uses database-based locking to ensure only one pod executes critical operations
 */
@Service
public class DistributedLockService {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private AuditService auditService;
    
    @Value("${spring.application.name:nps-integration-service}")
    private String applicationName;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    // In-memory lock cache for performance
    private final Map<String, LockInfo> lockCache = new ConcurrentHashMap<>();
    
    private String podId;
    
    /**
     * Initialize pod ID on startup
     */
    public DistributedLockService() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            this.podId = hostname + "-" + serverPort + "-" + UUID.randomUUID().toString().substring(0, 8);
            logger.info("DistributedLockService initialized with pod ID: {}", this.podId);
        } catch (Exception e) {
            this.podId = "unknown-" + UUID.randomUUID().toString().substring(0, 8);
            logger.warn("Could not determine hostname, using fallback pod ID: {}", this.podId);
        }
    }

    /**
     * Acquire a distributed lock
     * @param lockName The name of the lock to acquire
     * @param lockDurationMinutes How long to hold the lock (in minutes)
     * @param timeoutSeconds How long to wait for the lock (in seconds)
     * @return LockInfo if successful, empty if failed
     */
    public Optional<LockInfo> acquireLock(String lockName, int lockDurationMinutes, int timeoutSeconds) {
        logger.info("Attempting to acquire lock: {} for {} minutes", lockName, lockDurationMinutes);
        
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                // Try to acquire the lock
                if (tryAcquireLock(lockName, lockDurationMinutes)) {
                    LockInfo lockInfo = new LockInfo(lockName, this.podId, lockDurationMinutes);
                    lockCache.put(lockName, lockInfo);
                    
                    logger.info("Successfully acquired lock: {} on pod: {}", lockName, this.podId);
                    
                    auditService.logSystemEvent(
                        "DISTRIBUTED_LOCK_ACQUIRED",
                        "DistributedLock",
                        "Distributed lock acquired successfully",
                        Map.of(
                            "lockName", lockName,
                            "podId", this.podId,
                            "lockDurationMinutes", lockDurationMinutes
                        )
                    );
                    
                    return Optional.of(lockInfo);
                }
                
                // Wait before retrying
                Thread.sleep(1000); // 1 second
                
            } catch (Exception e) {
                logger.error("Error acquiring lock {}: {}", lockName, e.getMessage());
                break;
            }
        }
        
        logger.warn("Failed to acquire lock: {} within timeout: {} seconds", lockName, timeoutSeconds);
        return Optional.empty();
    }

    /**
     * Release a distributed lock
     */
    @Transactional
    public boolean releaseLock(String lockName) {
        logger.info("Releasing lock: {}", lockName);
        
        try {
            // Remove from database
            int deleted = jdbcTemplate.update(
                "DELETE FROM distributed_locks WHERE lock_name = ? AND pod_id = ?",
                lockName, this.podId
            );
            
            // Remove from cache
            lockCache.remove(lockName);
            
            if (deleted > 0) {
                logger.info("Successfully released lock: {} on pod: {}", lockName, this.podId);
                
                auditService.logSystemEvent(
                    "DISTRIBUTED_LOCK_RELEASED",
                    "DistributedLock",
                    "Distributed lock released successfully",
                    Map.of("lockName", lockName, "podId", this.podId)
                );
                
                return true;
            } else {
                logger.warn("Lock {} not found or not owned by this pod: {}", lockName, this.podId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error releasing lock {}: {}", lockName, e.getMessage());
            return false;
        }
    }

    /**
     * Try to acquire a lock atomically
     */
    @Transactional
    private boolean tryAcquireLock(String lockName, int lockDurationMinutes) {
        try {
            // Clean up expired locks first
            cleanupExpiredLocks();
            
            // Try to insert the lock
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            
            jdbcTemplate.update(
                "INSERT INTO distributed_locks (lock_name, pod_id, application_name, acquired_at, expires_at, lock_data) VALUES (?, ?, ?, ?, ?, ?)",
                lockName, this.podId, applicationName, LocalDateTime.now(), expiresAt, "EOD Processing Lock"
            );
            
            return true;
            
        } catch (DataIntegrityViolationException e) {
            // Lock already exists
            logger.debug("Lock {} already exists, checking if expired", lockName);
            
            // Check if the existing lock is expired
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM distributed_locks WHERE lock_name = ? AND expires_at > ?",
                Integer.class, lockName, LocalDateTime.now()
            );
            
            return count != null && count == 0;
            
        } catch (Exception e) {
            logger.error("Unexpected error acquiring lock: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clean up expired locks
     */
    @Transactional
    public void cleanupExpiredLocks() {
        try {
            int deleted = jdbcTemplate.update(
                "DELETE FROM distributed_locks WHERE expires_at <= ?",
                LocalDateTime.now()
            );
            
            if (deleted > 0) {
                logger.info("Cleaned up {} expired locks", deleted);
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired locks: {}", e.getMessage());
        }
    }

    /**
     * Check if this pod currently holds a lock
     */
    public boolean holdsLock(String lockName) {
        LockInfo lockInfo = lockCache.get(lockName);
        if (lockInfo != null && lockInfo.isValid()) {
            return true;
        }
        
        // Check database if not in cache
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM distributed_locks WHERE lock_name = ? AND pod_id = ? AND expires_at > ?",
                Integer.class, lockName, this.podId, LocalDateTime.now()
            );
            
            return count != null && count > 0;
            
        } catch (Exception e) {
            logger.error("Error checking lock status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get lock information
     */
    public Optional<LockInfo> getLockInfo(String lockName) {
        try {
            return jdbcTemplate.query(
                "SELECT lock_name, pod_id, application_name, acquired_at, expires_at FROM distributed_locks WHERE lock_name = ? AND expires_at > ?",
                rs -> {
                    if (rs.next()) {
                        return Optional.of(new LockInfo(
                            rs.getString("lock_name"),
                            rs.getString("pod_id"),
                            rs.getTimestamp("acquired_at").toLocalDateTime(),
                            rs.getTimestamp("expires_at").toLocalDateTime()
                        ));
                    }
                    return Optional.empty();
                },
                lockName, LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error getting lock info: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Execute a critical operation with distributed locking
     */
    public <T> Optional<T> executeWithLock(String lockName, int lockDurationMinutes, int timeoutSeconds, LockedOperation<T> operation) {
        Optional<LockInfo> lock = acquireLock(lockName, lockDurationMinutes, timeoutSeconds);
        
        if (lock.isPresent()) {
            try {
                logger.info("Executing critical operation with lock: {}", lockName);
                return Optional.of(operation.execute());
                
            } catch (Exception e) {
                logger.error("Error executing locked operation {}: {}", lockName, e.getMessage());
                return Optional.empty();
                
            } finally {
                releaseLock(lockName);
            }
        } else {
            logger.warn("Could not acquire lock for operation: {}", lockName);
            return Optional.empty();
        }
    }

    /**
     * Refresh lock expiry time
     */
    @Transactional
    public boolean refreshLock(String lockName, int additionalMinutes) {
        try {
            int updated = jdbcTemplate.update(
                "UPDATE distributed_locks SET expires_at = ? WHERE lock_name = ? AND pod_id = ? AND expires_at > ?",
                LocalDateTime.now().plusMinutes(additionalMinutes),
                lockName, this.podId, LocalDateTime.now()
            );
            
            if (updated > 0) {
                logger.info("Refreshed lock: {} for additional {} minutes", lockName, additionalMinutes);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error refreshing lock {}: {}", lockName, e.getMessage());
            return false;
        }
    }

    /**
     * Get current pod ID
     */
    public String getPodId() {
        return this.podId;
    }

    /**
     * Lock information holder
     */
    public static class LockInfo {
        private final String lockName;
        private final String podId;
        private final LocalDateTime acquiredAt;
        private final LocalDateTime expiresAt;
        private final int durationMinutes;

        public LockInfo(String lockName, String podId, int durationMinutes) {
            this.lockName = lockName;
            this.podId = podId;
            this.acquiredAt = LocalDateTime.now();
            this.expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
            this.durationMinutes = durationMinutes;
        }

        public LockInfo(String lockName, String podId, LocalDateTime acquiredAt, LocalDateTime expiresAt) {
            this.lockName = lockName;
            this.podId = podId;
            this.acquiredAt = acquiredAt;
            this.expiresAt = expiresAt;
            this.durationMinutes = (int) java.time.Duration.between(acquiredAt, expiresAt).toMinutes();
        }

        public String getLockName() { return lockName; }
        public String getPodId() { return podId; }
        public LocalDateTime getAcquiredAt() { return acquiredAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public int getDurationMinutes() { return durationMinutes; }

        public boolean isValid() {
            return LocalDateTime.now().isBefore(expiresAt);
        }

        public long getRemainingSeconds() {
            return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        }

        @Override
        public String toString() {
            return String.format("LockInfo{name='%s', pod='%s', expires='%s', remaining=%ds}", 
                lockName, podId, expiresAt, getRemainingSeconds());
        }
    }

    /**
     * Functional interface for locked operations
     */
    @FunctionalInterface
    public interface LockedOperation<T> {
        T execute() throws Exception;
    }
}
