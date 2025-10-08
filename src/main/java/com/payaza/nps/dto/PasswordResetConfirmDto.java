package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for confirming password reset
 */
public class PasswordResetConfirmDto {
    
    @NotBlank(message = "Reset token is required")
    @JsonProperty("token")
    private String token;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @JsonProperty("newPassword")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    @JsonProperty("confirmPassword")
    private String confirmPassword;
    
    // Constructors
    public PasswordResetConfirmDto() {}
    
    public PasswordResetConfirmDto(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
