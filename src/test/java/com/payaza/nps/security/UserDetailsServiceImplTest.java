package com.payaza.nps.security;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDetailsServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private InternalClientRepository clientRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new InternalClient();
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setEmail("test@example.com");
        testClient.setPassword("encoded_password");
        testClient.setClientType("BANK");
        testClient.setActive(true);
        testClient.setAccountLocked(false);
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded_password");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_BANK");

        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WithNonExistentEmail_ShouldThrowException() {
        // Given
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");

        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_WithInactiveClient_ShouldThrowException() {
        // Given
        testClient.setActive(false);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("test@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User account is inactive: test@example.com");

        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WithLockedAccount_ShouldThrowException() {
        // Given
        testClient.setAccountLocked(true);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("test@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User account is locked: test@example.com");

        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_WithAdminClient_ShouldReturnAdminAuthorities() {
        // Given
        testClient.setClientType("ADMIN");
        when(clientRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testClient));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_WithFinClient_ShouldReturnFinAuthorities() {
        // Given
        testClient.setClientType("FIN");
        when(clientRepository.findByEmail("fin@example.com")).thenReturn(Optional.of(testClient));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("fin@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_FIN");
    }

    @Test
    void loadUserByUsername_WithPayClient_ShouldReturnPayAuthorities() {
        // Given
        testClient.setClientType("PAY");
        when(clientRepository.findByEmail("pay@example.com")).thenReturn(Optional.of(testClient));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("pay@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_PAY");
    }

    @Test
    void loadUserByUsername_WithNullClientType_ShouldReturnBankAuthorities() {
        // Given
        testClient.setClientType(null);
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_BANK");
    }
}
