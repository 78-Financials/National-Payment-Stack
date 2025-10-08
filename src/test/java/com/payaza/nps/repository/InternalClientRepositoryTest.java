package com.payaza.nps.repository;

import com.payaza.nps.model.InternalClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InternalClientRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration,org.springframework.boot.autoconfigure.cloud.aws.autoconfigure.sqs.SqsAutoConfiguration,org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration,org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration"
})
public class InternalClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InternalClientRepository clientRepository;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new InternalClient();
        testClient.setClientId("TEST");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test_api_key_1234567890_secure");
        testClient.setTransactionPrefix("TST");
        testClient.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        testClient.setActive(true);
        testClient.setRateLimitPerMinute(100);
        testClient.setDescription("Test client for unit testing");
        testClient.setContactEmail("test@example.com");
        testClient.setContactPhone("+1234567890");
        testClient.setCreatedBy("TEST");
        testClient.setUpdatedBy("TEST");
    }

    @Test
    void testSaveAndFindById() {
        // Act
        InternalClient saved = clientRepository.save(testClient);
        entityManager.flush();
        entityManager.clear();

        Optional<InternalClient> found = clientRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        InternalClient client = found.get();
        assertEquals("TEST", client.getClientId());
        assertEquals("Test Client", client.getClientName());
        assertEquals("test_api_key_1234567890_secure", client.getApiKey());
        assertEquals("TST", client.getTransactionPrefix());
        assertEquals(Set.of("pacs008", "acmt023"), client.getAllowedEndpoints());
        assertTrue(client.isActive());
        assertEquals(100, client.getRateLimitPerMinute());
        assertNotNull(client.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
    }

    @Test
    void testFindByApiKey() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act
        Optional<InternalClient> found = clientRepository.findByApiKey("test_api_key_1234567890_secure");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("TEST", found.get().getClientId());
    }

    @Test
    void testFindByApiKey_NotFound() {
        // Act
        Optional<InternalClient> found = clientRepository.findByApiKey("nonexistent_key");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByClientId() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act
        Optional<InternalClient> found = clientRepository.findByClientId("TEST");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("TEST", found.get().getClientId());
    }

    @Test
    void testFindByTransactionPrefix() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act
        Optional<InternalClient> found = clientRepository.findByTransactionPrefix("TST");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("TST", found.get().getTransactionPrefix());
    }

    @Test
    void testFindByActiveTrue() {
        // Arrange
        InternalClient activeClient = new InternalClient();
        activeClient.setClientId("ACTIVE");
        activeClient.setClientName("Active Client");
        activeClient.setApiKey("active_api_key_1234567890_secure");
        activeClient.setTransactionPrefix("ACT");
        activeClient.setAllowedEndpoints(Set.of("pacs008"));
        activeClient.setActive(true);
        activeClient.setRateLimitPerMinute(100);
        activeClient.setCreatedBy("TEST");
        activeClient.setUpdatedBy("TEST");

        InternalClient inactiveClient = new InternalClient();
        inactiveClient.setClientId("INACTIVE");
        inactiveClient.setClientName("Inactive Client");
        inactiveClient.setApiKey("inactive_api_key_1234567890_secure");
        inactiveClient.setTransactionPrefix("INA");
        inactiveClient.setAllowedEndpoints(Set.of("pacs008"));
        inactiveClient.setActive(false);
        inactiveClient.setRateLimitPerMinute(100);
        inactiveClient.setCreatedBy("TEST");
        inactiveClient.setUpdatedBy("TEST");

        entityManager.persistAndFlush(activeClient);
        entityManager.persistAndFlush(inactiveClient);

        // Act
        List<InternalClient> activeClients = clientRepository.findByActiveTrue();

        // Assert
        assertEquals(1, activeClients.size());
        assertEquals("ACTIVE", activeClients.get(0).getClientId());
        assertTrue(activeClients.get(0).isActive());
    }

    @Test
    void testFindByActive() {
        // Arrange
        InternalClient activeClient = new InternalClient();
        activeClient.setClientId("ACTIVE");
        activeClient.setClientName("Active Client");
        activeClient.setApiKey("active_api_key_1234567890_secure");
        activeClient.setTransactionPrefix("ACT");
        activeClient.setAllowedEndpoints(Set.of("pacs008"));
        activeClient.setActive(true);
        activeClient.setRateLimitPerMinute(100);
        activeClient.setCreatedBy("TEST");
        activeClient.setUpdatedBy("TEST");

        InternalClient inactiveClient = new InternalClient();
        inactiveClient.setClientId("INACTIVE");
        inactiveClient.setClientName("Inactive Client");
        inactiveClient.setApiKey("inactive_api_key_1234567890_secure");
        inactiveClient.setTransactionPrefix("INA");
        inactiveClient.setAllowedEndpoints(Set.of("pacs008"));
        inactiveClient.setActive(false);
        inactiveClient.setRateLimitPerMinute(100);
        inactiveClient.setCreatedBy("TEST");
        inactiveClient.setUpdatedBy("TEST");

        entityManager.persistAndFlush(activeClient);
        entityManager.persistAndFlush(inactiveClient);

        // Act
        List<InternalClient> activeClients = clientRepository.findByActive(true);
        List<InternalClient> inactiveClients = clientRepository.findByActive(false);

        // Assert
        assertEquals(1, activeClients.size());
        assertEquals("ACTIVE", activeClients.get(0).getClientId());

        assertEquals(1, inactiveClients.size());
        assertEquals("INACTIVE", inactiveClients.get(0).getClientId());
    }

    @Test
    void testFindByCreatedBy() {
        // Arrange
        testClient.setCreatedBy("ADMIN");
        entityManager.persistAndFlush(testClient);

        InternalClient otherClient = new InternalClient();
        otherClient.setClientId("OTHER");
        otherClient.setClientName("Other Client");
        otherClient.setApiKey("other_api_key_1234567890_secure");
        otherClient.setTransactionPrefix("OTH");
        otherClient.setAllowedEndpoints(Set.of("pacs008"));
        otherClient.setActive(true);
        otherClient.setRateLimitPerMinute(100);
        otherClient.setCreatedBy("SYSTEM");
        otherClient.setUpdatedBy("SYSTEM");
        entityManager.persistAndFlush(otherClient);

        // Act
        List<InternalClient> adminClients = clientRepository.findByCreatedBy("ADMIN");

        // Assert
        assertEquals(1, adminClients.size());
        assertEquals("TEST", adminClients.get(0).getClientId());
        assertEquals("ADMIN", adminClients.get(0).getCreatedBy());
    }

    @Test
    void testExistsByClientId() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act & Assert
        assertTrue(clientRepository.existsByClientId("TEST"));
        assertFalse(clientRepository.existsByClientId("NONEXISTENT"));
    }

    @Test
    void testExistsByApiKey() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act & Assert
        assertTrue(clientRepository.existsByApiKey("test_api_key_1234567890_secure"));
        assertFalse(clientRepository.existsByApiKey("nonexistent_key"));
    }

    @Test
    void testExistsByTransactionPrefix() {
        // Arrange
        entityManager.persistAndFlush(testClient);

        // Act & Assert
        assertTrue(clientRepository.existsByTransactionPrefix("TST"));
        assertFalse(clientRepository.existsByTransactionPrefix("NONEXISTENT"));
    }

    @Test
    void testUpdateLastAccessedAt() {
        // Arrange
        entityManager.persistAndFlush(testClient);
        LocalDateTime newTimestamp = LocalDateTime.now().plusHours(1);

        // Act
        int updated = clientRepository.updateLastAccessedAt("TEST", newTimestamp);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertEquals(1, updated);
        Optional<InternalClient> found = clientRepository.findByClientId("TEST");
        assertTrue(found.isPresent());
        assertNotNull(found.get().getLastAccessedAt());
    }

    @Test
    void testFindActiveClientsWithEndpointAccess() {
        // Arrange
        InternalClient clientWithEndpoint = new InternalClient();
        clientWithEndpoint.setClientId("WITH_END");
        clientWithEndpoint.setClientName("Client With Endpoint");
        clientWithEndpoint.setApiKey("with_endpoint_api_key");
        clientWithEndpoint.setTransactionPrefix("WIT");
        clientWithEndpoint.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        clientWithEndpoint.setActive(true);
        clientWithEndpoint.setRateLimitPerMinute(100);
        clientWithEndpoint.setCreatedBy("TEST");
        clientWithEndpoint.setUpdatedBy("TEST");

        InternalClient clientWithoutEndpoint = new InternalClient();
        clientWithoutEndpoint.setClientId("WITHOUT_EN");
        clientWithoutEndpoint.setClientName("Client Without Endpoint");
        clientWithoutEndpoint.setApiKey("without_endpoint_api_key");
        clientWithoutEndpoint.setTransactionPrefix("WOT");
        clientWithoutEndpoint.setAllowedEndpoints(Set.of("pacs002"));
        clientWithoutEndpoint.setActive(true);
        clientWithoutEndpoint.setRateLimitPerMinute(100);
        clientWithoutEndpoint.setCreatedBy("TEST");
        clientWithoutEndpoint.setUpdatedBy("TEST");

        entityManager.persistAndFlush(clientWithEndpoint);
        entityManager.persistAndFlush(clientWithoutEndpoint);

        // Act
        List<InternalClient> clientsWithPacs008 = clientRepository.findActiveClientsWithEndpointAccess("pacs008");

        // Assert
        assertEquals(1, clientsWithPacs008.size());
        assertEquals("WITH_END", clientsWithPacs008.get(0).getClientId());
    }

    @Test
    void testFindByCreatedAtAfter() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.now().minusHours(1);
        
        // Create a unique client for this test to avoid conflicts
        InternalClient recentClient = new InternalClient();
        recentClient.setClientId("RECENT");
        recentClient.setClientName("Recent Client");
        recentClient.setApiKey("recent_api_key_1234567890_secure");
        recentClient.setTransactionPrefix("REC");
        recentClient.setAllowedEndpoints(Set.of("pacs008"));
        recentClient.setActive(true);
        recentClient.setRateLimitPerMinute(100);
        recentClient.setCreatedAt(baseTime.plusMinutes(30));
        recentClient.setCreatedBy("TEST");
        recentClient.setUpdatedBy("TEST");
        entityManager.persistAndFlush(recentClient);

        InternalClient olderClient = new InternalClient();
        olderClient.setClientId("OLDER");
        olderClient.setClientName("Older Client");
        olderClient.setApiKey("older_api_key_1234567890_secure");
        olderClient.setTransactionPrefix("OLD");
        olderClient.setAllowedEndpoints(Set.of("pacs008"));
        olderClient.setActive(true);
        olderClient.setRateLimitPerMinute(100);
        olderClient.setCreatedAt(baseTime.minusMinutes(30));
        olderClient.setCreatedBy("TEST");
        olderClient.setUpdatedBy("TEST");
        entityManager.persistAndFlush(olderClient);

        // Clear the entity manager to ensure fresh data
        entityManager.clear();

        // Act
        List<InternalClient> recentClients = clientRepository.findByCreatedAtAfter(baseTime);

        // Assert - Check that we have at least the recent client and filter by client ID to be specific
        assertTrue(recentClients.size() >= 1);
        boolean foundRecentClient = recentClients.stream()
            .anyMatch(client -> "RECENT".equals(client.getClientId()));
        assertTrue(foundRecentClient, "Should find the recent client");
        
        // Verify that the recent client is actually recent
        InternalClient foundRecent = recentClients.stream()
            .filter(client -> "RECENT".equals(client.getClientId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Recent client not found"));
        assertTrue(foundRecent.getCreatedAt().isAfter(baseTime), "Recent client should be created after base time");
    }

    @Test
    void testFindByRateLimitPerMinuteGreaterThan() {
        // Arrange
        InternalClient highLimitClient = new InternalClient();
        highLimitClient.setClientId("HIGH_LIMIT");
        highLimitClient.setClientName("High Limit Client");
        highLimitClient.setApiKey("high_limit_api_key_1234567890_secure");
        highLimitClient.setTransactionPrefix("HIG");
        highLimitClient.setAllowedEndpoints(Set.of("pacs008"));
        highLimitClient.setActive(true);
        highLimitClient.setRateLimitPerMinute(500);
        highLimitClient.setCreatedBy("TEST");
        highLimitClient.setUpdatedBy("TEST");

        entityManager.persistAndFlush(testClient); // rate limit 100
        entityManager.persistAndFlush(highLimitClient); // rate limit 500

        // Act
        List<InternalClient> highLimitClients = clientRepository.findByRateLimitPerMinuteGreaterThan(200);

        // Assert
        assertEquals(1, highLimitClients.size());
        assertEquals("HIGH_LIMIT", highLimitClients.get(0).getClientId());
        assertEquals(500, highLimitClients.get(0).getRateLimitPerMinute());
    }

    @Test
    void testCountActiveClients() {
        // Arrange
        InternalClient activeClient = new InternalClient();
        activeClient.setClientId("ACTIVE");
        activeClient.setClientName("Active Client");
        activeClient.setApiKey("active_api_key_1234567890_secure");
        activeClient.setTransactionPrefix("ACT");
        activeClient.setAllowedEndpoints(Set.of("pacs008"));
        activeClient.setActive(true);
        activeClient.setRateLimitPerMinute(100);
        activeClient.setCreatedBy("TEST");
        activeClient.setUpdatedBy("TEST");

        InternalClient inactiveClient = new InternalClient();
        inactiveClient.setClientId("INACTIVE");
        inactiveClient.setClientName("Inactive Client");
        inactiveClient.setApiKey("inactive_api_key_1234567890_secure");
        inactiveClient.setTransactionPrefix("INA");
        inactiveClient.setAllowedEndpoints(Set.of("pacs008"));
        inactiveClient.setActive(false);
        inactiveClient.setRateLimitPerMinute(100);
        inactiveClient.setCreatedBy("TEST");
        inactiveClient.setUpdatedBy("TEST");

        entityManager.persistAndFlush(activeClient);
        entityManager.persistAndFlush(inactiveClient);

        // Act
        long activeCount = clientRepository.countActiveClients();

        // Assert
        assertEquals(1, activeCount);
    }

    @Test
    void testFindByClientNameContainingIgnoreCase() {
        // Arrange
        testClient.setClientName("Test Banking Client");
        entityManager.persistAndFlush(testClient);

        InternalClient otherClient = new InternalClient();
        otherClient.setClientId("OTHER");
        otherClient.setClientName("Other Finance Client");
        otherClient.setApiKey("other_api_key_1234567890_secure");
        otherClient.setTransactionPrefix("OTH");
        otherClient.setAllowedEndpoints(Set.of("pacs008"));
        otherClient.setActive(true);
        otherClient.setRateLimitPerMinute(100);
        otherClient.setCreatedBy("TEST");
        otherClient.setUpdatedBy("TEST");
        entityManager.persistAndFlush(otherClient);

        // Act
        List<InternalClient> bankingClients = clientRepository.findByClientNameContainingIgnoreCase("banking");

        // Assert
        assertEquals(1, bankingClients.size());
        assertEquals("TEST", bankingClients.get(0).getClientId());
    }

    @Test
    void testUniqueConstraints() {
        // Arrange - First client
        entityManager.persistAndFlush(testClient);

        // Act & Assert - Try to create client with duplicate client ID
        InternalClient duplicateClientId = new InternalClient();
        duplicateClientId.setClientId("TEST"); // Duplicate
        duplicateClientId.setClientName("Duplicate Client");
        duplicateClientId.setApiKey("duplicate_api_key_1234567890_secure");
        duplicateClientId.setTransactionPrefix("DUP");
        duplicateClientId.setAllowedEndpoints(Set.of("pacs008"));
        duplicateClientId.setActive(true);
        duplicateClientId.setRateLimitPerMinute(100);
        duplicateClientId.setCreatedBy("TEST");
        duplicateClientId.setUpdatedBy("TEST");

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateClientId);
        });
    }

    @Test
    void testDeleteClient() {
        // Arrange
        InternalClient saved = clientRepository.save(testClient);
        Long clientId = saved.getId();

        // Act
        clientRepository.deleteById(clientId);

        // Assert
        Optional<InternalClient> found = clientRepository.findById(clientId);
        assertFalse(found.isPresent());
    }
}
