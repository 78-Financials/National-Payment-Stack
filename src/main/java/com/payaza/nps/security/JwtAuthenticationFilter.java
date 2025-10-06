package com.payaza.nps.security;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.service.InternalClientRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private InternalClientRegistry clientRegistry;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String clientId = jwtTokenProvider.getClientIdFromToken(token);
                InternalClient client = clientRegistry.getClientById(clientId);
                
                if (client != null && client.isActive()) {
                    // Set client context
                    ClientContext.setCurrentClient(client);
                    
                    // Set security context
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        client, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from request
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
