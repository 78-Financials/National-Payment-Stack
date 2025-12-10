package com.payaza.nps.config;

import com.payaza.nps.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Comprehensive test configuration for controller tests
 */
@TestConfiguration
public class TestControllerConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Use NoOpPasswordEncoder for tests (not secure, but fine for testing)
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        // Use reflection or create a mock implementation
        return provider;
    }

}
