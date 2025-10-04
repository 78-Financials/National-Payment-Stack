package com.payaza.nps.controller;

import com.payaza.nps.service.PaymentMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for monitoring and health check endpoints
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    @Autowired
    private PaymentMonitoringService monitoringService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<PaymentMonitoringService.SystemHealth> healthCheck() {
        logger.info("Health check requested");
        PaymentMonitoringService.SystemHealth health = monitoringService.checkSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<PaymentMonitoringService.PaymentStatistics> getStatistics() {
        logger.info("Payment statistics requested");
        PaymentMonitoringService.PaymentStatistics stats = monitoringService.generateStatistics();
        return ResponseEntity.ok(stats);
    }
}
