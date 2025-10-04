package com.payaza.nps.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encryption and security operations
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 256;

    static {
        // Add BouncyCastle provider
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Encrypt data using AES encryption
     */
    public String encrypt(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            logger.error("Error encrypting data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES decryption
     */
    public String decrypt(String encryptedData, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error decrypting data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generate a secure random key
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
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
     * Generate transaction ID
     */
    public String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + generateSecureRandomString(6);
    }

    /**
     * Generate payment ID
     */
    public String generatePaymentId() {
        return "PAY" + System.currentTimeMillis() + generateSecureRandomString(6);
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
}
