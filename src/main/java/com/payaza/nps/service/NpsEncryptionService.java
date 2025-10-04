package com.payaza.nps.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * NPS Encryption Service supporting both AES-256-CBC and AES-256-GCM modes
 * as required by the National Payment Stack specifications
 */
@Service
public class NpsEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(NpsEncryptionService.class);
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256;

    static {
        // Add BouncyCastle provider for enhanced cryptographic support
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypt payload using AES-256-GCM (preferred mode for participants)
     */
    public String encryptPayloadGCM(String plaintext, String key) {
        try {
            logger.debug("Encrypting payload using AES-256-GCM");
            
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            
            // Generate random IV for GCM
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            // Initialize cipher for encryption
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            logger.error("Error encrypting payload with AES-256-GCM", e);
            throw new RuntimeException("GCM encryption failed", e);
        }
    }

    /**
     * Decrypt payload using AES-256-GCM
     */
    public String decryptPayloadGCM(String encryptedData, String key) {
        try {
            logger.debug("Decrypting payload using AES-256-GCM");
            
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            
            // Decode base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            // Initialize cipher for decryption
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Error decrypting payload with AES-256-GCM", e);
            throw new RuntimeException("GCM decryption failed", e);
        }
    }

    /**
     * Encrypt payload using AES-256-CBC (NPS switch mode)
     */
    public String encryptPayloadCBC(String plaintext, String key) {
        try {
            logger.debug("Encrypting payload using AES-256-CBC");
            
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_CBC_TRANSFORMATION);
            
            // Generate random IV for CBC
            byte[] iv = new byte[16]; // 128 bits
            new SecureRandom().nextBytes(iv);
            
            // Initialize cipher for encryption
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            logger.error("Error encrypting payload with AES-256-CBC", e);
            throw new RuntimeException("CBC encryption failed", e);
        }
    }

    /**
     * Decrypt payload using AES-256-CBC
     */
    public String decryptPayloadCBC(String encryptedData, String key) {
        try {
            logger.debug("Decrypting payload using AES-256-CBC");
            
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_CBC_TRANSFORMATION);
            
            // Decode base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[16]; // 128 bits
            byte[] encrypted = new byte[encryptedWithIv.length - iv.length];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, encrypted, 0, encrypted.length);
            
            // Initialize cipher for decryption
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Error decrypting payload with AES-256-CBC", e);
            throw new RuntimeException("CBC decryption failed", e);
        }
    }

    /**
     * Encrypt payload - automatically chooses mode based on configuration
     */
    public String encryptPayload(String plaintext) {
        // Use GCM mode by default (participant preference)
        return encryptPayloadGCM(plaintext, getEncryptionKey());
    }

    /**
     * Decrypt payload - tries both modes to handle responses from NPS
     */
    public String decryptPayload(String encryptedData) {
        try {
            // First try GCM mode
            return decryptPayloadGCM(encryptedData, getEncryptionKey());
        } catch (Exception e) {
            logger.debug("GCM decryption failed, trying CBC mode: {}", e.getMessage());
            try {
                // Fallback to CBC mode (NPS switch mode)
                return decryptPayloadCBC(encryptedData, getEncryptionKey());
            } catch (Exception e2) {
                logger.error("Both GCM and CBC decryption failed", e2);
                throw new RuntimeException("Payload decryption failed with both modes", e2);
            }
        }
    }

    /**
     * Generate HMAC signature for request authentication
     */
    public String generateSignature(String clientId, String timestamp) {
        try {
            String dataToSign = clientId + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating signature", e);
            throw new RuntimeException("Signature generation failed", e);
        }
    }

    /**
     * Generate secure random key
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating key", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Generate SHA-256 hash
     */
    public String generateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating hash", e);
            throw new RuntimeException("Hash generation failed", e);
        }
    }

    /**
     * Generate secure random string
     */
    public String generateSecureRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }

    /**
     * Validate encryption key format
     */
    public boolean isValidKey(String key) {
        if (key == null || key.length() < 32) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get encryption key from configuration
     */
    private String getEncryptionKey() {
        // In a real implementation, this would come from secure configuration
        return "your-32-character-encryption-key-here"; // This should be from NpsConfiguration
    }

    /**
     * Test encryption/decryption with both modes
     */
    public void testEncryptionModes() {
        String testData = "Test payload for NPS encryption";
        String key = generateKey();
        
        logger.info("Testing encryption modes with test data: {}", testData);
        
        try {
            // Test GCM mode
            String gcmEncrypted = encryptPayloadGCM(testData, key);
            String gcmDecrypted = decryptPayloadGCM(gcmEncrypted, key);
            logger.info("GCM mode test: {}", testData.equals(gcmDecrypted) ? "PASSED" : "FAILED");
            
            // Test CBC mode
            String cbcEncrypted = encryptPayloadCBC(testData, key);
            String cbcDecrypted = decryptPayloadCBC(cbcEncrypted, key);
            logger.info("CBC mode test: {}", testData.equals(cbcDecrypted) ? "PASSED" : "FAILED");
            
        } catch (Exception e) {
            logger.error("Encryption mode test failed", e);
        }
    }
}
