package com.payaza.nps.security;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService implementation for NPS authentication
 */
@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private InternalClientRepository clientRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);
        
        InternalClient client = clientRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.warn("User not found with email: {}", email);
                return new UsernameNotFoundException("User not found with email: " + email);
            });
        
        if (!client.isActive()) {
            logger.warn("Inactive user attempted login: {}", email);
            throw new UsernameNotFoundException("User account is inactive: " + email);
        }
        
        if (client.isAccountLocked()) {
            logger.warn("Locked account attempted login: {}", email);
            throw new UsernameNotFoundException("User account is locked: " + email);
        }
        
        logger.debug("User loaded successfully: {} (clientId: {})", email, client.getClientId());
        
        return User.builder()
            .username(client.getEmail())
            .password(client.getPassword())
            .authorities(getAuthorities(client))
            .accountExpired(false)
            .accountLocked(client.isAccountLocked())
            .credentialsExpired(false)
            .disabled(!client.isActive())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(InternalClient client) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role based on client type
        String role = "ROLE_" + (client.getClientType() != null ? client.getClientType() : "BANK");
        authorities.add(new SimpleGrantedAuthority(role));
        
        // Add admin role if client is admin
        if ("ADMIN".equalsIgnoreCase(client.getClientType())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        logger.debug("Authorities for user {}: {}", client.getEmail(), authorities);
        return authorities;
    }
}
