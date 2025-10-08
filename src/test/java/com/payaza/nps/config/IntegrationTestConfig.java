package com.payaza.nps.config;

import com.payaza.nps.service.NpsApiService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration for integration tests
 * Provides necessary beans that are excluded by application-test.properties
 */
@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Use NoOpPasswordEncoder for tests (not secure, but fine for testing)
        return NoOpPasswordEncoder.getInstance();
    }
    
    @Bean
    @Primary
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider(
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = 
            new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    @Primary
    public NpsApiService mockNpsApiService() {
        NpsApiService mockService = mock(NpsApiService.class);
        
        // Mock successful responses for all NPS API calls
        try {
            when(mockService.sendAcmt023(anyString())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>SUCCESS</Status><ResponseCode>00</ResponseCode></Response>");
            when(mockService.sendAcmt024(anyString())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>SUCCESS</Status><ResponseCode>00</ResponseCode></Response>");
            when(mockService.sendPacs008(anyString())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>SUCCESS</Status><ResponseCode>00</ResponseCode></Response>");
            when(mockService.sendPacs002(anyString())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>SUCCESS</Status><ResponseCode>00</ResponseCode></Response>");
            when(mockService.sendPacs028(anyString())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Status>SUCCESS</Status><ResponseCode>00</ResponseCode></Response>");
            when(mockService.getParticipants()).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Participants><Participant><Code>001</Code><Name>Test Bank</Name></Participant></Participants>");
        } catch (Exception e) {
            // This should not happen in the mock setup
        }
        
        return mockService;
    }
}
