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
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountLockoutService
 */
@ExtendWith(MockitoExtension.class)
class AccountLockoutServiceTest {

    @Mock
    private InternalClientRepository clientRepository;

    @InjectMocks
    private AccountLockoutService accountLockoutService;

    private InternalClient testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new InternalClient();
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setEmail("test@example.com");
        testClient.setPassword("encoded_password");
        testClient.setActive(true);
        testClient.setLoginAttempts(0);
        testClient.setAccountLocked(false);
        
        // Set maxLoginAttempts using reflection since @Value doesn't work in unit tests
        java.lang.reflect.Field maxLoginAttemptsField = AccountLockoutService.class.getDeclaredField("maxLoginAttempts");
        maxLoginAttemptsField.setAccessible(true);
        maxLoginAttemptsField.set(accountLockoutService, 5);
        
        // Set lockoutDurationMinutes using reflection
        java.lang.reflect.Field lockoutDurationField = AccountLockoutService.class.getDeclaredField("lockoutDurationMinutes");
        lockoutDurationField.setAccessible(true);
        lockoutDurationField.set(accountLockoutService, 30);
    }

    @Test
    void recordFailedLogin_ShouldIncrementLoginAttempts() {
        // Given
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        accountLockoutService.recordFailedLogin("test@example.com");

        // Then
        assertThat(testClient.getLoginAttempts()).isEqualTo(1);
        assertThat(testClient.isAccountLocked()).isFalse();
        verify(clientRepository).findByEmail("test@example.com");
        verify(clientRepository).save(testClient);
    }

    @Test
    void recordFailedLogin_WithMaxAttempts_ShouldLockAccount() {
        // Given
        testClient.setLoginAttempts(4); // One less than max (5)
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        accountLockoutService.recordFailedLogin("test@example.com");

        // Then
        assertThat(testClient.getLoginAttempts()).isEqualTo(5);
        assertThat(testClient.isAccountLocked()).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
        verify(clientRepository).save(testClient);
    }

    @Test
    void recordFailedLogin_WithNonExistentEmail_ShouldNotThrowException() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> accountLockoutService.recordFailedLogin("nonexistent@example.com"))
                .doesNotThrowAnyException();

        verify(clientRepository).findByEmail("nonexistent@example.com");
        verify(clientRepository, never()).save(any());
    }

    @Test
    void recordSuccessfulLogin_ShouldResetLoginAttempts() {
        // Given
        testClient.setLoginAttempts(3);
        testClient.setAccountLocked(true);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        accountLockoutService.recordSuccessfulLogin("test@example.com");

        // Then
        assertThat(testClient.getLoginAttempts()).isEqualTo(0);
        assertThat(testClient.getLastLogin()).isNotNull();
        assertThat(testClient.getLastActivity()).isNotNull();
        verify(clientRepository).findByEmail("test@example.com");
        verify(clientRepository).save(testClient);
    }

    @Test
    void recordSuccessfulLogin_WithNonExistentEmail_ShouldNotThrowException() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> accountLockoutService.recordSuccessfulLogin("nonexistent@example.com"))
                .doesNotThrowAnyException();

        verify(clientRepository).findByEmail("nonexistent@example.com");
        verify(clientRepository, never()).save(any());
    }

    @Test
    void isAccountLocked_WithLockedAccount_ShouldReturnTrue() {
        // Given
        testClient.setAccountLocked(true);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        boolean result = accountLockoutService.isAccountLocked("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void isAccountLocked_WithUnlockedAccount_ShouldReturnFalse() {
        // Given
        testClient.setAccountLocked(false);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        boolean result = accountLockoutService.isAccountLocked("test@example.com");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void isAccountLocked_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = accountLockoutService.isAccountLocked("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void unlockAccount_WithValidEmail_ShouldReturnTrue() {
        // Given
        testClient.setAccountLocked(true);
        testClient.setLoginAttempts(5);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        boolean result = accountLockoutService.unlockAccount("test@example.com");

        // Then
        assertThat(result).isTrue();
        assertThat(testClient.isAccountLocked()).isFalse();
        assertThat(testClient.getLoginAttempts()).isEqualTo(0);
        verify(clientRepository).findByEmail("test@example.com");
        verify(clientRepository).save(testClient);
    }

    @Test
    void unlockAccount_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = accountLockoutService.unlockAccount("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByEmail("nonexistent@example.com");
        verify(clientRepository, never()).save(any());
    }

    @Test
    void getAccountStatus_ShouldReturnCorrectStatus() {
        // Given
        testClient.setAccountLocked(true);
        testClient.setLoginAttempts(3);
        testClient.setLastLogin(LocalDateTime.now().minusHours(1));
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        AccountLockoutService.AccountLockoutStatus status = accountLockoutService.getAccountStatus("test@example.com");

        // Then
        assertThat(status).isNotNull();
        assertThat(status.isLocked()).isTrue();
        assertThat(status.getLoginAttempts()).isEqualTo(3);
        assertThat(status.getLastLogin()).isNotNull();
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void getAccountStatus_WithNonExistentEmail_ShouldReturnDefaultStatus() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        AccountLockoutService.AccountLockoutStatus status = accountLockoutService.getAccountStatus("nonexistent@example.com");

        // Then
        assertThat(status).isNotNull();
        assertThat(status.isLocked()).isFalse();
        assertThat(status.getLoginAttempts()).isEqualTo(0);
        assertThat(status.getLastLogin()).isNull();
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void canUnlockAccount_WithUnlockedAccount_ShouldReturnTrue() {
        // Given
        testClient.setAccountLocked(false);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        boolean result = accountLockoutService.canUnlockAccount("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void canUnlockAccount_WithExpiredLockout_ShouldReturnTrue() {
        // Given
        testClient.setAccountLocked(true);
        testClient.setLastLogin(LocalDateTime.now().minusHours(1)); // 1 hour ago, should be unlockable
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        boolean result = accountLockoutService.canUnlockAccount("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void canUnlockAccount_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = accountLockoutService.canUnlockAccount("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }
}
