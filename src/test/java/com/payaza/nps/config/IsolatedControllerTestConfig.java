package com.payaza.nps.config;

import com.payaza.nps.controller.AdminController;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.service.ApiKeyGenerationService;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.InternalClientRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Isolated test configuration for controller tests
 * This configuration only loads the specific controller and its dependencies
 */
@TestConfiguration
public class IsolatedControllerTestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
