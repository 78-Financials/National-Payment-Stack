package com.payaza.nps.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InternalClient entity validation and behavior
 */
public class InternalClientTest {

    private Validator validator;
    private InternalClient client;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        client = new InternalClient();
        client.setClientId("TEST");
        client.setClientName("Test Client");
        client.setApiKey("test_api_key_12345678901234567890");
        client.setTransactionPrefix("TST");
        client.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        client.setActive(true);
        client.setRateLimitPerMinute(100);
        client.setDescription("Test client description");
        client.setContactEmail("test@example.com");
        client.setContactPhone("+1234567890");
        client.setCreatedBy("TEST");
        client.setUpdatedBy("TEST");
    }

    @Test
    void testValidClient() {
        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testClientIdValidation_Required() {
        // Arrange
        client.setClientId(null);

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientId")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void testClientIdValidation_TooShort() {
        // Arrange
        client.setClientId("AB"); // Less than 3 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientId")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 10 characters")));
    }

    @Test
    void testClientIdValidation_TooLong() {
        // Arrange
        client.setClientId("VERYLONGID"); // 10 characters, should be valid
        client.setClientId("VERYLONGIDX"); // 11 characters, should be invalid

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientId")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 10 characters")));
    }

    @Test
    void testClientNameValidation_Required() {
        // Arrange
        client.setClientName(null);

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientName")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void testClientNameValidation_TooLong() {
        // Arrange
        client.setClientName("A".repeat(101)); // 101 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientName")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("100 characters")));
    }

    @Test
    void testApiKeyValidation_Required() {
        // Arrange
        client.setApiKey(null);

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiKey")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void testApiKeyValidation_TooShort() {
        // Arrange
        client.setApiKey("short"); // Less than 20 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiKey")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("20 and 100 characters")));
    }

    @Test
    void testApiKeyValidation_TooLong() {
        // Arrange
        client.setApiKey("A".repeat(101)); // 101 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("apiKey")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("20 and 100 characters")));
    }

    @Test
    void testTransactionPrefixValidation_Required() {
        // Arrange
        client.setTransactionPrefix(null);

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionPrefix")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void testTransactionPrefixValidation_TooShort() {
        // Arrange
        client.setTransactionPrefix("AB"); // Less than 3 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionPrefix")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 10 characters")));
    }

    @Test
    void testActiveValidation_Required() {
        // Arrange
        client.setActive(null);

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("active")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")));
    }

    @Test
    void testRateLimitValidation_Positive() {
        // Arrange
        client.setRateLimitPerMinute(0); // Not positive

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rateLimitPerMinute")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positive")));
    }

    @Test
    void testRateLimitValidation_Negative() {
        // Arrange
        client.setRateLimitPerMinute(-1); // Negative

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rateLimitPerMinute")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("positive")));
    }

    @Test
    void testEmailValidation_InvalidFormat() {
        // Arrange
        client.setContactEmail("invalid-email");

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contactEmail")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Invalid email format")));
    }

    @Test
    void testEmailValidation_ValidFormat() {
        // Arrange
        client.setContactEmail("valid@example.com");

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailValidation_TooLong() {
        // Arrange
        client.setContactEmail("a".repeat(95) + "@example.com"); // 101 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contactEmail")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("100 characters")));
    }

    @Test
    void testDescriptionValidation_TooLong() {
        // Arrange
        client.setDescription("A".repeat(501)); // 501 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("500 characters")));
    }

    @Test
    void testPhoneValidation_TooLong() {
        // Arrange
        client.setContactPhone("+12345678901234567890"); // 21 characters

        // Act
        Set<ConstraintViolation<InternalClient>> violations = validator.validate(client);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("contactPhone")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("20 characters")));
    }

    @Test
    void testIsActiveMethod() {
        // Act & Assert
        client.setActive(true);
        assertTrue(client.isActive());

        client.setActive(false);
        assertFalse(client.isActive());

        client.setActive(null);
        assertFalse(client.isActive());
    }

    @Test
    void testAddAllowedEndpoint() {
        // Arrange
        client.setAllowedEndpoints(null);

        // Act
        client.addAllowedEndpoint("pacs008");

        // Assert
        assertNotNull(client.getAllowedEndpoints());
        assertTrue(client.getAllowedEndpoints().contains("pacs008"));
    }

    @Test
    void testRemoveAllowedEndpoint() {
        // Arrange
        client.setAllowedEndpoints(Set.of("pacs008", "acmt023"));

        // Act
        client.removeAllowedEndpoint("pacs008");

        // Assert
        assertFalse(client.getAllowedEndpoints().contains("pacs008"));
        assertTrue(client.getAllowedEndpoints().contains("acmt023"));
    }

    @Test
    void testHasEndpointAccess() {
        // Arrange
        client.setAllowedEndpoints(Set.of("pacs008", "acmt023"));

        // Act & Assert
        assertTrue(client.hasEndpointAccess("pacs008"));
        assertTrue(client.hasEndpointAccess("acmt023"));
        assertFalse(client.hasEndpointAccess("pacs002"));
        assertFalse(client.hasEndpointAccess(null));

        // Test with null endpoints
        client.setAllowedEndpoints(null);
        assertFalse(client.hasEndpointAccess("pacs008"));
    }

    @Test
    void testHasPermission() {
        // Arrange
        client.setAllowedEndpoints(Set.of("pacs008", "acmt023"));

        // Act & Assert
        assertTrue(client.hasPermission("pacs008"));
        assertTrue(client.hasPermission("acmt023"));
        assertFalse(client.hasPermission("pacs002"));
    }

    @Test
    void testToString() {
        // Act
        String stringRepresentation = client.toString();

        // Assert
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("TEST"));
        assertTrue(stringRepresentation.contains("Test Client"));
        assertTrue(stringRepresentation.contains("TST"));
        assertTrue(stringRepresentation.contains("true"));
        assertTrue(stringRepresentation.contains("100"));
    }

    @Test
    void testConstructorWithParameters() {
        // Arrange
        Set<String> endpoints = Set.of("pacs008", "acmt023");

        // Act
        InternalClient newClient = new InternalClient(
            "NEW",
            "New Client",
            "new_api_key_12345678901234567890",
            "NEW",
            endpoints,
            true,
            200
        );

        // Assert
        assertEquals("NEW", newClient.getClientId());
        assertEquals("New Client", newClient.getClientName());
        assertEquals("new_api_key_12345678901234567890", newClient.getApiKey());
        assertEquals("NEW", newClient.getTransactionPrefix());
        assertEquals(endpoints, newClient.getAllowedEndpoints());
        assertTrue(newClient.isActive());
        assertEquals(200, newClient.getRateLimitPerMinute());
    }

    @Test
    void testConstructorWithNullParameters() {
        // Act
        InternalClient newClient = new InternalClient(
            "NEW",
            "New Client",
            "new_api_key_12345678901234567890",
            "NEW",
            null,
            null,
            null
        );

        // Assert
        assertEquals("NEW", newClient.getClientId());
        assertEquals("New Client", newClient.getClientName());
        assertNotNull(newClient.getAllowedEndpoints()); // Should default to empty set
        assertTrue(newClient.isActive()); // Should default to true
        assertEquals(100, newClient.getRateLimitPerMinute()); // Should default to 100
    }

    @Test
    void testTimestampFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        client.setLastAccessedAt(now);

        // Assert
        assertEquals(now, client.getCreatedAt());
        assertEquals(now, client.getUpdatedAt());
        assertEquals(now, client.getLastAccessedAt());
    }

    @Test
    void testAuditFields() {
        // Act
        client.setCreatedBy("ADMIN");
        client.setUpdatedBy("USER");

        // Assert
        assertEquals("ADMIN", client.getCreatedBy());
        assertEquals("USER", client.getUpdatedBy());
    }
}
