package com.payaza.nps.config;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.InternalClientRegistry;
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
 * Enhanced API key authentication filter for internal clients
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    @Autowired
    private InternalClientRegistry clientRegistry;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey != null) {
            logger.debug("Looking up client for API key: {}", apiKey);
            InternalClient client = clientRegistry.getClientByApiKey(apiKey);
            logger.debug("Client lookup result: {}", client != null ? client.getClientId() : "null");
            
            if (client != null && client.isActive()) {
                // Set client context for the current request
                ClientContext.setCurrentClient(client);
                
                // Create authentication token with client-type-based authority
                String clientType = client.getClientType() != null ? client.getClientType() : "BANK";
                // Map client types to the expected role names
                String authority;
                switch (clientType.toUpperCase()) {
                    case "BANK":
                        authority = "ROLE_CLIENT_BANK";
                        break;
                    case "FIN":
                        authority = "ROLE_CLIENT_FIN";
                        break;
                    case "PAY":
                        authority = "ROLE_CLIENT_PAY";
                        break;
                    default:
                        authority = "ROLE_CLIENT_BANK"; // Default to BANK role
                        break;
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    client.getClientId(), 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority(authority))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Client authentication successful: {} ({}) with authority: {}", client.getClientId(), client.getClientName(), authority);
            } else if (client != null) {
                logger.warn("Client {} is inactive", client.getClientId());
                ClientContext.clear();
            } else {
                logger.warn("Invalid API key provided: {}", apiKey);
                ClientContext.clear();
            }
        } else {
            ClientContext.clear();
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up client context after request
            ClientContext.clear();
        }
    }
}
