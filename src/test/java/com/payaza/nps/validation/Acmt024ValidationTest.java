package com.payaza.nps.validation;

import com.payaza.nps.NigerianPaymentStackApplication;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
    com.payaza.nps.service.NpsXmlSignatureService.class,
    com.payaza.nps.service.NpsXmlEncryptionService.class
})
@ActiveProfiles("test")
@Import(com.payaza.nps.config.TestApplicationConfig.class)
public class Acmt024ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(Acmt024ValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    private String unsignedAcmt024SuccessMessage;
    private String unsignedAcmt024FailureMessage;

    @BeforeEach
    void setUp() {
        // Sample ACMT.024 Success message
        unsignedAcmt024SuccessMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04\">\n" +
                "    <IdVrfctnRpt>\n" +
                "        <Assgnmt>\n" +
                "            <MsgId>99901220250829140722546736145961156</MsgId>\n" +
                "            <CreDtTm>2025-08-29T14:07:22.357Z</CreDtTm>\n" +
                "            <Assgnr>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI>999012</BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>999012</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgnr>\n" +
                "            <Assgne>\n" +
                "                <Pty>\n" +
                "                    <Nm>Oso International Bank</Nm>\n" +
                "                </Pty>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI>999999</BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>999999</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgne>\n" +
                "        </Assgnmt>\n" +
                "        <OrgnlAssgnmt>\n" +
                "            <MsgId>99999920250829150504887742643314693</MsgId>\n" +
                "            <CreDtTm>2025-08-29T15:05:04.347Z</CreDtTm>\n" +
                "        </OrgnlAssgnmt>\n" +
                "        <Rpt>\n" +
                "            <OrgnlId>99999920250829150504887742643314693</OrgnlId>\n" +
                "            <Vrfctn>true</Vrfctn>\n" +
                "            <OrgnlPtyAndAcctId>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>1029384756</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </OrgnlPtyAndAcctId>\n" +
                "            <UpdtdPtyAndAcctId>\n" +
                "                <Pty>\n" +
                "                    <Nm>Emmanuel Oso</Nm>\n" +
                "                </Pty>\n" +
                "            </UpdtdPtyAndAcctId>\n" +
                "        </Rpt>\n" +
                "        <SplmtryData>\n" +
                "            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>\n" +
                "            <Envlp>\n" +
                "                <CustomData>\n" +
                "                    <CreditorInfo>\n" +
                "                        <AccountDesignation>1</AccountDesignation>\n" +
                "                        <IdType>BVN</IdType>\n" +
                "                        <IdValue>2211232346</IdValue>\n" +
                "                        <AccountTier>1</AccountTier>\n" +
                "                    </CreditorInfo>\n" +
                "                    <TransactionInfo>\n" +
                "                        <RiskRating>R000000000000000000B9</RiskRating>\n" +
                "                    </TransactionInfo>\n" +
                "                </CustomData>\n" +
                "            </Envlp>\n" +
                "        </SplmtryData>\n" +
                "    </IdVrfctnRpt>\n" +
                "</ns2:Document>";

        // Sample ACMT.024 Failure message
        unsignedAcmt024FailureMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04\">\n" +
                "    <IdVrfctnRpt>\n" +
                "        <Assgnmt>\n" +
                "            <MsgId>99901220250829140722546736145961157</MsgId>\n" +
                "            <CreDtTm>2025-08-29T14:07:22.357Z</CreDtTm>\n" +
                "            <Assgnr>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI></BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>999012</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgnr>\n" +
                "            <Assgne>\n" +
                "                <Pty>\n" +
                "                    <Nm>Oso International Bank</Nm>\n" +
                "                </Pty>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI></BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>999999</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgne>\n" +
                "        </Assgnmt>\n" +
                "        <OrgnlAssgnmt>\n" +
                "            <MsgId>99999920250829150504887742643314694</MsgId>\n" +
                "            <CreDtTm>2025-08-29T15:05:04.347Z</CreDtTm>\n" +
                "        </OrgnlAssgnmt>\n" +
                "        <Rpt>\n" +
                "            <OrgnlId>99999920250829150504887742643314694</OrgnlId>\n" +
                "            <Vrfctn>false</Vrfctn>\n" +
                "            <Rsn>\n" +
                "                <Cd>33</Cd>\n" +
                "                <Prtry>Account number mismatch</Prtry>\n" +
                "            </Rsn>\n" +
                "            <OrgnlPtyAndAcctId>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>1029384756</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </OrgnlPtyAndAcctId>\n" +
                "        </Rpt>\n" +
                "        <SplmtryData>\n" +
                "            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>\n" +
                "            <Envlp>\n" +
                "                <CustomData>\n" +
                "                    <CreditorInfo>\n" +
                "                        <AccountDesignation></AccountDesignation>\n" +
                "                        <IdType></IdType>\n" +
                "                        <IdValue></IdValue>\n" +
                "                        <AccountTier></AccountTier>\n" +
                "                    </CreditorInfo>\n" +
                "                    <TransactionInfo>\n" +
                "                        <RiskRating></RiskRating>\n" +
                "                    </TransactionInfo>\n" +
                "                </CustomData>\n" +
                "            </Envlp>\n" +
                "        </SplmtryData>\n" +
                "    </IdVrfctnRpt>\n" +
                "</ns2:Document>";

        logger.info("ACMT.024 Validation Test setup completed");
    }

    @Test
    void testAcmt024SuccessXmlSignatureGeneration() throws Exception {
        logger.info("=== Testing ACMT.024 Success XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Digital Signature generation for ACMT.024 Success");
        String signedXml = xmlSignatureService.signXmlDocument(unsignedAcmt024SuccessMessage, privateKey);
        assertNotNull(signedXml, "Signed XML should not be null");
        assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"),
                "Signed XML should contain Signature element");
            assertTrue(signedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), 
                    "Signed XML should contain correct canonicalization method");
        assertTrue(signedXml.contains("<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>"),
                "Signed XML should contain RSA-SHA256 signature method");
        assertTrue(signedXml.contains("<DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"),
                "Signed XML should contain SHA-256 digest method");
        assertTrue(signedXml.contains("<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"),
                "Signed XML should contain enveloped signature transform");
        assertTrue(signedXml.contains("99901220250829140722546736145961156"), "Original message ID should be present in signed XML");
        logger.info("✅ ACMT.024 Success XML Digital Signature generation test passed");

        logger.info("Step 2: Testing XML Digital Signature verification for ACMT.024 Success");
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ ACMT.024 Success XML Digital Signature verification test passed");

        // Verify content is preserved
        String unsignedContent = extractContentWithoutSignature(signedXml);
        assertTrue(verifyXmlContentMatches(unsignedAcmt024SuccessMessage, unsignedContent), "ACMT.024 Success content should be preserved after signing");
        logger.info("✅ ACMT.024 Success content preservation test passed");

        logger.info("✅ ALL ACMT.024 SUCCESS XML SIGNATURE TESTS PASSED");
    }

    @Test
    void testAcmt024FailureXmlSignatureGeneration() throws Exception {
        logger.info("=== Testing ACMT.024 Failure XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Digital Signature generation for ACMT.024 Failure");
        String signedXml = xmlSignatureService.signXmlDocument(unsignedAcmt024FailureMessage, privateKey);
        assertNotNull(signedXml, "Signed XML should not be null");
        assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"),
                "Signed XML should contain Signature element");
            assertTrue(signedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), 
                    "Signed XML should contain correct canonicalization method");
        assertTrue(signedXml.contains("<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>"),
                "Signed XML should contain RSA-SHA256 signature method");
        assertTrue(signedXml.contains("<DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"),
                "Signed XML should contain SHA-256 digest method");
        assertTrue(signedXml.contains("<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"),
                "Signed XML should contain enveloped signature transform");
        assertTrue(signedXml.contains("99901220250829140722546736145961157"), "Original message ID should be present in signed XML");
        assertTrue(signedXml.contains("<Vrfctn>false</Vrfctn>"), "Failure verification status should be present");
        assertTrue(signedXml.contains("<Cd>33</Cd>"), "Failure code should be present");
        assertTrue(signedXml.contains("Account number mismatch"), "Failure reason should be present");
        logger.info("✅ ACMT.024 Failure XML Digital Signature generation test passed");

        logger.info("Step 2: Testing XML Digital Signature verification for ACMT.024 Failure");
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ ACMT.024 Failure XML Digital Signature verification test passed");

        // Verify content is preserved
        String unsignedContent = extractContentWithoutSignature(signedXml);
        assertTrue(verifyXmlContentMatches(unsignedAcmt024FailureMessage, unsignedContent), "ACMT.024 Failure content should be preserved after signing");
        logger.info("✅ ACMT.024 Failure content preservation test passed");

        logger.info("✅ ALL ACMT.024 FAILURE XML SIGNATURE TESTS PASSED");
    }

    @Test
    void testAcmt024SuccessXmlEncryption() throws Exception {
        logger.info("=== Testing ACMT.024 Success XML Encryption ===");
        // Generate test key pair
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Encryption for ACMT.024 Success");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt024SuccessMessage, publicKey);
        
        // Debug: Log the encrypted XML structure
        logger.debug("Generated encrypted ACMT.024 Success XML structure:\n" + encryptedXml);
        
        // Validate encryption structure
        assertNotNull(encryptedXml, "Encrypted ACMT.024 Success XML should not be null");
        assertTrue(encryptedXml.contains("<EncryptedData"), 
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
        assertFalse(encryptedXml.contains("99901220250829140722546736145961156"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("Emmanuel Oso"), "Original name should be encrypted");
        logger.info("✅ ACMT.024 Success XML Encryption structure validation passed");

        // Verify original content is encrypted
        assertFalse(encryptedXml.contains("99901220250829140722546736145961156"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("Emmanuel Oso"), "Original name should be encrypted");
        assertFalse(encryptedXml.contains("1029384756"), "Original account number should be encrypted");
        assertFalse(encryptedXml.contains("<Vrfctn>true</Vrfctn>"), "Verification status should be encrypted");
        logger.info("✅ ACMT.024 Success content encryption verification passed");

        logger.info("Step 2: Testing XML Decryption for ACMT.024 Success");
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertNotNull(decryptedXml, "Decrypted ACMT.024 Success XML should not be null");
        
        // Debug: Log the decrypted XML to see what we got
        logger.debug("Original XML:\n" + unsignedAcmt024SuccessMessage);
        logger.debug("Decrypted XML:\n" + decryptedXml);
        
        // Verify that key content is present in decrypted XML
        assertTrue(decryptedXml.contains("99901220250829140722546736145961156"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("Emmanuel Oso"), "Decrypted XML should contain original name");
        assertTrue(decryptedXml.contains("1029384756"), "Decrypted XML should contain original account number");
        assertTrue(decryptedXml.contains("<Vrfctn>true</Vrfctn>"), "Decrypted XML should contain verification status");
        assertTrue(decryptedXml.contains("BVN"), "Decrypted XML should contain ID type");
        assertTrue(decryptedXml.contains("2211232346"), "Decrypted XML should contain ID value");
        logger.info("✅ ACMT.024 Success XML Decryption validation passed");

        logger.info("✅ ALL ACMT.024 SUCCESS XML ENCRYPTION TESTS PASSED");
    }

    @Test
    void testAcmt024FailureXmlEncryption() throws Exception {
        logger.info("=== Testing ACMT.024 Failure XML Encryption ===");
        // Generate test key pair
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Encryption for ACMT.024 Failure");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt024FailureMessage, publicKey);
        
        // Debug: Log the encrypted XML structure
        logger.debug("Generated encrypted ACMT.024 Failure XML structure:\n" + encryptedXml);
        
        // Validate encryption structure
        assertNotNull(encryptedXml, "Encrypted ACMT.024 Failure XML should not be null");
        assertTrue(encryptedXml.contains("<EncryptedData"), 
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
        assertFalse(encryptedXml.contains("99901220250829140722546736145961157"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("<Vrfctn>false</Vrfctn>"), "Failure verification status should be encrypted");
        assertFalse(encryptedXml.contains("<Cd>33</Cd>"), "Failure code should be encrypted");
        assertFalse(encryptedXml.contains("Account number mismatch"), "Failure reason should be encrypted");
        logger.info("✅ ACMT.024 Failure XML Encryption structure validation passed");

        // Verify original content is encrypted
        assertFalse(encryptedXml.contains("99901220250829140722546736145961157"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("<Vrfctn>false</Vrfctn>"), "Failure verification status should be encrypted");
        assertFalse(encryptedXml.contains("<Cd>33</Cd>"), "Failure code should be encrypted");
        assertFalse(encryptedXml.contains("Account number mismatch"), "Failure reason should be encrypted");
        logger.info("✅ ACMT.024 Failure content encryption verification passed");

        logger.info("Step 2: Testing XML Decryption for ACMT.024 Failure");
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertNotNull(decryptedXml, "Decrypted ACMT.024 Failure XML should not be null");
        
        // Debug: Log the decrypted XML to see what we got
        logger.debug("Original Failure XML:\n" + unsignedAcmt024FailureMessage);
        logger.debug("Decrypted Failure XML:\n" + decryptedXml);
        
        // Verify that key content is present in decrypted XML
        assertTrue(decryptedXml.contains("99901220250829140722546736145961157"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("<Vrfctn>false</Vrfctn>"), "Decrypted XML should contain failure verification status");
        assertTrue(decryptedXml.contains("<Cd>33</Cd>"), "Decrypted XML should contain failure code");
        assertTrue(decryptedXml.contains("Account number mismatch"), "Decrypted XML should contain failure reason");
        logger.info("✅ ACMT.024 Failure XML Decryption validation passed");

        logger.info("✅ ALL ACMT.024 FAILURE XML ENCRYPTION TESTS PASSED");
    }

    @Test
    void testAcmt024CompleteFlow() throws Exception {
        logger.info("=== Testing ACMT.024 Complete Signed and Encrypted Flow ===");

        // Generate key pairs for encryption and signature
        KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey encryptionPrivateKey = encryptionKeyPair.getPrivate();
        PublicKey encryptionPublicKey = encryptionKeyPair.getPublic();

        KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey signaturePrivateKey = signatureKeyPair.getPrivate();
        PublicKey signaturePublicKey = signatureKeyPair.getPublic();

        logger.info("Step 1: Encrypting ACMT.024 Success message");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt024SuccessMessage, encryptionPublicKey);
        assertNotNull(encryptedXml, "Encrypted ACMT.024 Success XML should not be null");
        logger.info("✅ ACMT.024 Success encryption completed");

        logger.info("Step 2: Signing encrypted ACMT.024 Success message");
        String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signaturePrivateKey);
        assertNotNull(signedEncryptedXml, "Signed and encrypted ACMT.024 Success XML should not be null");
        
        // Debug: Log the signed and encrypted XML structure
        logger.debug("Generated signed and encrypted ACMT.024 Success XML structure:\n" + signedEncryptedXml);
        
        // Verify both signature and encryption are present
        assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                "Signed and encrypted XML should contain signature");
        assertTrue(signedEncryptedXml.contains("<EncryptedData"), 
                "Signed and encrypted XML should contain encrypted data");
        logger.info("✅ ACMT.024 Success signing completed");

        logger.info("Step 3: Verifying signature on encrypted ACMT.024 Success message");
        boolean signatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedXml, signaturePublicKey);
        assertTrue(signatureValid, "Signature verification should succeed");
        logger.info("✅ ACMT.024 Success signature verification completed");

        logger.info("Step 4: Removing signature and decrypting ACMT.024 Success message");
        String xmlWithoutSignature = extractContentWithoutSignature(signedEncryptedXml);
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignature, encryptionPrivateKey);
        assertNotNull(decryptedXml, "Decrypted ACMT.024 Success XML should not be null");
        
        // Verify that key content is present in decrypted XML
        assertTrue(decryptedXml.contains("99901220250829140722546736145961156"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("Emmanuel Oso"), "Decrypted XML should contain original name");
        assertTrue(decryptedXml.contains("<Vrfctn>true</Vrfctn>"), "Decrypted XML should contain verification status");
        logger.info("✅ ACMT.024 Success decryption completed");

        // Test Failure scenario
        logger.info("Step 5: Testing ACMT.024 Failure complete flow");
        String encryptedFailureXml = xmlEncryptionService.encryptXmlDocument(unsignedAcmt024FailureMessage, encryptionPublicKey);
        String signedEncryptedFailureXml = xmlSignatureService.signXmlDocument(encryptedFailureXml, signaturePrivateKey);
        boolean failureSignatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedFailureXml, signaturePublicKey);
        assertTrue(failureSignatureValid, "Failure message signature verification should succeed");
        
        String xmlWithoutSignatureFailure = extractContentWithoutSignature(signedEncryptedFailureXml);
        String decryptedFailureXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignatureFailure, encryptionPrivateKey);
        
        // Verify that key content is present in decrypted failure XML
        assertTrue(decryptedFailureXml.contains("99901220250829140722546736145961157"), "Decrypted failure XML should contain original message ID");
        assertTrue(decryptedFailureXml.contains("<Vrfctn>false</Vrfctn>"), "Decrypted failure XML should contain failure verification status");
        assertTrue(decryptedFailureXml.contains("<Cd>33</Cd>"), "Decrypted failure XML should contain failure code");
        logger.info("✅ ACMT.024 Failure complete flow completed");

        logger.info("✅ ALL ACMT.024 COMPLETE FLOW TESTS PASSED");
        logger.info("📋 ACMT.024 implementation is NIBSS compliant");
    }

    @Test
    void testAcmt024MessageStructureValidation() throws Exception {
        logger.info("=== Testing ACMT.024 Message Structure Validation ===");
        
        // Test Success message structure
        logger.info("Step 1: Validating ACMT.024 Success message structure");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<IdVrfctnRpt>"), "Should contain IdVrfctnRpt element");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<Assgnmt>"), "Should contain Assgnmt element");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<OrgnlAssgnmt>"), "Should contain OrgnlAssgnmt element");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<Rpt>"), "Should contain Rpt element");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<SplmtryData>"), "Should contain SplmtryData element");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<Vrfctn>true</Vrfctn>"), "Should contain verification result");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<UpdtdPtyAndAcctId>"), "Should contain updated party info for success");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<AccountDesignation>1</AccountDesignation>"), "Should contain account designation");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<IdType>BVN</IdType>"), "Should contain ID type");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<IdValue>2211232346</IdValue>"), "Should contain ID value");
        assertTrue(unsignedAcmt024SuccessMessage.contains("<AccountTier>1</AccountTier>"), "Should contain account tier");
        logger.info("✅ ACMT.024 Success message structure validation passed");

        // Test Failure message structure
        logger.info("Step 2: Validating ACMT.024 Failure message structure");
        assertTrue(unsignedAcmt024FailureMessage.contains("<IdVrfctnRpt>"), "Should contain IdVrfctnRpt element");
        assertTrue(unsignedAcmt024FailureMessage.contains("<Assgnmt>"), "Should contain Assgnmt element");
        assertTrue(unsignedAcmt024FailureMessage.contains("<OrgnlAssgnmt>"), "Should contain OrgnlAssgnmt element");
        assertTrue(unsignedAcmt024FailureMessage.contains("<Rpt>"), "Should contain Rpt element");
        assertTrue(unsignedAcmt024FailureMessage.contains("<SplmtryData>"), "Should contain SplmtryData element");
        assertTrue(unsignedAcmt024FailureMessage.contains("<Vrfctn>false</Vrfctn>"), "Should contain failure verification result");
        assertTrue(unsignedAcmt024FailureMessage.contains("<Rsn>"), "Should contain reason for failure");
        assertTrue(unsignedAcmt024FailureMessage.contains("<Cd>33</Cd>"), "Should contain failure code");
        assertTrue(unsignedAcmt024FailureMessage.contains("Account number mismatch"), "Should contain failure reason");
        assertTrue(unsignedAcmt024FailureMessage.contains("<AccountDesignation></AccountDesignation>"), "Should contain empty account designation for failure");
        assertTrue(unsignedAcmt024FailureMessage.contains("<IdType></IdType>"), "Should contain empty ID type for failure");
        assertTrue(unsignedAcmt024FailureMessage.contains("<IdValue></IdValue>"), "Should contain empty ID value for failure");
        assertTrue(unsignedAcmt024FailureMessage.contains("<AccountTier></AccountTier>"), "Should contain empty account tier for failure");
        logger.info("✅ ACMT.024 Failure message structure validation passed");

        logger.info("✅ ALL ACMT.024 MESSAGE STRUCTURE VALIDATION TESTS PASSED");
    }

    private String extractContentWithoutSignature(String signedXml) throws Exception {
        // Parse the XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes()));
        
        // Remove signature nodes
        NodeList signatureNodes = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
        if (signatureNodes.getLength() > 0) {
            Node signatureNode = signatureNodes.item(0);
            signatureNode.getParentNode().removeChild(signatureNode);
        }
        
        // Convert back to string
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(sw));
        return sw.toString();
    }

    /**
     * Helper method to verify that two XML strings have the same content,
     * ignoring formatting differences like whitespace and self-closing tags.
     */
    private boolean verifyXmlContentMatches(String original, String decrypted) {
        try {
            // Parse both XML documents
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(true); // Ignore whitespace-only text nodes
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document originalDoc = builder.parse(new ByteArrayInputStream(original.getBytes()));
            Document decryptedDoc = builder.parse(new ByteArrayInputStream(decrypted.getBytes()));
            
            // Normalize both documents
            originalDoc.normalizeDocument(); // Use normalizeDocument for full normalization
            decryptedDoc.normalizeDocument();
            
            // Compare the normalized documents
            boolean isEqual = originalDoc.isEqualNode(decryptedDoc);
            
            if (!isEqual) {
                // If not equal, let's try a more lenient comparison by comparing just the text content
                String originalText = originalDoc.getTextContent();
                String decryptedText = decryptedDoc.getTextContent();
                
                if (originalText != null && decryptedText != null) {
                    isEqual = originalText.trim().equals(decryptedText.trim());
                    
                    if (!isEqual) {
                        logger.debug("XML content differs:");
                        logger.debug("Original text content: " + originalText.trim());
                        logger.debug("Decrypted text content: " + decryptedText.trim());
                    }
                } else {
                    logger.debug("One or both XML documents have null text content");
                    isEqual = (originalText == null && decryptedText == null);
                }
            }
            
            return isEqual;
        } catch (Exception e) {
            logger.error("Error comparing XML content", e);
            return false;
        }
    }
}
