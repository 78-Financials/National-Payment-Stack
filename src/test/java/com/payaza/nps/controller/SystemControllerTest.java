package com.payaza.nps.controller;

import com.payaza.nps.service.SystemMonitoringService;
import com.payaza.nps.service.LogAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SystemController
 */
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemMonitoringService systemMonitoringService;

    @MockBean
    private LogAggregationService logAggregationService;

    private Map<String, Object> systemHealth;
    private Map<String, Object> systemMetrics;
    private List<Map<String, Object>> systemLogs;

    @BeforeEach
    void setUp() {
        // Setup system health
        systemHealth = new HashMap<>();
        systemHealth.put("status", "HEALTHY");
        systemHealth.put("uptime", "99.9%");
        systemHealth.put("version", "1.0.0");
        systemHealth.put("timestamp", LocalDateTime.now());
        systemHealth.put("issues", new ArrayList<>());

        // Setup system metrics
        systemMetrics = new HashMap<>();
        Map<String, Object> resources = new HashMap<>();
        resources.put("memoryUsage", 75.5);
        resources.put("cpuUsage", 45.2);
        resources.put("diskUsage", 60.8);
        systemMetrics.put("resources", resources);
        
        Map<String, Object> application = new HashMap<>();
        application.put("activeSessions", 25);
        application.put("totalRequests", 15000);
        application.put("errorCount", 15);
        systemMetrics.put("application", application);
        systemMetrics.put("timestamp", LocalDateTime.now());

        // Setup system logs
        systemLogs = new ArrayList<>();
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("id", "LOG001");
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("level", "INFO");
        logEntry.put("source", "SystemController");
        logEntry.put("message", "System health check completed");
        systemLogs.add(logEntry);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSystemHealth_ShouldReturnHealthStatus() throws Exception {
        // Given
        when(systemMonitoringService.getSystemHealth()).thenReturn(systemHealth);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.uptime").value("99.9%"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.issues").isArray());

        verify(systemMonitoringService).getSystemHealth();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSystemMetrics_ShouldReturnMetrics() throws Exception {
        // Given
        when(systemMonitoringService.getSystemMetrics()).thenReturn(systemMetrics);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resources.memoryUsage").value(75.5))
                .andExpect(jsonPath("$.resources.cpuUsage").value(45.2))
                .andExpect(jsonPath("$.application.activeSessions").value(25))
                .andExpect(jsonPath("$.application.totalRequests").value(15000));

        verify(systemMonitoringService).getSystemMetrics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPerformanceMetrics_ShouldReturnPerformanceData() throws Exception {
        // Given
        Map<String, Object> performance = new HashMap<>();
        performance.put("averageResponseTime", 2.1);
        performance.put("p95ResponseTime", 5.8);
        performance.put("throughput", 150.5);
        performance.put("errorRate", 0.5);
        
        when(systemMonitoringService.getPerformanceMetrics(any(), any())).thenReturn(performance);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageResponseTime").value(2.1))
                .andExpect(jsonPath("$.p95ResponseTime").value(5.8))
                .andExpect(jsonPath("$.throughput").value(150.5))
                .andExpect(jsonPath("$.errorRate").value(0.5));

        verify(systemMonitoringService).getPerformanceMetrics(null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDatabaseHealth_ShouldReturnDatabaseStatus() throws Exception {
        // Given
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("status", "HEALTHY");
        dbHealth.put("connectionCount", 15);
        dbHealth.put("activeConnections", 8);
        dbHealth.put("responseTime", 12);
        
        when(systemMonitoringService.getDatabaseHealth()).thenReturn(dbHealth);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/database/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.connectionCount").value(15))
                .andExpect(jsonPath("$.activeConnections").value(8))
                .andExpect(jsonPath("$.responseTime").value(12));

        verify(systemMonitoringService).getDatabaseHealth();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getExternalServicesHealth_ShouldReturnServicesStatus() throws Exception {
        // Given
        Map<String, Object> servicesHealth = new HashMap<>();
        servicesHealth.put("status", "HEALTHY");
        
        Map<String, Object> nibss = new HashMap<>();
        nibss.put("status", "HEALTHY");
        nibss.put("responseTime", 150);
        servicesHealth.put("nibss", nibss);
        
        Map<String, Object> email = new HashMap<>();
        email.put("status", "HEALTHY");
        email.put("responseTime", 200);
        servicesHealth.put("email", email);
        
        when(systemMonitoringService.getExternalServicesHealth()).thenReturn(servicesHealth);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/external-services/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.nibss.status").value("HEALTHY"))
                .andExpect(jsonPath("$.email.status").value("HEALTHY"));

        verify(systemMonitoringService).getExternalServicesHealth();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSystemLogs_ShouldReturnLogs() throws Exception {
        // Given
        when(logAggregationService.getSystemLogs(anyInt(), anyInt(), anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(systemLogs);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/logs")
                .param("page", "0")
                .param("size", "50")
                .param("level", "INFO")
                .param("source", "SystemController"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("LOG001"))
                .andExpect(jsonPath("$[0].level").value("INFO"))
                .andExpect(jsonPath("$[0].source").value("SystemController"));

        verify(logAggregationService).getSystemLogs(0, 50, "INFO", "SystemController", null, null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchLogs_ShouldReturnSearchResults() throws Exception {
        // Given
        when(logAggregationService.searchLogs(anyString(), anyInt(), anyInt(), any(), any()))
                .thenReturn(systemLogs);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/logs/search")
                .param("query", "health check")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("LOG001"));

        verify(logAggregationService).searchLogs("health check", 0, 50, null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", 1000);
        stats.put("errorRate", 2.5);
        
        Map<String, Long> logsByLevel = new HashMap<>();
        logsByLevel.put("INFO", 800L);
        logsByLevel.put("WARN", 150L);
        logsByLevel.put("ERROR", 50L);
        stats.put("logsByLevel", logsByLevel);
        
        when(logAggregationService.getLogStatistics(any(), any())).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/logs/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs").value(1000))
                .andExpect(jsonPath("$.errorRate").value(2.5))
                .andExpect(jsonPath("$.logsByLevel.INFO").value(800))
                .andExpect(jsonPath("$.logsByLevel.WARN").value(150));

        verify(logAggregationService).getLogStatistics(null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exportLogs_ShouldReturnLogFile() throws Exception {
        // Given
        byte[] logData = "Log,Data,Here".getBytes();
        when(logAggregationService.exportLogs(anyString(), any(), any(), anyString(), anyString()))
                .thenReturn(logData);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/logs/export")
                .param("format", "csv")
                .param("level", "ERROR"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(logAggregationService).exportLogs("csv", null, null, "ERROR", null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSystemAlerts_ShouldReturnAlerts() throws Exception {
        // Given
        List<Map<String, Object>> alerts = new ArrayList<>();
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", "SYS001");
        alert.put("type", "HIGH_MEMORY_USAGE");
        alert.put("severity", "WARNING");
        alert.put("message", "Memory usage is above 85%");
        alert.put("timestamp", LocalDateTime.now());
        alert.put("acknowledged", false);
        alerts.add(alert);
        
        when(systemMonitoringService.getSystemAlerts(anyInt(), anyInt(), anyString())).thenReturn(alerts);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/alerts")
                .param("page", "0")
                .param("size", "20")
                .param("severity", "WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("SYS001"))
                .andExpect(jsonPath("$[0].type").value("HIGH_MEMORY_USAGE"))
                .andExpect(jsonPath("$[0].severity").value("WARNING"));

        verify(systemMonitoringService).getSystemAlerts(0, 20, "WARNING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void acknowledgeSystemAlert_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(systemMonitoringService).acknowledgeSystemAlert(anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/admin/system/alerts/SYS001/acknowledge")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Alert acknowledged successfully"));

        verify(systemMonitoringService).acknowledgeSystemAlert("SYS001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSystemConfiguration_ShouldReturnConfiguration() throws Exception {
        // Given
        Map<String, Object> config = new HashMap<>();
        config.put("maxConnections", 100);
        config.put("timeoutSeconds", 30);
        config.put("retryAttempts", 3);
        config.put("logLevel", "INFO");
        
        when(systemMonitoringService.getSystemConfiguration()).thenReturn(config);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxConnections").value(100))
                .andExpect(jsonPath("$.timeoutSeconds").value(30))
                .andExpect(jsonPath("$.retryAttempts").value(3))
                .andExpect(jsonPath("$.logLevel").value("INFO"));

        verify(systemMonitoringService).getSystemConfiguration();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSystemConfiguration_WithValidData_ShouldUpdateConfiguration() throws Exception {
        // Given
        Map<String, Object> config = new HashMap<>();
        config.put("maxConnections", 200);
        config.put("timeoutSeconds", 60);
        
        doNothing().when(systemMonitoringService).updateSystemConfiguration(any(Map.class));

        // When & Then
        mockMvc.perform(put("/api/v1/admin/system/configuration")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"maxConnections\":200,\"timeoutSeconds\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("System configuration updated successfully"));

        verify(systemMonitoringService).updateSystemConfiguration(any(Map.class));
    }

    @Test
    void getSystemHealth_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSystemMetrics_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/system/metrics"))
                .andExpect(status().isForbidden());
    }
}
