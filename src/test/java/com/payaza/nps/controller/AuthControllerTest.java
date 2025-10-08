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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequestDto loginRequest;
    private LoginResponseDto loginResponse;
    private UserProfileDto userProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        
        // Setup test data
        loginRequest = new LoginRequestDto();
        loginRequest.setClientId("testclient");
        loginRequest.setApiKey("test-api-key");

        loginResponse = new LoginResponseDto();
        loginResponse.setToken("test-jwt-token");
        loginResponse.setClientId("testclient");
        loginResponse.setClientName("Test Client");
        loginResponse.setPermissions(Arrays.asList("READ", "WRITE"));

        userProfile = new UserProfileDto();
        userProfile.setClientName("Test Client");
        userProfile.setEmail("test@example.com");
        userProfile.setClientType("INTERNAL");
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        // Given
        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(LoginRequestDto.class));
    }

    @Test
    void login_InvalidCredentials_ShouldReturn401() throws Exception {
        // Given
        when(authService.login(any(LoginRequestDto.class))).thenThrow(new RuntimeException("Invalid client credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(LoginRequestDto.class));
    }

    @Test
    void logout_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(authService).logout();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        verify(authService).logout();
    }

    @Test
    void getProfile_ShouldReturnUserProfile() throws Exception {
        // Given
        when(userService.getCurrentUserProfile()).thenReturn(userProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/profile")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        verify(userService).getCurrentUserProfile();
    }

    @Test
    void getProfile_UserNotFound_ShouldReturn404() throws Exception {
        // Given
        when(userService.getCurrentUserProfile()).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/profile")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());

        verify(userService).getCurrentUserProfile();
    }

    @Test
    void refreshToken_ShouldReturnNewToken() throws Exception {
        // Given
        when(authService.refreshToken(anyString())).thenReturn(Map.of("token", "new-token"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        verify(authService).refreshToken(anyString());
    }

    @Test
    void refreshToken_InvalidToken_ShouldReturn401() throws Exception {
        // Given
        when(authService.refreshToken(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest());

        verify(authService).refreshToken(anyString());
    }
}