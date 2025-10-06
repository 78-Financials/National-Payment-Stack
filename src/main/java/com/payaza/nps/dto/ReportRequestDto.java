package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report request DTO
 */
public class ReportRequestDto {
    
    @NotBlank(message = "Report type is required")
    @JsonProperty("reportType")
    private String reportType;
    
    @JsonProperty("reportName")
    private String reportName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("fromDate")
    private LocalDate fromDate;
    
    @JsonProperty("toDate")
    private LocalDate toDate;
    
    @JsonProperty("clientIds")
    private List<String> clientIds;
    
    @JsonProperty("filters")
    private Map<String, Object> filters;
    
    @JsonProperty("groupBy")
    private String groupBy;
    
    @JsonProperty("format")
    private String format = "json";
    
    @JsonProperty("schedule")
    private ScheduleDto schedule;
    
    // Constructors
    public ReportRequestDto() {}
    
    public ReportRequestDto(String reportType, String reportName) {
        this.reportType = reportType;
        this.reportName = reportName;
    }
    
    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    
    public List<String> getClientIds() { return clientIds; }
    public void setClientIds(List<String> clientIds) { this.clientIds = clientIds; }
    
    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    
    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public ScheduleDto getSchedule() { return schedule; }
    public void setSchedule(ScheduleDto schedule) { this.schedule = schedule; }
    
    // Inner class for schedule
    public static class ScheduleDto {
        @JsonProperty("enabled")
        private boolean enabled;
        
        @JsonProperty("frequency")
        private String frequency; // DAILY, WEEKLY, MONTHLY
        
        @JsonProperty("time")
        private String time; // HH:mm format
        
        @JsonProperty("recipients")
        private List<String> recipients;
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
    }
}
