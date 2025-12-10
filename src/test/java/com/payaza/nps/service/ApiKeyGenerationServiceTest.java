package com.payaza.nps.service;

import com.payaza.nps.service.ApiKeyGenerationService.KeyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiKeyGenerationService
 */
public class ApiKeyGenerationServiceTest {

    private ApiKeyGenerationService apiKeyGenerationService;

    @BeforeEach
    void setUp() {
        apiKeyGenerationService = new ApiKeyGenerationService();
    }

    @Test
    void testGenerateApiKey_WithPrefix() {
        // Act
        String apiKey = apiKeyGenerationService.generateApiKey("nps");

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("nps_"));
        assertTrue(apiKey.length() >= 20); // Minimum length
        assertTrue(apiKey.length() <= 100); // Maximum length
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat(apiKey));
    }

    @Test
    void testGenerateApiKey_WithCustomLengthAndType() {
        // Act
        String apiKey = apiKeyGenerationService.generateApiKey("test", 16, KeyType.ALPHANUMERIC);

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("test_"));
        assertEquals(21, apiKey.length()); // "test_" + 16 chars = 21
        assertTrue(apiKey.matches("test_[A-Za-z0-9]{16}"));
    }

    @Test
    void testGenerateApiKey_WithSpecialCharacters() {
        // Act
        String apiKey = apiKeyGenerationService.generateApiKey("test", 16, KeyType.ALPHANUMERIC_WITH_SPECIAL);

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("test_"));
        assertEquals(21, apiKey.length());
        assertTrue(apiKey.matches("test_[A-Za-z0-9_-]{16}"));
    }

    @Test
    void testGenerateApiKey_Hexadecimal() {
        // Act
        String apiKey = apiKeyGenerationService.generateApiKey("hex", 12, KeyType.HEXADECIMAL);

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("hex_"));
        assertEquals(16, apiKey.length()); // "hex_" + 12 chars = 16
        assertTrue(apiKey.matches("hex_[0-9A-F]{12}"));
    }

    @Test
    void testGenerateUuidApiKey() {
        // Act
        String apiKey = apiKeyGenerationService.generateUuidApiKey();

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("nps_"));
        assertEquals(36, apiKey.length()); // "nps_" + 32 chars (UUID without hyphens)
        assertTrue(apiKey.matches("nps_[0-9a-f]{32}"));
    }

    @Test
    void testGenerateBase64ApiKey() {
        // Act
        String apiKey = apiKeyGenerationService.generateBase64ApiKey(24);

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("nps_"));
        assertTrue(apiKey.length() > 30); // Base64 encoding of 24 bytes
        assertTrue(apiKey.matches("nps_[A-Za-z0-9_-]+"));
    }

    @Test
    void testGenerateClientApiKey() {
        // Act
        String apiKey = apiKeyGenerationService.generateClientApiKey("TEST");

        // Assert
        assertNotNull(apiKey);
        assertTrue(apiKey.startsWith("test_api_key_"));
        assertTrue(apiKey.length() >= 29);
        assertTrue(apiKey.matches("test_api_key_[A-Za-z0-9]+"), 
                   "API key '" + apiKey + "' should match pattern 'test_api_key_[A-Za-z0-9]+'");
        
        // Note: The generated client API key format may not pass the strict validation
        // as it's designed for a specific use case, not general API key validation
        // We'll test the format separately
    }

    @RepeatedTest(10)
    void testGenerateApiKey_Uniqueness() {
        // Act - Generate multiple API keys
        String key1 = apiKeyGenerationService.generateApiKey("test");
        String key2 = apiKeyGenerationService.generateApiKey("test");
        String key3 = apiKeyGenerationService.generateApiKey("test");

        // Assert - All should be unique
        assertNotEquals(key1, key2);
        assertNotEquals(key2, key3);
        assertNotEquals(key1, key3);
    }

    @Test
    void testIsValidApiKeyFormat_ValidKeys() {
        // Valid API keys (minimum 20 characters)
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat("nps_abc123def456ghi789"));
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat("test_api_key_123456789"));
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat("client_12345678901234567890"));
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat("A_very_long_api_key_with_many_characters_123456789"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", // Empty
        "nokey", // No underscore
        "short", // Too short
        "nps_", // Empty suffix
        "nps_abc", // Too short overall
        "nps_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // Too long (104 chars)
        "nps_key@invalid", // Invalid characters
        "nps key with spaces", // Spaces
        "nps_key\nwith\nnewlines" // Newlines
    })
    void testIsValidApiKeyFormat_InvalidKeys(String invalidKey) {
        // Assert - All these keys should be invalid
        assertFalse(apiKeyGenerationService.isValidApiKeyFormat(invalidKey), 
                   "Key '" + invalidKey + "' should be invalid");
    }

    @Test
    void testIsValidApiKeyFormat_NullKey() {
        // Assert
        assertFalse(apiKeyGenerationService.isValidApiKeyFormat(null));
    }

    @Test
    void testExtractPrefix_ValidKeys() {
        // Act & Assert
        assertEquals("nps", apiKeyGenerationService.extractPrefix("nps_abc123def456"));
        assertEquals("test", apiKeyGenerationService.extractPrefix("test_api_key_123"));
        assertEquals("client", apiKeyGenerationService.extractPrefix("client_123456789"));
        assertEquals("very", apiKeyGenerationService.extractPrefix("very_long_prefix_suffix")); // Only extracts up to first underscore
    }

    @Test
    void testExtractPrefix_InvalidKeys() {
        // Act & Assert
        assertNull(apiKeyGenerationService.extractPrefix(null));
        assertNull(apiKeyGenerationService.extractPrefix("nokey"));
        assertNull(apiKeyGenerationService.extractPrefix(""));
    }

    @Test
    void testGenerateApiKey_MaxLengthConstraint() {
        // Act - Generate key that would exceed 35 characters after prefix
        String apiKey = apiKeyGenerationService.generateApiKey("verylongprefix", 50, KeyType.ALPHANUMERIC);

        // Assert - Should be truncated to fit within limits
        assertNotNull(apiKey);
        assertTrue(apiKey.length() <= 100); // Maximum length
        assertTrue(apiKey.startsWith("verylongprefix_"));
    }

    @Test
    void testGenerateApiKey_MinimumLength() {
        // Act
        String apiKey = apiKeyGenerationService.generateApiKey("nps", 1, KeyType.ALPHANUMERIC);

        // Assert
        assertNotNull(apiKey);
        assertEquals(5, apiKey.length()); // "nps_" + 1 char = 5
        // Note: This key will be invalid according to isValidApiKeyFormat due to minimum length requirement
        assertFalse(apiKeyGenerationService.isValidApiKeyFormat(apiKey));
    }

    @Test
    void testGenerateClientApiKey_ConsistentFormat() {
        // Act - Generate multiple client API keys
        String key1 = apiKeyGenerationService.generateClientApiKey("BANK");
        String key2 = apiKeyGenerationService.generateClientApiKey("FINANCE");
        String key3 = apiKeyGenerationService.generateClientApiKey("PAYMENT");

        // Assert - All should follow the same format
        assertTrue(key1.startsWith("bank_api_key_"));
        assertTrue(key2.startsWith("finance_api_key_"));
        assertTrue(key3.startsWith("payment_api_key_"));

        // All should be valid
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat(key1));
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat(key2));
        assertTrue(apiKeyGenerationService.isValidApiKeyFormat(key3));
    }

    @Test
    void testGenerateApiKey_AllKeyTypes() {
        // Act & Assert - Test all key types
        String alphanumeric = apiKeyGenerationService.generateApiKey("test", 10, KeyType.ALPHANUMERIC);
        assertTrue(alphanumeric.matches("test_[A-Za-z0-9]{10}"));

        String withSpecial = apiKeyGenerationService.generateApiKey("test", 10, KeyType.ALPHANUMERIC_WITH_SPECIAL);
        assertTrue(withSpecial.matches("test_[A-Za-z0-9_-]{10}"));

        String hex = apiKeyGenerationService.generateApiKey("test", 10, KeyType.HEXADECIMAL);
        assertTrue(hex.matches("test_[0-9A-F]{10}"));
    }

    @Test
    void testGenerateApiKey_EdgeCases() {
        // Act & Assert - Test edge cases
        String emptyPrefix = apiKeyGenerationService.generateApiKey("", 5, KeyType.ALPHANUMERIC);
        assertTrue(emptyPrefix.matches("_[A-Za-z0-9]{5}"));

        String longPrefix = apiKeyGenerationService.generateApiKey("a".repeat(20), 5, KeyType.ALPHANUMERIC);
        assertTrue(longPrefix.length() <= 100);
        assertTrue(longPrefix.startsWith("a".repeat(20) + "_"));
    }
}
