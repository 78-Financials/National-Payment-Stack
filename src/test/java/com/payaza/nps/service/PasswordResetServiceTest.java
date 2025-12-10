package com.payaza.nps.service;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordResetService
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private InternalClientRepository clientRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new InternalClient();
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setEmail("test@example.com");
        testClient.setPassword("encoded_password");
        testClient.setActive(true);
    }

    @Test
    void initiatePasswordReset_WithValidEmail_ShouldReturnTrue() {
        // Given
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(passwordService.generateResetToken()).thenReturn("reset_token_123");
        doNothing().when(notificationService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // When
        boolean result = passwordResetService.initiatePasswordReset("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
        verify(passwordService).generateResetToken();
        verify(clientRepository).save(testClient);
        verify(notificationService).sendPasswordResetEmail("test@example.com", "reset_token_123", "Test Client");
    }

    @Test
    void initiatePasswordReset_WithNonExistentEmail_ShouldReturnTrue() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = passwordResetService.initiatePasswordReset("nonexistent@example.com");

        // Then
        assertThat(result).isTrue(); // Should return true to prevent email enumeration
        verify(clientRepository).findByEmail("nonexistent@example.com");
        verify(passwordService, never()).generateResetToken();
        verify(clientRepository, never()).save(any());
    }

    @Test
    void confirmPasswordReset_WithValidToken_ShouldReturnTrue() {
        // Given
        testClient.setPasswordResetToken("valid_token");
        testClient.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        when(clientRepository.findByPasswordResetToken("valid_token")).thenReturn(Optional.of(testClient));
        when(passwordService.isPasswordStrong("NewPassword@123")).thenReturn(true);
        when(passwordService.encodePassword("NewPassword@123")).thenReturn("new_encoded_password");

        // When
        boolean result = passwordResetService.confirmPasswordReset("valid_token", "NewPassword@123");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByPasswordResetToken("valid_token");
        verify(passwordService).isPasswordStrong("NewPassword@123");
        verify(passwordService).encodePassword("NewPassword@123");
        verify(clientRepository).save(testClient);
        
        // Verify that reset token fields are cleared
        assertThat(testClient.getPasswordResetToken()).isNull();
        assertThat(testClient.getPasswordResetExpires()).isNull();
        assertThat(testClient.getLoginAttempts()).isEqualTo(0);
    }

    @Test
    void confirmPasswordReset_WithInvalidToken_ShouldReturnFalse() {
        // Given
        when(clientRepository.findByPasswordResetToken("invalid_token")).thenReturn(Optional.empty());

        // When
        boolean result = passwordResetService.confirmPasswordReset("invalid_token", "NewPassword@123");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByPasswordResetToken("invalid_token");
        verify(passwordService, never()).isPasswordStrong(anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void confirmPasswordReset_WithExpiredToken_ShouldReturnFalse() {
        // Given
        testClient.setPasswordResetToken("expired_token");
        testClient.setPasswordResetExpires(LocalDateTime.now().minusHours(1)); // Expired
        
        when(clientRepository.findByPasswordResetToken("expired_token")).thenReturn(Optional.of(testClient));

        // When
        boolean result = passwordResetService.confirmPasswordReset("expired_token", "NewPassword@123");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByPasswordResetToken("expired_token");
        verify(passwordService, never()).isPasswordStrong(anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void confirmPasswordReset_WithWeakPassword_ShouldReturnFalse() {
        // Given
        testClient.setPasswordResetToken("valid_token");
        testClient.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        when(clientRepository.findByPasswordResetToken("valid_token")).thenReturn(Optional.of(testClient));
        when(passwordService.isPasswordStrong("weak")).thenReturn(false);

        // When
        boolean result = passwordResetService.confirmPasswordReset("valid_token", "weak");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByPasswordResetToken("valid_token");
        verify(passwordService).isPasswordStrong("weak");
        verify(passwordService, never()).encodePassword(anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void isValidResetToken_WithValidToken_ShouldReturnTrue() {
        // Given
        testClient.setPasswordResetToken("valid_token");
        testClient.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        
        when(clientRepository.findByPasswordResetToken("valid_token")).thenReturn(Optional.of(testClient));

        // When
        boolean result = passwordResetService.isValidResetToken("valid_token");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByPasswordResetToken("valid_token");
    }

    @Test
    void isValidResetToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        when(clientRepository.findByPasswordResetToken("invalid_token")).thenReturn(Optional.empty());

        // When
        boolean result = passwordResetService.isValidResetToken("invalid_token");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByPasswordResetToken("invalid_token");
    }

    @Test
    void isValidResetToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean result = passwordResetService.isValidResetToken(null);

        // Then
        assertThat(result).isFalse();
        verify(clientRepository, never()).findByPasswordResetToken(anyString());
    }

    @Test
    void isValidResetToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean result = passwordResetService.isValidResetToken("");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository, never()).findByPasswordResetToken(anyString());
    }
}
