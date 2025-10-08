package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payaza.nps.dto.ReportRequestDto;
import com.payaza.nps.dto.ReportResponseDto;
import com.payaza.nps.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ReportController
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private ObjectMapper objectMapper;

    private ReportRequestDto reportRequest;
    private ReportResponseDto reportResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup report request
        reportRequest = new ReportRequestDto();
        reportRequest.setReportType("transaction_summary");
        reportRequest.setReportName("Daily Transaction Summary");
        reportRequest.setDescription("Summary of all transactions for today");
        reportRequest.setFromDate(LocalDate.now().minusDays(1));
        reportRequest.setToDate(LocalDate.now());
        reportRequest.setFormat("json");

        // Setup report response
        reportResponse = new ReportResponseDto();
        reportResponse.setReportId("RPT001");
        reportResponse.setReportName("Daily Transaction Summary");
        reportResponse.setReportType("transaction_summary");
        reportResponse.setStatus("COMPLETED");
        reportResponse.setCreatedAt(LocalDateTime.now());
        reportResponse.setCompletedAt(LocalDateTime.now());
        
        // Add mock data
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("transactionId", "TXN001");
        transaction.put("amount", 1000.0);
        transaction.put("status", "SUCCESS");
        data.add(transaction);
        reportResponse.setData(data);
        
        // Add summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", 1);
        summary.put("generatedAt", LocalDateTime.now());
        reportResponse.setSummary(summary);
    }

    @Test
    void generateReport_WithValidRequest_ShouldReturnReport() throws Exception {
        // Given
        when(reportService.generateReport(any(ReportRequestDto.class))).thenReturn(reportResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("RPT001"))
                .andExpect(jsonPath("$.reportName").value("Daily Transaction Summary"))
                .andExpect(jsonPath("$.reportType").value("transaction_summary"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.summary").exists());

        verify(reportService).generateReport(any(ReportRequestDto.class));
    }

    @Test
    void generateReport_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        when(reportService.generateReport(any(ReportRequestDto.class)))
                .thenThrow(new RuntimeException("Invalid report request"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isBadRequest());

        verify(reportService).generateReport(any(ReportRequestDto.class));
    }

    @Test
    void exportReport_WithValidReportId_ShouldReturnFile() throws Exception {
        // Given
        byte[] reportData = "CSV,Data,Here".getBytes();
        when(reportService.exportReport(anyString(), anyString())).thenReturn(reportData);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/export/RPT001")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService).exportReport("RPT001", "csv");
    }

    @Test
    void getReportTemplates_ShouldReturnTemplates() throws Exception {
        // Given
        List<Map<String, Object>> templates = new ArrayList<>();
        Map<String, Object> template = new HashMap<>();
        template.put("id", "transaction_summary");
        template.put("name", "Transaction Summary Report");
        template.put("description", "Summary of all transactions");
        template.put("category", "transactions");
        templates.add(template);
        
        when(reportService.getReportTemplates()).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/templates")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("transaction_summary"))
                .andExpect(jsonPath("$[0].name").value("Transaction Summary Report"));

        verify(reportService).getReportTemplates();
    }

    @Test
    void scheduleReport_WithValidRequest_ShouldReturnScheduleId() throws Exception {
        // Given
        when(reportService.scheduleReport(any(ReportRequestDto.class))).thenReturn("SCHED001");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/reports/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report scheduled successfully"))
                .andExpect(jsonPath("$.scheduleId").value("SCHED001"));

        verify(reportService).scheduleReport(any(ReportRequestDto.class));
    }

    @Test
    void getScheduledReports_ShouldReturnScheduledReports() throws Exception {
        // Given
        List<Map<String, Object>> scheduledReports = new ArrayList<>();
        Map<String, Object> scheduledReport = new HashMap<>();
        scheduledReport.put("id", "SCHED001");
        scheduledReport.put("reportName", "Daily Transaction Summary");
        scheduledReport.put("status", "SCHEDULED");
        scheduledReports.add(scheduledReport);
        
        when(reportService.getScheduledReports()).thenReturn(scheduledReports);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/scheduled")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("SCHED001"))
                .andExpect(jsonPath("$[0].reportName").value("Daily Transaction Summary"));

        verify(reportService).getScheduledReports();
    }

    @Test
    void cancelScheduledReport_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(reportService).cancelScheduledReport(anyString());

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/reports/scheduled/SCHED001")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Scheduled report cancelled successfully"));

        verify(reportService).cancelScheduledReport("SCHED001");
    }

    @Test
    void getReportHistory_ShouldReturnHistory() throws Exception {
        // Given
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> historyItem = new HashMap<>();
        historyItem.put("reportId", "RPT001");
        historyItem.put("reportName", "Daily Transaction Summary");
        historyItem.put("status", "COMPLETED");
        historyItem.put("createdAt", LocalDateTime.now());
        history.add(historyItem);
        
        when(reportService.getReportHistory(anyInt(), anyInt(), any(), any())).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/history")
                .param("page", "0")
                .param("size", "20")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reportId").value("RPT001"));

        verify(reportService).getReportHistory(0, 20, null, null);
    }

    @Test
    void getRealtimeMetrics_ShouldReturnMetrics() throws Exception {
        // Given
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("systemUptime", "99.9%");
        metrics.put("activeConnections", 150);
        metrics.put("transactionsToday", 1250);
        metrics.put("successRate", 98.5);
        
        when(reportService.getRealtimeMetrics()).thenReturn(metrics);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/metrics/realtime")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemUptime").value("99.9%"))
                .andExpect(jsonPath("$.activeConnections").value(150))
                .andExpect(jsonPath("$.transactionsToday").value(1250));

        verify(reportService).getRealtimeMetrics();
    }

    @Test
    void getPerformanceMetrics_ShouldReturnPerformanceData() throws Exception {
        // Given
        Map<String, Object> performance = new HashMap<>();
        performance.put("averageResponseTime", 2.1);
        performance.put("p95ResponseTime", 5.8);
        performance.put("throughput", 150.5);
        performance.put("errorRate", 0.5);
        
        when(reportService.getPerformanceMetrics(any(), any())).thenReturn(performance);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/metrics/performance")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageResponseTime").value(2.1))
                .andExpect(jsonPath("$.p95ResponseTime").value(5.8))
                .andExpect(jsonPath("$.throughput").value(150.5));

        verify(reportService).getPerformanceMetrics(null, null);
    }

    @Test
    void generateReport_WithoutAdminRole_ShouldReturn200() throws Exception {
        // Given
        when(reportService.generateReport(any(ReportRequestDto.class))).thenReturn(reportResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk());

        verify(reportService).generateReport(any(ReportRequestDto.class));
    }

    @Test
    void getReportTemplates_WithoutAdminRole_ShouldReturn200() throws Exception {
        // Given
        List<Map<String, Object>> templates = new ArrayList<>();
        when(reportService.getReportTemplates()).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/reports/templates")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reportService).getReportTemplates();
    }
}
