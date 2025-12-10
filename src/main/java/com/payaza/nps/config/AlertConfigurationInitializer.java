package com.payaza.nps.config;

import com.payaza.nps.service.AlertConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize alert configuration on application startup
 */
@Component
public class AlertConfigurationInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertConfigurationInitializer.class);
    
    @Autowired
    private AlertConfigurationService alertConfigService;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // Initialize default alert configuration
            alertConfigService.initializeDefaults();
            
            logger.info("Alert configuration initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing alert configuration: {}", e.getMessage(), e);
        }
    }
}
