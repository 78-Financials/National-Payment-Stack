package com.payaza.nps.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
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
 * Filter for API key authentication
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    @Autowired
    private NpsConfiguration npsConfig;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey != null && isValidApiKey(apiKey)) {
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "api-user", 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER"))
            );
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.debug("API key authentication successful");
        } else if (apiKey != null) {
            logger.warn("Invalid API key provided: {}", apiKey);
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isValidApiKey(String apiKey) {
        // In a real implementation, you would validate against a database or configuration
        // For now, we'll use the client secret as the API key
        return npsConfig.getClientSecret() != null && npsConfig.getClientSecret().equals(apiKey);
    }
}
