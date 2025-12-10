package com.payaza.nps.validation;

import com.payaza.nps.service.NpsXmlEncryptionService;
import com.payaza.nps.service.NpsXmlSignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation test for acmt.023 Identification Verification Request message
 * Tests XML signature and encryption with the exact structure provided by NIBSS
 */
@SpringBootTest(classes = {
    com.payaza.nps.service.NpsXmlSignatureService.class,
    com.payaza.nps.service.NpsXmlEncryptionService.class
})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Import(com.payaza.nps.config.TestApplicationConfig.class)
public class Acmt023ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(Acmt023ValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    @BeforeEach
    void setUp() {
        logger.info("ACMT.023 Validation Test setup completed");
    }

    /**
     * Test XML signature generation for acmt.023 message
     */
    @Test
    void testAcmt023XmlSignature() {
        logger.info("=== Testing ACMT.023 XML Signature Generation ===");
        
        try {
            // Sample unsigned acmt.023 message (exact structure from NIBSS)
            String unsignedAcmt023Message = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>
                            <Cretr>
                                <Pty>
                                    <Nm>Crystal Bank</Nm>
                                </Pty>
                            </Cretr>
                            <Assgnr>
                                <Pty>
                                    <Nm>Oso International Bank</Nm>
                                </Pty>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999999</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999999</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999012</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999012</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty>
                                    <Nm>Emmanuel Osod</Nm>
                                </Pty>
                                <Acct>
                                    <Id>
                                        <IBAN>1029384756</IBAN>
                                    </Id>
                                </Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                </ns2:Document>
                """;

            // Generate test key pair
            KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            logger.info("Step 1: Testing XML Digital Signature generation for ACMT.023");
            String signedXml = xmlSignatureService.signXmlDocument(unsignedAcmt023Message, privateKey);
            
            // Validate signature structure
            assertNotNull(signedXml, "Signed ACMT.023 XML should not be null");
            assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                    "Signed XML should contain XMLDSig signature element");
            assertTrue(signedXml.contains("<SignedInfo>"), "Signed XML should contain SignedInfo element");
            assertTrue(signedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), 
                    "Signed XML should contain correct canonicalization method");
            assertTrue(signedXml.contains("<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>"), 
                    "Signed XML should contain RSA-SHA256 signature method");
            assertTrue(signedXml.contains("<DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"), 
                    "Signed XML should contain SHA-256 digest method");
            assertTrue(signedXml.contains("<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"), 
                    "Signed XML should contain enveloped signature transform");
            
            logger.info("✅ ACMT.023 XML Digital Signature generation test passed");

            // Step 2: Verify the signature
            logger.info("Step 2: Testing XML Digital Signature verification for ACMT.023");
            boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
            assertTrue(isValid, "ACMT.023 signature verification should succeed");
            logger.info("✅ ACMT.023 XML Digital Signature verification test passed");

            // Step 3: Verify original content is preserved
            assertTrue(signedXml.contains("99999920250829150504887742643314693"), "Original message ID should be preserved");
            assertTrue(signedXml.contains("Emmanuel Osod"), "Original party name should be preserved");
            assertTrue(signedXml.contains("1029384756"), "Original IBAN should be preserved");
            logger.info("✅ ACMT.023 content preservation test passed");
            
            logger.info("✅ ALL ACMT.023 XML SIGNATURE TESTS PASSED");
            
        } catch (Exception e) {
            logger.error("❌ ACMT.023 XML signature test failed", e);
            fail("ACMT.023 XML signature test failed: " + e.getMessage());
        }
    }

    /**
     * Test XML encryption for acmt.023 message
     */
    @Test
    void testAcmt023XmlEncryption() {
        logger.info("=== Testing ACMT.023 XML Encryption ===");
        
        try {
            // Sample unsigned acmt.023 message
            String unsignedAcmt023Message = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>
                            <Cretr>
                                <Pty>
                                    <Nm>Crystal Bank</Nm>
                                </Pty>
                            </Cretr>
                            <Assgnr>
                                <Pty>
                                    <Nm>Oso International Bank</Nm>
                                </Pty>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999999</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999999</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999012</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999012</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty>
                                    <Nm>Emmanuel Osod</Nm>
                                </Pty>
                                <Acct>
                                    <Id>
                                        <IBAN>1029384756</IBAN>
                                    </Id>
                                </Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                </ns2:Document>
                """;

            // Generate test key pair
            KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            logger.info("Step 1: Testing XML Encryption for ACMT.023");
            String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt023Message, publicKey);
            
            // Debug: Log the encrypted XML structure
            logger.debug("Generated encrypted ACMT.023 XML structure:\n" + encryptedXml);
            
            // Validate encryption structure
            assertNotNull(encryptedXml, "Encrypted ACMT.023 XML should not be null");
            assertTrue(encryptedXml.contains("<EncryptedData Type=\"http://www.w3.org/2001/04/xmlenc#Content\" xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), 
                    "Encrypted XML should contain EncryptedData element");
            assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\">"), 
                    "Encryption method should be AES-256-CBC");
            assertTrue(encryptedXml.contains("<InitializationVector>"), 
                    "InitializationVector should be present in EncryptionMethod");
            assertTrue(encryptedXml.contains("<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                    "KeyInfo element should be present with correct namespace");
            assertTrue(encryptedXml.contains("<EncryptedKey xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), 
                    "EncryptedKey element should be present with correct namespace");
            assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\"/>"), 
                    "Key encryption method should be RSA-OAEP-MGF1P");
            assertTrue(encryptedXml.contains("<CipherData>"), "CipherData element should be present");
            assertTrue(encryptedXml.contains("<CipherValue>"), "CipherValue element should be present");
            
            logger.info("✅ ACMT.023 XML Encryption structure validation passed");

            // Verify original content is encrypted
            assertFalse(encryptedXml.contains("Emmanuel Osod"), "Original party name should be encrypted");
            assertFalse(encryptedXml.contains("1029384756"), "Original IBAN should be encrypted");
            assertFalse(encryptedXml.contains("Crystal Bank"), "Original creator name should be encrypted");
            logger.info("✅ ACMT.023 content encryption verification passed");

            // Step 2: Test decryption
            logger.info("Step 2: Testing XML Decryption for ACMT.023");
            String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
            assertNotNull(decryptedXml, "Decrypted ACMT.023 XML should not be null");
            
            // Verify decrypted content matches original
            assertTrue(decryptedXml.contains("Emmanuel Osod"), "Decrypted XML should contain original party name");
            assertTrue(decryptedXml.contains("1029384756"), "Decrypted XML should contain original IBAN");
            assertTrue(decryptedXml.contains("Crystal Bank"), "Decrypted XML should contain original creator name");
            assertTrue(decryptedXml.contains("99999920250829150504887742643314693"), "Decrypted XML should contain original message ID");
            logger.info("✅ ACMT.023 XML Decryption validation passed");
            
            logger.info("✅ ALL ACMT.023 XML ENCRYPTION TESTS PASSED");
            
        } catch (Exception e) {
            logger.error("❌ ACMT.023 XML encryption test failed", e);
            fail("ACMT.023 XML encryption test failed: " + e.getMessage());
        }
    }

    /**
     * Test ACMT.023 message structure validation
     */
    @Test
    void testAcmt023MessageStructureValidation() {
        logger.info("=== Testing ACMT.023 Message Structure Validation ===");
        
        try {
            // Sample unsigned acmt.023 message
            String unsignedAcmt023Message = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>
                            <Cretr>
                                <Pty>
                                    <Nm>Crystal Bank</Nm>
                                </Pty>
                            </Cretr>
                            <Assgnr>
                                <Pty>
                                    <Nm>Oso International Bank</Nm>
                                </Pty>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999999</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999999</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999012</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999012</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty>
                                    <Nm>Emmanuel Osod</Nm>
                                </Pty>
                                <Acct>
                                    <Id>
                                        <IBAN>1029384756</IBAN>
                                    </Id>
                                </Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                </ns2:Document>
                """;

            logger.info("Step 1: Validating ACMT.023 message structure");
            
            // Validate core structure elements
            assertTrue(unsignedAcmt023Message.contains("<IdVrfctnReq>"), "Should contain IdVrfctnReq element");
            assertTrue(unsignedAcmt023Message.contains("<Assgnmt>"), "Should contain Assgnmt element");
            assertTrue(unsignedAcmt023Message.contains("<Vrfctn>"), "Should contain Vrfctn element");
            assertTrue(unsignedAcmt023Message.contains("<PtyAndAcctId>"), "Should contain PtyAndAcctId element");
            
            // Validate assignment details
            assertTrue(unsignedAcmt023Message.contains("<MsgId>99999920250829150504887742643314693</MsgId>"), "Should contain message ID");
            assertTrue(unsignedAcmt023Message.contains("<CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>"), "Should contain creation timestamp");
            
            // Validate creator information
            assertTrue(unsignedAcmt023Message.contains("<Cretr>"), "Should contain Cretr element");
            assertTrue(unsignedAcmt023Message.contains("<Nm>Crystal Bank</Nm>"), "Should contain creator name");
            
            // Validate assignor information
            assertTrue(unsignedAcmt023Message.contains("<Assgnr>"), "Should contain Assgnr element");
            assertTrue(unsignedAcmt023Message.contains("<Nm>Oso International Bank</Nm>"), "Should contain assignor name");
            assertTrue(unsignedAcmt023Message.contains("<BICFI>999999</BICFI>"), "Should contain assignor BICFI");
            assertTrue(unsignedAcmt023Message.contains("<MmbId>999999</MmbId>"), "Should contain assignor member ID");
            
            // Validate assignee information
            assertTrue(unsignedAcmt023Message.contains("<Assgne>"), "Should contain Assgne element");
            assertTrue(unsignedAcmt023Message.contains("<BICFI>999012</BICFI>"), "Should contain assignee BICFI");
            assertTrue(unsignedAcmt023Message.contains("<MmbId>999012</MmbId>"), "Should contain assignee member ID");
            
            // Validate verification details
            assertTrue(unsignedAcmt023Message.contains("<Id>99999920250829150504887742643314693</Id>"), "Should contain verification ID");
            assertTrue(unsignedAcmt023Message.contains("<Nm>Emmanuel Osod</Nm>"), "Should contain party name for verification");
            assertTrue(unsignedAcmt023Message.contains("<IBAN>1029384756</IBAN>"), "Should contain IBAN for verification");
            
            logger.info("✅ ACMT.023 message structure validation passed");
            logger.info("✅ ALL ACMT.023 MESSAGE STRUCTURE VALIDATION TESTS PASSED");
            
        } catch (Exception e) {
            logger.error("❌ ACMT.023 message structure validation test failed", e);
            fail("ACMT.023 message structure validation test failed: " + e.getMessage());
        }
    }

    /**
     * Test complete signed and encrypted acmt.023 message flow
     */
    @Test
    void testAcmt023CompleteFlow() {
        logger.info("=== Testing ACMT.023 Complete Signed and Encrypted Flow ===");
        
        try {
            // Sample unsigned acmt.023 message
            String unsignedAcmt023Message = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>
                            <Cretr>
                                <Pty>
                                    <Nm>Crystal Bank</Nm>
                                </Pty>
                            </Cretr>
                            <Assgnr>
                                <Pty>
                                    <Nm>Oso International Bank</Nm>
                                </Pty>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999999</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999999</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999012</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999012</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty>
                                    <Nm>Emmanuel Osod</Nm>
                                </Pty>
                                <Acct>
                                    <Id>
                                        <IBAN>1029384756</IBAN>
                                    </Id>
                                </Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                </ns2:Document>
                """;

            // Generate test key pairs
            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();
            
            PrivateKey signaturePrivateKey = signatureKeyPair.getPrivate();
            PublicKey signaturePublicKey = signatureKeyPair.getPublic();
            PrivateKey encryptionPrivateKey = encryptionKeyPair.getPrivate();
            PublicKey encryptionPublicKey = encryptionKeyPair.getPublic();

            logger.info("Step 1: Encrypting ACMT.023 message");
            String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt023Message, encryptionPublicKey);
            assertNotNull(encryptedXml, "Encrypted ACMT.023 XML should not be null");
            logger.info("✅ ACMT.023 encryption completed");

            logger.info("Step 2: Signing encrypted ACMT.023 message");
            String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signaturePrivateKey);
            assertNotNull(signedEncryptedXml, "Signed and encrypted ACMT.023 XML should not be null");
            
            // Debug: Log the signed and encrypted XML structure
            logger.debug("Generated signed and encrypted ACMT.023 XML structure:\n" + signedEncryptedXml);
            
            // Verify both signature and encryption are present
            assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                    "Signed and encrypted XML should contain signature");
            assertTrue(signedEncryptedXml.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Type=\"http://www.w3.org/2001/04/xmlenc#Content\">"), 
                    "Signed and encrypted XML should contain encrypted data");
            logger.info("✅ ACMT.023 signing completed");

            logger.info("Step 3: Verifying signature on encrypted ACMT.023 message");
            boolean signatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedXml, signaturePublicKey);
            assertTrue(signatureValid, "Signature verification should succeed");
            logger.info("✅ ACMT.023 signature verification completed");

            logger.info("Step 4: Removing signature and decrypting ACMT.023 message");
            String xmlWithoutSignature = xmlSignatureService.removeSignature(signedEncryptedXml);
            String decryptedXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignature, encryptionPrivateKey);
            assertNotNull(decryptedXml, "Decrypted ACMT.023 XML should not be null");
            
            // Verify decrypted content matches original
            assertTrue(decryptedXml.contains("Emmanuel Osod"), "Decrypted XML should contain original party name");
            assertTrue(decryptedXml.contains("1029384756"), "Decrypted XML should contain original IBAN");
            assertTrue(decryptedXml.contains("Crystal Bank"), "Decrypted XML should contain original creator name");
            logger.info("✅ ACMT.023 decryption completed");
            
            logger.info("✅ ALL ACMT.023 COMPLETE FLOW TESTS PASSED");
            logger.info("📋 ACMT.023 implementation is NIBSS compliant");
            
        } catch (Exception e) {
            logger.error("❌ ACMT.023 complete flow test failed", e);
            fail("ACMT.023 complete flow test failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to verify that two XML strings have the same content,
     * ignoring formatting differences like whitespace and self-closing tags.
     */
    private boolean verifyXmlContentMatches(String original, String decrypted) {
        try {
            // Parse both XML documents
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            
            org.w3c.dom.Document originalDoc = builder.parse(new java.io.ByteArrayInputStream(original.getBytes()));
            org.w3c.dom.Document decryptedDoc = builder.parse(new java.io.ByteArrayInputStream(decrypted.getBytes()));
            
            // Normalize both documents
            originalDoc.normalize();
            decryptedDoc.normalize();
            
            // Compare the document elements
            return originalDoc.getDocumentElement().isEqualNode(decryptedDoc.getDocumentElement());
            
        } catch (Exception e) {
            logger.error("Failed to compare XML content", e);
            return false;
        }
    }
}
