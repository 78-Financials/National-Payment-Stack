package com.payaza.nps.service;

import com.payaza.nps.dto.ReportRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Report Scheduler Service for scheduled report generation
 */
@Service
public class ReportSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportSchedulerService.class);
    
    @Autowired
    private ReportService reportService;
    
    // In-memory storage for demo purposes (in production, use database)
    private final Map<String, ScheduledReport> scheduledReports = new HashMap<>();
    
    /**
     * Schedule report generation
     */
    public void scheduleReport(String scheduleId, ReportRequestDto request) {
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setId(scheduleId);
        scheduledReport.setRequest(request);
        scheduledReport.setStatus("SCHEDULED");
        scheduledReport.setCreatedAt(LocalDateTime.now());
        scheduledReport.setNextRun(calculateNextRun(request.getSchedule()));
        
        scheduledReports.put(scheduleId, scheduledReport);
        
        logger.info("Report scheduled: {} - Next run: {}", scheduleId, scheduledReport.getNextRun());
    }
    
    /**
     * Get scheduled reports
     */
    public List<Map<String, Object>> getScheduledReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        
        for (ScheduledReport report : scheduledReports.values()) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("id", report.getId());
            reportData.put("reportName", report.getRequest().getReportName());
            reportData.put("reportType", report.getRequest().getReportType());
            reportData.put("status", report.getStatus());
            reportData.put("nextRun", report.getNextRun());
            reportData.put("createdAt", report.getCreatedAt());
            reportData.put("frequency", report.getRequest().getSchedule().getFrequency());
            reportData.put("recipients", report.getRequest().getSchedule().getRecipients());
            
            reports.add(reportData);
        }
        
        return reports;
    }
    
    /**
     * Cancel scheduled report
     */
    public void cancelScheduledReport(String scheduleId) {
        ScheduledReport report = scheduledReports.remove(scheduleId);
        if (report != null) {
            report.setStatus("CANCELLED");
            logger.info("Scheduled report cancelled: {}", scheduleId);
        }
    }
    
    /**
     * Process scheduled reports (called by scheduler)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processScheduledReports() {
        LocalDateTime now = LocalDateTime.now();
        
        for (ScheduledReport report : scheduledReports.values()) {
            if ("SCHEDULED".equals(report.getStatus()) && 
                report.getNextRun() != null && 
                report.getNextRun().isBefore(now)) {
                
                try {
                    logger.info("Processing scheduled report: {}", report.getId());
                    
                    // Generate the report
                    reportService.generateReport(report.getRequest());
                    
                    // Update next run time
                    report.setNextRun(calculateNextRun(report.getRequest().getSchedule()));
                    report.setLastRun(now);
                    
                    logger.info("Scheduled report processed successfully: {}", report.getId());
                    
                } catch (Exception e) {
                    logger.error("Failed to process scheduled report: {}", report.getId(), e);
                    report.setStatus("FAILED");
                    report.setErrorMessage(e.getMessage());
                }
            }
        }
    }
    
    /**
     * Calculate next run time based on schedule
     */
    private LocalDateTime calculateNextRun(ReportRequestDto.ScheduleDto schedule) {
        if (schedule == null || !schedule.isEnabled()) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (schedule.getFrequency().toUpperCase()) {
            case "DAILY":
                return now.plusDays(1).withHour(parseHour(schedule.getTime()))
                          .withMinute(parseMinute(schedule.getTime()));
            case "WEEKLY":
                return now.plusWeeks(1).withHour(parseHour(schedule.getTime()))
                          .withMinute(parseMinute(schedule.getTime()));
            case "MONTHLY":
                return now.plusMonths(1).withHour(parseHour(schedule.getTime()))
                          .withMinute(parseMinute(schedule.getTime()));
            default:
                return now.plusHours(1);
        }
    }
    
    /**
     * Parse hour from time string (HH:mm format)
     */
    private int parseHour(String time) {
        if (time == null || !time.contains(":")) {
            return 9; // Default to 9 AM
        }
        return Integer.parseInt(time.split(":")[0]);
    }
    
    /**
     * Parse minute from time string (HH:mm format)
     */
    private int parseMinute(String time) {
        if (time == null || !time.contains(":")) {
            return 0; // Default to 0 minutes
        }
        return Integer.parseInt(time.split(":")[1]);
    }
    
    /**
     * Inner class for scheduled report
     */
    private static class ScheduledReport {
        private String id;
        private ReportRequestDto request;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime nextRun;
        private LocalDateTime lastRun;
        private String errorMessage;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public ReportRequestDto getRequest() { return request; }
        public void setRequest(ReportRequestDto request) { this.request = request; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getNextRun() { return nextRun; }
        public void setNextRun(LocalDateTime nextRun) { this.nextRun = nextRun; }
        
        public LocalDateTime getLastRun() { return lastRun; }
        public void setLastRun(LocalDateTime lastRun) { this.lastRun = lastRun; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
