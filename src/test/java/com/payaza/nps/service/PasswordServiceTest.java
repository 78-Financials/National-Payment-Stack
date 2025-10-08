package com.payaza.nps.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordService
 */
@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void encodePassword_WithValidPassword_ShouldReturnEncodedPassword() {
        // Given
        String rawPassword = "TestPassword@123";
        String encodedPassword = "encoded_password_hash";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // When
        String result = passwordService.encodePassword(rawPassword);

        // Then
        assertThat(result).isEqualTo(encodedPassword);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void encodePassword_WithNullPassword_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> passwordService.encodePassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void encodePassword_WithEmptyPassword_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> passwordService.encodePassword(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty");
    }

    @Test
    void verifyPassword_WithValidPassword_ShouldReturnTrue() {
        // Given
        String rawPassword = "TestPassword@123";
        String encodedPassword = "encoded_password_hash";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        boolean result = passwordService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertThat(result).isTrue();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void verifyPassword_WithInvalidPassword_ShouldReturnFalse() {
        // Given
        String rawPassword = "WrongPassword@123";
        String encodedPassword = "encoded_password_hash";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When
        boolean result = passwordService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertThat(result).isFalse();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void verifyPassword_WithNullPassword_ShouldReturnFalse() {
        // When
        boolean result = passwordService.verifyPassword(null, "encoded_password");

        // Then
        assertThat(result).isFalse();
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void verifyPassword_WithNullEncodedPassword_ShouldReturnFalse() {
        // When
        boolean result = passwordService.verifyPassword("password", null);

        // Then
        assertThat(result).isFalse();
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void generateResetToken_ShouldReturnValidToken() {
        // When
        String token = passwordService.generateResetToken();

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        // Token should be base64 URL encoded (no padding)
        assertThat(token).doesNotContain("=");
        assertThat(token).doesNotContain("+");
        assertThat(token).doesNotContain("/");
    }

    @Test
    void generateSecurePassword_WithDefaultLength_ShouldReturnValidPassword() {
        // When
        String password = passwordService.generateSecurePassword(8);

        // Then
        assertThat(password).isNotNull();
        assertThat(password).hasSize(8);
        // Should contain at least one character from each required set
        assertThat(password).matches(".*[A-Z].*"); // At least one uppercase
        assertThat(password).matches(".*[a-z].*"); // At least one lowercase
        assertThat(password).matches(".*[0-9].*"); // At least one digit
        assertThat(password).matches(".*[!@#$%^&*].*"); // At least one special char
    }

    @Test
    void generateSecurePassword_WithCustomLength_ShouldReturnValidPassword() {
        // When
        String password = passwordService.generateSecurePassword(12);

        // Then
        assertThat(password).isNotNull();
        assertThat(password).hasSize(12);
    }

    @Test
    void generateSecurePassword_WithLengthLessThan8_ShouldUseMinimumLength() {
        // When
        String password = passwordService.generateSecurePassword(5);

        // Then
        assertThat(password).isNotNull();
        assertThat(password).hasSize(8); // Should use minimum length of 8
    }

    @Test
    void isPasswordStrong_WithStrongPassword_ShouldReturnTrue() {
        // Given
        String strongPassword = "StrongPass@123";

        // When
        boolean result = passwordService.isPasswordStrong(strongPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isPasswordStrong_WithWeakPassword_ShouldReturnFalse() {
        // Given
        String weakPassword = "weak";

        // When
        boolean result = passwordService.isPasswordStrong(weakPassword);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPasswordStrong_WithNullPassword_ShouldReturnFalse() {
        // When
        boolean result = passwordService.isPasswordStrong(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPasswordStrong_WithPasswordMissingUppercase_ShouldReturnFalse() {
        // Given
        String password = "lowercase123!";

        // When
        boolean result = passwordService.isPasswordStrong(password);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPasswordStrong_WithPasswordMissingLowercase_ShouldReturnFalse() {
        // Given
        String password = "UPPERCASE123!";

        // When
        boolean result = passwordService.isPasswordStrong(password);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPasswordStrong_WithPasswordMissingDigit_ShouldReturnFalse() {
        // Given
        String password = "NoDigitsHere!";

        // When
        boolean result = passwordService.isPasswordStrong(password);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isPasswordStrong_WithPasswordMissingSpecialChar_ShouldReturnFalse() {
        // Given
        String password = "NoSpecialChars123";

        // When
        boolean result = passwordService.isPasswordStrong(password);

        // Then
        assertThat(result).isFalse();
    }
}
