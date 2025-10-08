package com.payaza.nps.service;

import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private InternalClientRegistry clientRegistry;

    @InjectMocks
    private AuthService authService;

    private InternalClient testClient;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = new InternalClient();
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test_api_key_12345");
        testClient.setClientType("BANK");
        testClient.setActive(true);
        testClient.setLastActivity(LocalDateTime.now());

        // Setup login request
        loginRequest = new LoginRequestDto();
        loginRequest.setClientId("TEST_CLIENT");
        loginRequest.setApiKey("test_api_key_12345");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        when(clientRegistry.getClientByApiKey("test_api_key_12345")).thenReturn(testClient);
        when(jwtTokenProvider.generateToken(testClient)).thenReturn("jwt_token_12345");
        when(jwtTokenProvider.generateRefreshToken(testClient)).thenReturn("refresh_token_12345");
        when(jwtTokenProvider.getExpirationDate("jwt_token_12345")).thenReturn(LocalDateTime.now().plusHours(1));

        // When
        LoginResponseDto response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getClientId()).isEqualTo("TEST_CLIENT");
        assertThat(response.getClientName()).isEqualTo("Test Client");
        assertThat(response.getToken()).isEqualTo("jwt_token_12345");
        assertThat(response.getClientType()).isEqualTo("BANK");
        assertThat(response.getPermissions()).isNotEmpty();
        assertThat(response.getExpiresAt()).isNotNull();

        verify(clientRegistry).getClientByApiKey("test_api_key_12345");
        verify(jwtTokenProvider).generateToken(testClient);
        verify(jwtTokenProvider).generateRefreshToken(testClient);
        verify(jwtTokenProvider).getExpirationDate("jwt_token_12345");
        // Note: updateClient is commented out in the actual service implementation
    }

    @Test
    void login_WithInvalidApiKey_ShouldThrowException() {
        // Given - Create a request with invalid API key
        LoginRequestDto invalidRequest = new LoginRequestDto();
        invalidRequest.setClientId("TEST_CLIENT");
        invalidRequest.setApiKey("invalid_api_key");
        
        when(clientRegistry.getClientByApiKey("invalid_api_key")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid client credentials");

        verify(clientRegistry).getClientByApiKey("invalid_api_key");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void login_WithMismatchedClientId_ShouldThrowException() {
        // Given
        InternalClient differentClient = new InternalClient();
        differentClient.setClientId("DIFFERENT_CLIENT");
        differentClient.setApiKey("test_api_key_12345");
        
        when(clientRegistry.getClientByApiKey("test_api_key_12345")).thenReturn(differentClient);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid client credentials");

        verify(clientRegistry).getClientByApiKey("test_api_key_12345");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void login_WithInactiveClient_ShouldThrowException() {
        // Given
        testClient.setActive(false);
        when(clientRegistry.getClientByApiKey("test_api_key_12345")).thenReturn(testClient);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client account is inactive");

        verify(clientRegistry).getClientByApiKey("test_api_key_12345");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() {
        // Given
        String oldToken = "old_jwt_token";
        String newToken = "new_jwt_token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        
        when(jwtTokenProvider.validateToken(oldToken)).thenReturn(true);
        when(jwtTokenProvider.getClientIdFromToken(oldToken)).thenReturn("TEST_CLIENT");
        when(clientRegistry.getClientById("TEST_CLIENT")).thenReturn(testClient);
        when(jwtTokenProvider.generateToken(testClient)).thenReturn(newToken);
        when(jwtTokenProvider.getExpirationDate(newToken)).thenReturn(expiresAt);

        // When
        Map<String, String> response = authService.refreshToken(oldToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("token")).isEqualTo(newToken);
        assertThat(response.get("expiresAt")).isEqualTo(expiresAt.toString());

        verify(jwtTokenProvider).validateToken(oldToken);
        verify(jwtTokenProvider).getClientIdFromToken(oldToken);
        verify(clientRegistry).getClientById("TEST_CLIENT");
        verify(jwtTokenProvider).generateToken(testClient);
        verify(jwtTokenProvider).getExpirationDate(newToken);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid_token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid or expired token");

        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getClientIdFromToken(any());
    }

    @Test
    void refreshToken_WithNonExistentClient_ShouldThrowException() {
        // Given
        String token = "valid_token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClientIdFromToken(token)).thenReturn("NON_EXISTENT_CLIENT");
        when(clientRegistry.getClientById("NON_EXISTENT_CLIENT")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client not found or inactive");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getClientIdFromToken(token);
        verify(clientRegistry).getClientById("NON_EXISTENT_CLIENT");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void refreshToken_WithInactiveClient_ShouldThrowException() {
        // Given
        String token = "valid_token";
        testClient.setActive(false);
        
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClientIdFromToken(token)).thenReturn("TEST_CLIENT");
        when(clientRegistry.getClientById("TEST_CLIENT")).thenReturn(testClient);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client not found or inactive");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getClientIdFromToken(token);
        verify(clientRegistry).getClientById("TEST_CLIENT");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void logout_ShouldCompleteSuccessfully() {
        // When
        authService.logout();

        // Then - should not throw any exception
        // In a real implementation, you would verify that the token is blacklisted
        assertThatCode(() -> authService.logout()).doesNotThrowAnyException();
    }

    @Test
    void getClientPermissions_WithBankClient_ShouldReturnBankPermissions() {
        // Given
        testClient.setClientType("BANK");

        // When
        List<String> permissions = authService.getClientPermissions(testClient);

        // Then
        assertThat(permissions).containsExactlyInAnyOrder(
            "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
            "TRANSACTION_HISTORY", "CLIENT_PROFILE"
        );
    }

    @Test
    void getClientPermissions_WithFinClient_ShouldReturnFinPermissions() {
        // Given
        testClient.setClientType("FIN");

        // When
        List<String> permissions = authService.getClientPermissions(testClient);

        // Then
        assertThat(permissions).containsExactlyInAnyOrder(
            "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
            "TRANSACTION_HISTORY", "CLIENT_PROFILE", "ANALYTICS_VIEW"
        );
    }

    @Test
    void getClientPermissions_WithPayClient_ShouldReturnPayPermissions() {
        // Given
        testClient.setClientType("PAY");

        // When
        List<String> permissions = authService.getClientPermissions(testClient);

        // Then
        assertThat(permissions).containsExactlyInAnyOrder(
            "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
            "TRANSACTION_HISTORY", "CLIENT_PROFILE", "ANALYTICS_VIEW", 
            "WEBHOOK_MANAGE"
        );
    }

    @Test
    void getClientPermissions_WithUnknownClientType_ShouldReturnDefaultPermissions() {
        // Given
        testClient.setClientType("UNKNOWN");

        // When
        List<String> permissions = authService.getClientPermissions(testClient);

        // Then
        assertThat(permissions).containsExactly("CLIENT_PROFILE");
    }

    @Test
    void getClientPermissions_WithNullClientType_ShouldReturnDefaultPermissions() {
        // Given
        testClient.setClientType(null);

        // When
        List<String> permissions = authService.getClientPermissions(testClient);

        // Then
        assertThat(permissions).containsExactlyInAnyOrder(
            "PAYMENT_INITIATE", "PAYMENT_VIEW", "ACCOUNT_VERIFY", 
            "TRANSACTION_HISTORY", "CLIENT_PROFILE"
        );
    }
}
