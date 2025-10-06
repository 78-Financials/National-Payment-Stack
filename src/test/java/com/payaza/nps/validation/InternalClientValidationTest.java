package com.payaza.nps.validation;

import com.payaza.nps.model.InternalClient;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.InternalClientRegistry;
import com.payaza.nps.validation.ClientPermissionValidator;
import com.payaza.nps.validation.TransactionIdValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for internal client authentication and validation
 */
@SpringBootTest
@TestPropertySource(properties = {
    "internal.clients.bank.client-id=BAN",
    "internal.clients.bank.api-key=ban_api_key_12345",
    "internal.clients.bank.transaction-prefix=BAN",
    "internal.clients.bank.allowed-endpoints=pacs008,acmt023,acmt024",
    "internal.clients.bank.active=true",
    "internal.clients.bank.rate-limit-per-minute=100",
    "internal.clients.bank.encryption-key=ban_encryption_key_32_chars_long",
    
    "internal.clients.finance.client-id=FIN",
    "internal.clients.finance.api-key=fin_api_key_67890",
    "internal.clients.finance.transaction-prefix=FIN",
    "internal.clients.finance.allowed-endpoints=pacs008,pacs002,pacs028",
    "internal.clients.finance.active=true",
    "internal.clients.finance.rate-limit-per-minute=50",
    "internal.clients.finance.encryption-key=fin_encryption_key_32_chars_long",
    
    "internal.clients.payment.client-id=PAY",
    "internal.clients.payment.api-key=pay_api_key_11111",
    "internal.clients.payment.transaction-prefix=PAY",
    "internal.clients.payment.allowed-endpoints=pacs008,acmt023,acmt024,pacs002,pacs028",
    "internal.clients.payment.active=true",
    "internal.clients.payment.rate-limit-per-minute=200",
    "internal.clients.payment.encryption-key=pay_encryption_key_32_chars_long"
})
public class InternalClientValidationTest {

    @Autowired
    private InternalClientRegistry clientRegistry;
    
    @Autowired
    private ClientPermissionValidator permissionValidator;
    
    @Autowired
    private TransactionIdValidator transactionIdValidator;

    @BeforeEach
    void setUp() {
        // Clear any existing client context
        ClientContext.clear();
    }

    @Test
    void testClientRegistryInitialization() {
        System.out.println("=== Testing Client Registry Initialization ===");
        
        // Test that all 3 clients are registered
        assertEquals(3, clientRegistry.getAllClients().size());
        
        // Test Banking client
        InternalClient bankClient = clientRegistry.getClientByApiKey("ban_api_key_12345");
        assertNotNull(bankClient);
        assertEquals("BAN", bankClient.getClientId());
        assertEquals("Banking Application", bankClient.getClientName());
        assertEquals("BAN", bankClient.getTransactionPrefix());
        assertTrue(bankClient.isActive());
        assertEquals(100, bankClient.getRateLimitPerMinute());
        assertTrue(bankClient.hasPermission("pacs008"));
        assertTrue(bankClient.hasPermission("acmt023"));
        assertFalse(bankClient.hasPermission("pacs002")); // Not allowed for BAN
        
        // Test Finance client
        InternalClient financeClient = clientRegistry.getClientByApiKey("fin_api_key_67890");
        assertNotNull(financeClient);
        assertEquals("FIN", financeClient.getClientId());
        assertEquals("Finance System", financeClient.getClientName());
        assertEquals("FIN", financeClient.getTransactionPrefix());
        assertTrue(financeClient.isActive());
        assertEquals(50, financeClient.getRateLimitPerMinute());
        assertTrue(financeClient.hasPermission("pacs008"));
        assertTrue(financeClient.hasPermission("pacs002"));
        assertFalse(financeClient.hasPermission("acmt023")); // Not allowed for FIN
        
        // Test Payment client
        InternalClient paymentClient = clientRegistry.getClientByApiKey("pay_api_key_11111");
        assertNotNull(paymentClient);
        assertEquals("PAY", paymentClient.getClientId());
        assertEquals("Payment Gateway", paymentClient.getClientName());
        assertEquals("PAY", paymentClient.getTransactionPrefix());
        assertTrue(paymentClient.isActive());
        assertEquals(200, paymentClient.getRateLimitPerMinute());
        assertTrue(paymentClient.hasPermission("pacs008"));
        assertTrue(paymentClient.hasPermission("acmt023"));
        assertTrue(paymentClient.hasPermission("pacs002"));
        
        System.out.println("✅ All clients registered successfully:");
        System.out.println("- Banking Client (BAN): " + bankClient.getClientName());
        System.out.println("- Finance Client (FIN): " + financeClient.getClientName());
        System.out.println("- Payment Client (PAY): " + paymentClient.getClientName());
    }

    @Test
    void testTransactionIdValidation() {
        System.out.println("=== Testing Transaction ID Validation ===");
        
        // Test Banking client transaction IDs
        InternalClient bankClient = clientRegistry.getClientByApiKey("ban_api_key_12345");
        ClientContext.setCurrentClient(bankClient);
        
        // Valid BAN transaction IDs (with hyphen format)
        assertTrue(transactionIdValidator.isValidTransactionId("BAN-123456789"));
        assertTrue(transactionIdValidator.isValidTransactionId("BAN-20250829174941709740087747292"));
        assertTrue(transactionIdValidator.isValidTransactionId("BAN-TXN-001"));
        assertTrue(transactionIdValidator.isValidTransactionId("BAN-1")); // Minimum valid format
        
        // Invalid BAN transaction IDs
        assertFalse(transactionIdValidator.isValidTransactionId("BAN123456789")); // Missing hyphen
        assertFalse(transactionIdValidator.isValidTransactionId("FIN-123456789")); // Wrong prefix
        assertFalse(transactionIdValidator.isValidTransactionId("BA-123456789")); // Too short prefix
        assertFalse(transactionIdValidator.isValidTransactionId("BANK-123456789")); // Wrong prefix (BANK vs BAN)
        assertFalse(transactionIdValidator.isValidTransactionId("BAN")); // Missing hyphen and suffix
        assertFalse(transactionIdValidator.isValidTransactionId("")); // Empty
        assertFalse(transactionIdValidator.isValidTransactionId(null)); // Null
        
        // Test with specific client prefix validation
        assertFalse(transactionIdValidator.isValidTransactionId("FIN-123456789", "BAN")); // Wrong prefix for BAN
        assertFalse(transactionIdValidator.isValidTransactionId("BA-123456789", "BAN")); // Too short prefix for BAN
        
        // Test Finance client transaction IDs
        InternalClient financeClient = clientRegistry.getClientByApiKey("fin_api_key_67890");
        ClientContext.setCurrentClient(financeClient);
        
        // Valid FIN transaction IDs (with hyphen format)
        assertTrue(transactionIdValidator.isValidTransactionId("FIN-123456789"));
        assertTrue(transactionIdValidator.isValidTransactionId("FIN-20250829174941709740087747292"));
        assertTrue(transactionIdValidator.isValidTransactionId("FIN-TXN-001"));
        assertTrue(transactionIdValidator.isValidTransactionId("FIN-1")); // Minimum valid format
        
        // Invalid FIN transaction IDs
        assertFalse(transactionIdValidator.isValidTransactionId("FIN123456789")); // Missing hyphen
        assertFalse(transactionIdValidator.isValidTransactionId("BAN-123456789")); // Wrong prefix
        assertFalse(transactionIdValidator.isValidTransactionId("PAY-123456789")); // Wrong prefix
        
        // Test Payment client transaction IDs
        InternalClient paymentClient = clientRegistry.getClientByApiKey("pay_api_key_11111");
        ClientContext.setCurrentClient(paymentClient);
        
        // Valid PAY transaction IDs (with hyphen format)
        assertTrue(transactionIdValidator.isValidTransactionId("PAY-123456789"));
        assertTrue(transactionIdValidator.isValidTransactionId("PAY-20250829174941709740087747292"));
        assertTrue(transactionIdValidator.isValidTransactionId("PAY-TXN-001"));
        assertTrue(transactionIdValidator.isValidTransactionId("PAY-1")); // Minimum valid format
        
        System.out.println("✅ Transaction ID validation working correctly:");
        System.out.println("- BAN prefix: BAN-123456789 ✓");
        System.out.println("- FIN prefix: FIN-123456789 ✓");
        System.out.println("- PAY prefix: PAY-123456789 ✓");
        System.out.println("- Wrong prefixes rejected ✓");
    }

    @Test
    void testClientPermissionValidation() {
        System.out.println("=== Testing Client Permission Validation ===");
        
        // Test Banking client permissions
        InternalClient bankClient = clientRegistry.getClientByApiKey("ban_api_key_12345");
        ClientContext.setCurrentClient(bankClient);
        
        assertTrue(permissionValidator.hasPermission("pacs008"));
        assertTrue(permissionValidator.hasPermission("acmt023"));
        assertTrue(permissionValidator.hasPermission("acmt024"));
        assertFalse(permissionValidator.hasPermission("pacs002")); // Not allowed
        assertFalse(permissionValidator.hasPermission("pacs028")); // Not allowed
        
        // Test Finance client permissions
        InternalClient financeClient = clientRegistry.getClientByApiKey("fin_api_key_67890");
        ClientContext.setCurrentClient(financeClient);
        
        assertTrue(permissionValidator.hasPermission("pacs008"));
        assertTrue(permissionValidator.hasPermission("pacs002"));
        assertTrue(permissionValidator.hasPermission("pacs028"));
        assertFalse(permissionValidator.hasPermission("acmt023")); // Not allowed
        assertFalse(permissionValidator.hasPermission("acmt024")); // Not allowed
        
        // Test Payment client permissions (full access)
        InternalClient paymentClient = clientRegistry.getClientByApiKey("pay_api_key_11111");
        ClientContext.setCurrentClient(paymentClient);
        
        assertTrue(permissionValidator.hasPermission("pacs008"));
        assertTrue(permissionValidator.hasPermission("acmt023"));
        assertTrue(permissionValidator.hasPermission("acmt024"));
        assertTrue(permissionValidator.hasPermission("pacs002"));
        assertTrue(permissionValidator.hasPermission("pacs028"));
        
        System.out.println("✅ Client permission validation working correctly:");
        System.out.println("- Banking Client (BAN): pacs008, acmt023, acmt024 ✓");
        System.out.println("- Finance Client (FIN): pacs008, pacs002, pacs028 ✓");
        System.out.println("- Payment Client (PAY): All endpoints ✓");
    }

    @Test
    void testTransactionIdGeneration() {
        System.out.println("=== Testing Transaction ID Generation ===");
        
        // Test Banking client transaction ID generation
        InternalClient bankClient = clientRegistry.getClientByApiKey("ban_api_key_12345");
        ClientContext.setCurrentClient(bankClient);
        
        String bankTransactionId = transactionIdValidator.generateTransactionId();
        assertNotNull(bankTransactionId);
        assertTrue(bankTransactionId.startsWith("BAN-"));
        assertTrue(transactionIdValidator.isValidTransactionId(bankTransactionId));
        
        // Test Finance client transaction ID generation
        InternalClient financeClient = clientRegistry.getClientByApiKey("fin_api_key_67890");
        ClientContext.setCurrentClient(financeClient);
        
        String financeTransactionId = transactionIdValidator.generateTransactionId();
        assertNotNull(financeTransactionId);
        assertTrue(financeTransactionId.startsWith("FIN-"));
        assertTrue(transactionIdValidator.isValidTransactionId(financeTransactionId));
        
        // Test Payment client transaction ID generation
        InternalClient paymentClient = clientRegistry.getClientByApiKey("pay_api_key_11111");
        ClientContext.setCurrentClient(paymentClient);
        
        String paymentTransactionId = transactionIdValidator.generateTransactionId();
        assertNotNull(paymentTransactionId);
        assertTrue(paymentTransactionId.startsWith("PAY-"));
        assertTrue(transactionIdValidator.isValidTransactionId(paymentTransactionId));
        
        System.out.println("✅ Transaction ID generation working correctly:");
        System.out.println("- Generated BAN transaction ID: " + bankTransactionId);
        System.out.println("- Generated FIN transaction ID: " + financeTransactionId);
        System.out.println("- Generated PAY transaction ID: " + paymentTransactionId);
        
        // Verify generated IDs follow the new format
        assertTrue(bankTransactionId.startsWith("BAN-"));
        assertTrue(financeTransactionId.startsWith("FIN-"));
        assertTrue(paymentTransactionId.startsWith("PAY-"));
    }

    @Test
    void testInvalidApiKey() {
        System.out.println("=== Testing Invalid API Key Handling ===");
        
        // Test invalid API key
        InternalClient invalidClient = clientRegistry.getClientByApiKey("invalid_api_key");
        assertNull(invalidClient);
        
        // Test null API key
        InternalClient nullClient = clientRegistry.getClientByApiKey(null);
        assertNull(nullClient);
        
        // Test empty API key
        InternalClient emptyClient = clientRegistry.getClientByApiKey("");
        assertNull(emptyClient);
        
        System.out.println("✅ Invalid API key handling working correctly:");
        System.out.println("- Invalid API key returns null ✓");
        System.out.println("- Null API key returns null ✓");
        System.out.println("- Empty API key returns null ✓");
    }
}
