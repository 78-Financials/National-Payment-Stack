package com.payaza.nps.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Test security configuration to provide mock beans
 */
@TestConfiguration
public class TestSecurityConfig {
    
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Use NoOpPasswordEncoder for tests (not secure, but fine for testing)
        return NoOpPasswordEncoder.getInstance();
    }
}
