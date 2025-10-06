package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.dto.UserProfileDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.service.AuthService;
import com.payaza.nps.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDto loginRequest;
    private LoginResponseDto loginResponse;
    private UserProfileDto userProfile;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequestDto();
        loginRequest.setClientId("TEST_CLIENT");
        loginRequest.setApiKey("test_api_key_12345");

        // Setup login response
        loginResponse = new LoginResponseDto();
        loginResponse.setToken("jwt_token_12345");
        loginResponse.setClientId("TEST_CLIENT");
        loginResponse.setClientName("Test Client");
        loginResponse.setClientType("BANK");
        loginResponse.setPermissions(Arrays.asList("PAYMENT_INITIATE", "PAYMENT_VIEW"));
        loginResponse.setExpiresAt(LocalDateTime.now().plusHours(1));

        // Setup user profile
        userProfile = new UserProfileDto();
        userProfile.setId("TEST_CLIENT");
        userProfile.setClientName("Test Client");
        userProfile.setEmail("test@example.com");
        userProfile.setPhone("+1234567890");
        userProfile.setClientType("BANK");
        userProfile.setActive(true);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
        // Given
        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_12345"))
                .andExpect(jsonPath("$.clientId").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Test Client"))
                .andExpect(jsonPath("$.clientType").value("BANK"))
                .andExpect(jsonPath("$.permissions").isArray());

        verify(authService).login(any(LoginRequestDto.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
        // Given
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(LoginRequestDto.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() throws Exception {
        // Given
        Map<String, String> refreshResponse = Map.of(
            "token", "new_jwt_token_12345",
            "expiresAt", LocalDateTime.now().plusHours(1).toString()
        );
        when(authService.refreshToken(anyString())).thenReturn(refreshResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer jwt_token_12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new_jwt_token_12345"))
                .andExpect(jsonPath("$.expiresAt").exists());

        verify(authService).refreshToken("jwt_token_12345");
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "CLIENT"})
    void getProfile_WithAuthenticatedUser_ShouldReturnUserProfile() throws Exception {
        // Given
        when(userService.getCurrentUserProfile()).thenReturn(userProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Test Client"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.clientType").value("BANK"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).getCurrentUserProfile();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "CLIENT"})
    void updateProfile_WithValidData_ShouldReturnUpdatedProfile() throws Exception {
        // Given
        UserProfileDto updatedProfile = new UserProfileDto();
        updatedProfile.setId("TEST_CLIENT");
        updatedProfile.setClientName("Updated Client Name");
        updatedProfile.setEmail("updated@example.com");
        updatedProfile.setPhone("+0987654321");
        updatedProfile.setClientType("BANK");
        updatedProfile.setActive(true);

        when(userService.updateUserProfile(any(UserProfileDto.class))).thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/v1/auth/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProfile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Updated Client Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).updateUserProfile(any(UserProfileDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "CLIENT"})
    void changePassword_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).changePassword(any());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"old123\",\"newPassword\":\"new123\",\"confirmPassword\":\"new123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(any());
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).requestPasswordReset(anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(csrf())
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset instructions sent to your email"));

        verify(userService).requestPasswordReset("test@example.com");
    }

    @Test
    void resetPassword_WithValidToken_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).resetPassword(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .with(csrf())
                .param("token", "reset_token_12345")
                .param("newPassword", "newpassword123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(userService).resetPassword("reset_token_12345", "newpassword123");
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "CLIENT"})
    void logout_WithAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(authService).logout();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout();
    }

    @Test
    void getProfile_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/auth/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userProfile)))
                .andExpect(status().isUnauthorized());
    }
}
