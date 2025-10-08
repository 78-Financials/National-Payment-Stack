package com.payaza.nps.service;

import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.LoginResponseDto;
import com.payaza.nps.dto.ChangePasswordRequestDto;
import com.payaza.nps.dto.PasswordResetRequestDto;
import com.payaza.nps.dto.PasswordResetConfirmDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private InternalClientRepository clientRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordService passwordService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private AccountLockoutService accountLockoutService;

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
        testClient.setEmail("test@example.com");
        testClient.setPassword("encoded_password");
        testClient.setApiKey("test_api_key_12345");
        testClient.setClientType("BANK");
        testClient.setActive(true);
        testClient.setLastActivity(LocalDateTime.now());

        // Setup login request
        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
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

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(clientRepository).findByEmail("test@example.com");
        verify(accountLockoutService).recordSuccessfulLogin("test@example.com");
        verify(jwtTokenProvider).generateToken(testClient);
        verify(jwtTokenProvider).generateRefreshToken(testClient);
        verify(jwtTokenProvider).getExpirationDate("jwt_token_12345");
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(accountLockoutService).recordFailedLogin("test@example.com");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void login_WithLockedAccount_ShouldThrowException() {
        // Given
        when(accountLockoutService.isAccountLocked("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account is locked due to too many failed login attempts");

        verify(accountLockoutService).isAccountLocked("test@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_WithClientNotFound_ShouldThrowException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(clientRepository).findByEmail("test@example.com");
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
        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));
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
        verify(clientRepository).findByClientId("TEST_CLIENT");
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
        when(clientRepository.findByClientId("NON_EXISTENT_CLIENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client not found or inactive");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getClientIdFromToken(token);
        verify(clientRepository).findByClientId("NON_EXISTENT_CLIENT");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void refreshToken_WithInactiveClient_ShouldThrowException() {
        // Given
        String token = "valid_token";
        testClient.setActive(false);
        
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getClientIdFromToken(token)).thenReturn("TEST_CLIENT");
        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client not found or inactive");

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getClientIdFromToken(token);
        verify(clientRepository).findByClientId("TEST_CLIENT");
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void logout_ShouldCompleteSuccessfully() {
        // When
        authService.logout();

        // Then - should not throw any exception
        assertThatCode(() -> authService.logout()).doesNotThrowAnyException();
    }

    @Test
    void changePassword_WithValidRequest_ShouldReturnTrue() {
        // Given
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("old_password");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("NewPassword@123");

        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(passwordService.verifyPassword("old_password", "encoded_password")).thenReturn(true);
        when(passwordService.isPasswordStrong("NewPassword@123")).thenReturn(true);

        // When
        boolean result = authService.changePassword(request, "test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
        verify(passwordService).verifyPassword("old_password", "encoded_password");
        verify(passwordService).isPasswordStrong("NewPassword@123");
        verify(passwordService).encodePassword("NewPassword@123");
        verify(clientRepository).save(testClient);
    }

    @Test
    void changePassword_WithMismatchedPasswords_ShouldThrowException() {
        // Given
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("old_password");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("DifferentPassword@123");

        // When & Then
        assertThatThrownBy(() -> authService.changePassword(request, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password and confirm password do not match");
    }

    @Test
    void changePassword_WithInvalidCurrentPassword_ShouldThrowException() {
        // Given
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setCurrentPassword("wrong_password");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("NewPassword@123");

        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(passwordService.verifyPassword("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.changePassword(request, "test@example.com"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid current password");
    }

    @Test
    void requestPasswordReset_ShouldReturnTrue() {
        // Given
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setEmail("test@example.com");

        when(passwordResetService.initiatePasswordReset("test@example.com")).thenReturn(true);

        // When
        boolean result = authService.requestPasswordReset(request);

        // Then
        assertThat(result).isTrue();
        verify(passwordResetService).initiatePasswordReset("test@example.com");
    }

    @Test
    void confirmPasswordReset_WithValidToken_ShouldReturnTrue() {
        // Given
        PasswordResetConfirmDto request = new PasswordResetConfirmDto();
        request.setToken("valid_token");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("NewPassword@123");

        when(passwordResetService.confirmPasswordReset("valid_token", "NewPassword@123")).thenReturn(true);

        // When
        boolean result = authService.confirmPasswordReset(request);

        // Then
        assertThat(result).isTrue();
        verify(passwordResetService).confirmPasswordReset("valid_token", "NewPassword@123");
    }

    @Test
    void unlockAccount_ShouldReturnTrue() {
        // Given
        when(accountLockoutService.unlockAccount("test@example.com")).thenReturn(true);

        // When
        boolean result = authService.unlockAccount("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(accountLockoutService).unlockAccount("test@example.com");
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
}
