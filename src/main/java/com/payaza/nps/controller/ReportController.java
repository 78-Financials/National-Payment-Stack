package com.payaza.nps.controller;

import com.payaza.nps.dto.ReportRequestDto;
import com.payaza.nps.dto.ReportResponseDto;
import com.payaza.nps.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Advanced Analytics and Reporting Controller
 */
@RestController
@RequestMapping("/api/v1/admin/reports")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    
    @Autowired
    private ReportService reportService;
    
    /**
     * Generate custom report
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDto> generateReport(@Valid @RequestBody ReportRequestDto request) {
        try {
            logger.info("Generating custom report: {}", request.getReportType());
            ReportResponseDto report = reportService.generateReport(request);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Failed to generate report: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Export report in various formats
     */
    @GetMapping("/export/{reportId}")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportId,
            @RequestParam String format) {
        try {
            byte[] reportData = reportService.exportReport(reportId, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.setContentDispositionFormData("attachment", 
                String.format("report_%s.%s", reportId, format.toLowerCase()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
        } catch (Exception e) {
            logger.error("Failed to export report: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get available report templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> getReportTemplates() {
        try {
            List<Map<String, Object>> templates = reportService.getReportTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            logger.error("Failed to get report templates: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Schedule report generation
     */
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, String>> scheduleReport(@Valid @RequestBody ReportRequestDto request) {
        try {
            String scheduleId = reportService.scheduleReport(request);
            logger.info("Report scheduled with ID: {}", scheduleId);
            return ResponseEntity.ok(Map.of(
                "message", "Report scheduled successfully",
                "scheduleId", scheduleId
            ));
        } catch (Exception e) {
            logger.error("Failed to schedule report: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get scheduled reports
     */
    @GetMapping("/scheduled")
    public ResponseEntity<List<Map<String, Object>>> getScheduledReports() {
        try {
            List<Map<String, Object>> scheduledReports = reportService.getScheduledReports();
            return ResponseEntity.ok(scheduledReports);
        } catch (Exception e) {
            logger.error("Failed to get scheduled reports: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel scheduled report
     */
    @DeleteMapping("/scheduled/{scheduleId}")
    public ResponseEntity<Map<String, String>> cancelScheduledReport(@PathVariable String scheduleId) {
        try {
            reportService.cancelScheduledReport(scheduleId);
            logger.info("Scheduled report cancelled: {}", scheduleId);
            return ResponseEntity.ok(Map.of("message", "Scheduled report cancelled successfully"));
        } catch (Exception e) {
            logger.error("Failed to cancel scheduled report: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get report history
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getReportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            List<Map<String, Object>> history = reportService.getReportHistory(page, size, from, to);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Failed to get report history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get real-time metrics
     */
    @GetMapping("/metrics/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        try {
            Map<String, Object> metrics = reportService.getRealtimeMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to get real-time metrics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get performance metrics
     */
    @GetMapping("/metrics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            Map<String, Object> metrics = reportService.getPerformanceMetrics(from, to);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to get performance metrics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    private MediaType getMediaType(String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return MediaType.parseMediaType("text/csv");
            case "xlsx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "json":
                return MediaType.APPLICATION_JSON;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
