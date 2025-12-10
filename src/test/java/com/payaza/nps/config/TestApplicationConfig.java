package com.payaza.nps.config;

import com.payaza.nps.service.NpsXmlEncryptionService;
import com.payaza.nps.service.NpsXmlSignatureService;
import com.payaza.nps.service.Acmt024XmlParser;
import com.payaza.nps.service.NpsXmlDecryptionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * Test application configuration for XML signature/encryption tests
 */
@TestConfiguration
public class TestApplicationConfig {
    
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Use NoOpPasswordEncoder for tests (not secure, but fine for testing)
        return NoOpPasswordEncoder.getInstance();
    }
    
    @Bean
    public NpsXmlSignatureService xmlSignatureService() {
        return new NpsXmlSignatureService();
    }
    
    @Bean
    public NpsXmlEncryptionService xmlEncryptionService() {
        return new NpsXmlEncryptionService();
    }
    
    @Bean
    public Acmt024XmlParser acmt024XmlParser() {
        return new Acmt024XmlParser();
    }
    
    @Bean
    public NpsXmlDecryptionService npsXmlDecryptionService() {
        return new NpsXmlDecryptionService();
    }
    
}
