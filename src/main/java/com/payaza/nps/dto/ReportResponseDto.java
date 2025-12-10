package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report response DTO
 */
public class ReportResponseDto {
    
    @JsonProperty("reportId")
    private String reportId;
    
    @JsonProperty("reportName")
    private String reportName;
    
    @JsonProperty("reportType")
    private String reportType;
    
    @JsonProperty("status")
    private String status; // PENDING, COMPLETED, FAILED
    
    @JsonProperty("data")
    private List<Map<String, Object>> data;
    
    @JsonProperty("summary")
    private Map<String, Object> summary;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    @JsonProperty("downloadUrl")
    private String downloadUrl;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    // Constructors
    public ReportResponseDto() {}
    
    public ReportResponseDto(String reportId, String reportName, String reportType, String status) {
        this.reportId = reportId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }
    
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
