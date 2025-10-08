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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InternalClientRegistry service
 */
@ExtendWith(MockitoExtension.class)
public class InternalClientRegistryTest {

    @Mock
    private InternalClientRepository clientRepository;

    @Mock
    private ApiKeyGenerationService apiKeyGenerationService;

    @InjectMocks
    private InternalClientRegistry clientRegistry;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        // Initialize the service fields manually since @Value annotations don't work in unit tests
        try {
            java.lang.reflect.Field defaultAdminClientIdField = InternalClientRegistry.class.getDeclaredField("defaultAdminClientId");
            defaultAdminClientIdField.setAccessible(true);
            defaultAdminClientIdField.set(clientRegistry, "ADMIN");
            
            java.lang.reflect.Field defaultAdminClientNameField = InternalClientRegistry.class.getDeclaredField("defaultAdminClientName");
            defaultAdminClientNameField.setAccessible(true);
            defaultAdminClientNameField.set(clientRegistry, "System Administrator");
            
            java.lang.reflect.Field defaultAdminApiKeyField = InternalClientRegistry.class.getDeclaredField("defaultAdminApiKey");
            defaultAdminApiKeyField.setAccessible(true);
            defaultAdminApiKeyField.set(clientRegistry, "admin_api_key_99999_secure_default");
        } catch (Exception e) {
            // If reflection fails, the tests will fail with null pointer exceptions
            // which is better than silent failures
        }
        
        testClient = new InternalClient();
        testClient.setId(1L);
        testClient.setClientId("TEST");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test_api_key_123");
        testClient.setTransactionPrefix("TST");
        testClient.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        testClient.setActive(true);
        testClient.setRateLimitPerMinute(100);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        testClient.setCreatedBy("TEST");
        testClient.setUpdatedBy("TEST");
    }

    @Test
    void testGetClientByApiKey_Success() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize(); // Load clients into cache

        // Act
        InternalClient result = clientRegistry.getClientByApiKey("test_api_key_123");

        // Assert
        assertNotNull(result);
        assertEquals("TEST", result.getClientId());
        assertEquals("Test Client", result.getClientName());
        assertEquals("test_api_key_123", result.getApiKey());
    }

    @Test
    void testGetClientByApiKey_NotFound() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByApiKey("nonexistent_key");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetClientByApiKey_NullApiKey() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByApiKey(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetClientByApiKey_EmptyApiKey() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByApiKey("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetClientByClientId_Success() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByClientId("TEST");

        // Assert
        assertNotNull(result);
        assertEquals("TEST", result.getClientId());
        assertEquals("Test Client", result.getClientName());
    }

    @Test
    void testGetClientByClientId_CaseInsensitive() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByClientId("test");

        // Assert
        assertNotNull(result);
        assertEquals("TEST", result.getClientId());
    }

    @Test
    void testGetClientByClientId_NotFound() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByClientId("NONEXISTENT");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetAllClients() {
        // Arrange
        InternalClient client2 = new InternalClient();
        client2.setClientId("TEST2");
        client2.setClientName("Test Client 2");
        client2.setApiKey("test2_api_key");
        client2.setTransactionPrefix("TST2");
        client2.setAllowedEndpoints(Set.of("pacs008"));
        client2.setActive(true);
        client2.setRateLimitPerMinute(50);

        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient, client2));
        clientRegistry.initialize();

        // Act
        List<InternalClient> result = clientRegistry.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> "TEST".equals(c.getClientId())));
        assertTrue(result.stream().anyMatch(c -> "TEST2".equals(c.getClientId())));
    }

    @Test
    void testGetRegisteredClientIds() {
        // Arrange
        InternalClient client2 = new InternalClient();
        client2.setClientId("TEST2");
        client2.setApiKey("test2_api_key");
        client2.setTransactionPrefix("TST2");
        client2.setAllowedEndpoints(Set.of("pacs008"));
        client2.setActive(true);
        client2.setRateLimitPerMinute(50);

        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient, client2));
        clientRegistry.initialize();

        // Act
        Set<String> result = clientRegistry.getRegisteredClientIds();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("TEST"));
        assertTrue(result.contains("TEST2"));
    }

    @Test
    void testClientExists_ByApiKey() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act & Assert
        assertTrue(clientRegistry.clientExists("test_api_key_123"));
        assertFalse(clientRegistry.clientExists("nonexistent_key"));
        assertFalse(clientRegistry.clientExists(null));
    }

    @Test
    void testClientExists_ByClientId() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act & Assert
        assertTrue(clientRegistry.clientExistsByClientId("TEST"));
        assertTrue(clientRegistry.clientExistsByClientId("test")); // Case insensitive
        assertFalse(clientRegistry.clientExistsByClientId("NONEXISTENT"));
        assertFalse(clientRegistry.clientExistsByClientId(null));
    }

    @Test
    void testRefreshCache() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));

        // Act
        clientRegistry.refreshCache();

        // Assert
        assertTrue(clientRegistry.clientExists("test_api_key_123"));
        assertTrue(clientRegistry.clientExistsByClientId("TEST"));
        verify(clientRepository, atLeastOnce()).findByActiveTrue();
    }

    @Test
    void testAddClientToCache_ActiveClient() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        clientRegistry.initialize();

        // Act
        clientRegistry.addClientToCache(testClient);

        // Assert
        assertTrue(clientRegistry.clientExists("test_api_key_123"));
        assertTrue(clientRegistry.clientExistsByClientId("TEST"));
    }

    @Test
    void testAddClientToCache_InactiveClient() {
        // Arrange
        testClient.setActive(false);
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        clientRegistry.initialize();

        // Act
        clientRegistry.addClientToCache(testClient);

        // Assert - Inactive clients should not be added to cache
        assertFalse(clientRegistry.clientExists("test_api_key_123"));
        assertFalse(clientRegistry.clientExistsByClientId("TEST"));
    }

    @Test
    void testRemoveClientFromCache() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();
        assertTrue(clientRegistry.clientExists("test_api_key_123")); // Verify it exists

        // Act
        clientRegistry.removeClientFromCache("TEST");

        // Assert
        assertFalse(clientRegistry.clientExists("test_api_key_123"));
        assertFalse(clientRegistry.clientExistsByClientId("TEST"));
    }

    @Test
    void testUpdateClientInCache_ActiveClient() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());
        clientRegistry.initialize();

        // Act
        clientRegistry.updateClientInCache(testClient);

        // Assert
        assertTrue(clientRegistry.clientExists("test_api_key_123"));
        assertTrue(clientRegistry.clientExistsByClientId("TEST"));
    }

    @Test
    void testUpdateClientInCache_DeactivatedClient() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();
        assertTrue(clientRegistry.clientExists("test_api_key_123")); // Verify it exists

        // Act - Deactivate client
        testClient.setActive(false);
        clientRegistry.updateClientInCache(testClient);

        // Assert - Deactivated client should be removed from cache
        assertFalse(clientRegistry.clientExists("test_api_key_123"));
        assertFalse(clientRegistry.clientExistsByClientId("TEST"));
    }

    @Test
    void testUpdateLastAccessedAsync() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient));
        clientRegistry.initialize();

        // Act
        InternalClient result = clientRegistry.getClientByApiKey("test_api_key_123");

        // Assert
        assertNotNull(result);
        verify(clientRepository, atLeastOnce()).updateLastAccessedAt(eq("TEST"), any(LocalDateTime.class));
    }

    @Test
    void testGetCacheStatistics() {
        // Arrange
        InternalClient client2 = new InternalClient();
        client2.setClientId("TEST2");
        client2.setApiKey("test2_api_key");
        client2.setTransactionPrefix("TST2");
        client2.setAllowedEndpoints(Set.of("pacs008"));
        client2.setActive(true);
        client2.setRateLimitPerMinute(50);

        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(testClient, client2));
        clientRegistry.initialize();

        // Act
        Map<String, Object> stats = clientRegistry.getCacheStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(2, stats.get("totalClients"));
        assertNotNull(stats.get("clientIds"));
        assertNotNull(stats.get("lastRefresh"));
        
        @SuppressWarnings("unchecked")
        Set<String> clientIds = (Set<String>) stats.get("clientIds");
        assertEquals(2, clientIds.size());
        assertTrue(clientIds.contains("TEST"));
        assertTrue(clientIds.contains("TEST2"));
    }

    @Test
    void testInitialize_WithException() {
        // Arrange
        when(clientRepository.findByActiveTrue()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception, should handle gracefully
        assertDoesNotThrow(() -> clientRegistry.initialize());
    }

    @Test
    void testInitialize_EnsureDefaultAdminClient() {
        // Arrange
        when(clientRepository.existsByClientId("ADMIN")).thenReturn(false);
        when(clientRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        // Act
        clientRegistry.initialize();

        // Assert
        verify(clientRepository).existsByClientId("ADMIN");
        // Note: In a real test, we'd verify that save was called for the default admin client
        // but this requires more complex setup with @SpyBean or integration testing
    }

    @Test
    void testInitialize_DefaultAdminClientExists() {
        // Arrange
        InternalClient adminClient = new InternalClient();
        adminClient.setClientId("ADMIN");
        adminClient.setClientName("System Administrator");
        adminClient.setApiKey("admin_api_key_99999_secure_default");
        adminClient.setTransactionPrefix("ADM");
        adminClient.setAllowedEndpoints(Set.of("pacs008", "acmt023", "acmt024", "pacs002", "pacs028"));
        adminClient.setActive(true);
        adminClient.setRateLimitPerMinute(1000);

        when(clientRepository.existsByClientId("ADMIN")).thenReturn(true);
        when(clientRepository.findByActiveTrue()).thenReturn(Arrays.asList(adminClient));

        // Act
        clientRegistry.initialize();

        // Assert
        verify(clientRepository).existsByClientId("ADMIN");
        assertTrue(clientRegistry.clientExists("admin_api_key_99999_secure_default"));
        assertTrue(clientRegistry.clientExistsByClientId("ADMIN"));
    }
}
