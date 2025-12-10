package com.payaza.nps.service;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for account lockout and security management
 */
@Service
@Transactional
public class AccountLockoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutService.class);
    
    @Autowired
    private InternalClientRepository clientRepository;
    
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;
    
    /**
     * Record a failed login attempt
     */
    public void recordFailedLogin(String email) {
        logger.debug("Recording failed login attempt for email: {}", email);
        
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            logger.warn("Failed login attempt for non-existent email: {}", email);
            return;
        }
        
        InternalClient client = clientOpt.get();
        client.incrementLoginAttempts();
        
        // Check if account should be locked
        if (client.getLoginAttempts() >= maxLoginAttempts) {
            client.lockAccount();
            logger.warn("Account locked due to too many failed login attempts: {}", email);
        }
        
        clientRepository.save(client);
    }
    
    /**
     * Record a successful login
     */
    public void recordSuccessfulLogin(String email) {
        logger.debug("Recording successful login for email: {}", email);
        
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            logger.warn("Successful login recorded for non-existent email: {}", email);
            return;
        }
        
        InternalClient client = clientOpt.get();
        client.resetLoginAttempts();
        client.setLastLogin(LocalDateTime.now());
        client.setLastActivity(LocalDateTime.now());
        
        clientRepository.save(client);
    }
    
    /**
     * Check if account is locked
     */
    public boolean isAccountLocked(String email) {
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return false;
        }
        
        InternalClient client = clientOpt.get();
        return client.isAccountLocked();
    }
    
    /**
     * Unlock an account (admin operation)
     */
    public boolean unlockAccount(String email) {
        logger.info("Unlocking account for email: {}", email);
        
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            logger.warn("Attempted to unlock non-existent account: {}", email);
            return false;
        }
        
        InternalClient client = clientOpt.get();
        client.unlockAccount();
        clientRepository.save(client);
        
        logger.info("Account unlocked successfully for email: {}", email);
        return true;
    }
    
    /**
     * Get account lockout status
     */
    public AccountLockoutStatus getAccountStatus(String email) {
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return new AccountLockoutStatus(false, 0, null);
        }
        
        InternalClient client = clientOpt.get();
        return new AccountLockoutStatus(
            client.isAccountLocked(),
            client.getLoginAttempts(),
            client.getLastLogin()
        );
    }
    
    /**
     * Check if account can be unlocked (time-based unlock)
     */
    public boolean canUnlockAccount(String email) {
        Optional<InternalClient> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return false;
        }
        
        InternalClient client = clientOpt.get();
        if (!client.isAccountLocked()) {
            return true; // Already unlocked
        }
        
        // Check if lockout duration has passed
        if (client.getLastLogin() != null) {
            LocalDateTime unlockTime = client.getLastLogin().plusMinutes(lockoutDurationMinutes);
            return LocalDateTime.now().isAfter(unlockTime);
        }
        
        return false;
    }
    
    /**
     * Auto-unlock accounts that have passed the lockout duration
     */
    @Transactional
    public void autoUnlockAccounts() {
        logger.info("Running auto-unlock for expired lockouts");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(lockoutDurationMinutes);
        
        clientRepository.findAll().forEach(client -> {
            if (client.isAccountLocked() && 
                client.getLastLogin() != null && 
                client.getLastLogin().isBefore(cutoffTime)) {
                
                client.unlockAccount();
                clientRepository.save(client);
                logger.info("Auto-unlocked account: {}", client.getEmail());
            }
        });
    }
    
    /**
     * Inner class for account lockout status
     */
    public static class AccountLockoutStatus {
        private final boolean locked;
        private final int loginAttempts;
        private final LocalDateTime lastLogin;
        
        public AccountLockoutStatus(boolean locked, int loginAttempts, LocalDateTime lastLogin) {
            this.locked = locked;
            this.loginAttempts = loginAttempts;
            this.lastLogin = lastLogin;
        }
        
        public boolean isLocked() { return locked; }
        public int getLoginAttempts() { return loginAttempts; }
        public LocalDateTime getLastLogin() { return lastLogin; }
    }
}
