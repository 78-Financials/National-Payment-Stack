package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Webhook configuration DTO
 */
public class WebhookConfigDto {
    
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Webhook name is required")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "Webhook URL is required")
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @NotNull(message = "Event types are required")
    @JsonProperty("eventTypes")
    private List<String> eventTypes;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("retryAttempts")
    private int retryAttempts;
    
    @JsonProperty("timeoutSeconds")
    private int timeoutSeconds;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public WebhookConfigDto() {}
    
    public WebhookConfigDto(String name, String url, List<String> eventTypes) {
        this.name = name;
        this.url = url;
        this.eventTypes = eventTypes;
        this.isActive = true;
        this.retryAttempts = 3;
        this.timeoutSeconds = 30;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public List<String> getEventTypes() { return eventTypes; }
    public void setEventTypes(List<String> eventTypes) { this.eventTypes = eventTypes; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
    
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
