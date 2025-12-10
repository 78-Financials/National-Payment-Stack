package com.payaza.nps.dto;

import java.time.LocalDateTime;

/**
 * DTO for queue status information
 */
public class QueueStatusDto {
    
    private Long inboundQueueSize;
    private Long errorQueueSize;
    private Long outboundQueueSize;
    private LocalDateTime lastCheckedAt;
    private String status;

    // Constructors
    public QueueStatusDto() {
        this.lastCheckedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public QueueStatusDto(Long inboundQueueSize, Long errorQueueSize, Long outboundQueueSize) {
        this();
        this.inboundQueueSize = inboundQueueSize;
        this.errorQueueSize = errorQueueSize;
        this.outboundQueueSize = outboundQueueSize;
    }

    // Getters and Setters
    public Long getInboundQueueSize() {
        return inboundQueueSize;
    }

    public void setInboundQueueSize(Long inboundQueueSize) {
        this.inboundQueueSize = inboundQueueSize;
    }

    public Long getErrorQueueSize() {
        return errorQueueSize;
    }

    public void setErrorQueueSize(Long errorQueueSize) {
        this.errorQueueSize = errorQueueSize;
    }

    public Long getOutboundQueueSize() {
        return outboundQueueSize;
    }

    public void setOutboundQueueSize(Long outboundQueueSize) {
        this.outboundQueueSize = outboundQueueSize;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "QueueStatusDto{" +
                "inboundQueueSize=" + inboundQueueSize +
                ", errorQueueSize=" + errorQueueSize +
                ", outboundQueueSize=" + outboundQueueSize +
                ", lastCheckedAt=" + lastCheckedAt +
                ", status='" + status + '\'' +
                '}';
    }
}
