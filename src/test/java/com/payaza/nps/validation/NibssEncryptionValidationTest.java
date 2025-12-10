package com.payaza.nps.validation;

import com.payaza.nps.service.NpsXmlEncryptionService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Test to validate that our encryption implementation matches the exact NIBSS specification
 * 
 * This test validates the encryption process as specified by NIBSS:
 * - AES-256-CBC for payload encryption (not AES-256-GCM)
 * - RSA-OAEP-MGF1P for session key encryption
 * - Document-level encryption (entire Document element replaced with EncryptedData)
 */
@SpringBootTest(classes = {
    com.payaza.nps.service.NpsXmlSignatureService.class,
    com.payaza.nps.service.NpsXmlEncryptionService.class
})
@ActiveProfiles("test")
@Import(com.payaza.nps.config.TestApplicationConfig.class)
public class NibssEncryptionValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(NibssEncryptionValidationTest.class);

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    /**
     * Test encryption with the correct NIBSS specification
     */
    @Test
    public void testNibssEncryptionCompliance() throws Exception {
        logger.info("🧪 Testing NIBSS encryption compliance with exact specification");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        
        // Create sample XML document
        String originalXml = createSampleXmlDocument();
        logger.debug("Original XML:\n{}", originalXml);
        
        // Encrypt the entire document
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(originalXml, publicKey);
        logger.debug("Encrypted XML:\n{}", encryptedXml);
        
        // Validate encryption structure matches NIBSS specification
        validateEncryptionStructure(encryptedXml);
        
        // Test decryption
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        logger.debug("Decrypted XML:\n{}", decryptedXml);
        
        // Verify decryption produces the original content
        assert decryptedXml.contains("<ns2:Document") : "Decrypted XML should contain Document element";
        assert decryptedXml.contains("TEST-MESSAGE-ID") : "Decrypted XML should contain original message ID";
        
        logger.info("✅ NIBSS encryption compliance test passed");
    }
    
    /**
     * Test that our encryption uses AES-256-CBC (not AES-256-GCM)
     */
    @Test
    public void testEncryptionAlgorithm() throws Exception {
        logger.info("🧪 Testing encryption algorithm compliance");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        
        // Create sample XML document
        String originalXml = createSampleXmlDocument();
        
        // Encrypt the document
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(originalXml, publicKey);
        
        // Verify encryption method is AES-256-CBC (as per NIBSS example)
        assert encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\">") :
            "Should use AES-256-CBC encryption method as per NIBSS example";
        
        // Verify key encryption method is RSA-OAEP
        assert encryptedXml.contains("Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\"") :
            "Should use RSA-OAEP-MGF1P for session key encryption";
        
        // Verify document-level encryption (Type="Content")
        assert encryptedXml.contains("Type=\"http://www.w3.org/2001/04/xmlenc#Content\"") :
            "Should use document-level encryption (Content type)";
        
        logger.info("✅ Encryption algorithm test passed");
    }
    
    /**
     * Test document-level encryption as specified by NIBSS
     */
    @Test
    public void testDocumentLevelEncryption() throws Exception {
        logger.info("🧪 Testing document-level encryption");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        
        // Create sample XML document
        String originalXml = createSampleXmlDocument();
        
        // Encrypt the document
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(originalXml, publicKey);
        
        // Verify that the original message content is encrypted (but structure preserved)
        assert !encryptedXml.contains("<GrpHdr>") :
            "Original content should be encrypted and not visible";
        
        // Verify that EncryptedData is present
        assert encryptedXml.contains("<EncryptedData") && encryptedXml.contains("xmlns=\"http://www.w3.org/2001/04/xmlenc#\"") :
            "Should contain EncryptedData element";
        
        // Verify that the Document element structure is preserved
        assert encryptedXml.contains("<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\">") :
            "Document element structure should be preserved";
        
        // Test decryption to ensure we get back the original content
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assert decryptedXml.contains("<FIToFICstmrCdtTrf>") :
            "Decrypted content should contain the original FIToFICstmrCdtTrf element";
        
        logger.info("✅ Document-level encryption test passed");
    }
    
    private KeyPair generateTestKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    
    private String createSampleXmlDocument() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>TEST-MESSAGE-ID</MsgId>\n" +
                "            <CreDtTm>2025-04-02T21:43:19.267Z</CreDtTm>\n" +
                "            <BtchBookg>false</BtchBookg>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <SttlmInf>\n" +
                "                <SttlmMtd>CLRG</SttlmMtd>\n" +
                "            </SttlmInf>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>INSTRUCTION-ID</InstrId>\n" +
                "                <EndToEndId>E2E-ID</EndToEndId>\n" +
                "                <TxId>TRANSACTION-ID</TxId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"NGN\">1000.00</IntrBkSttlmAmt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>Test Debtor</Nm>\n" +
                "            </Dbtr>\n" +
                "            <Cdtr>\n" +
                "                <Nm>Test Creditor</Nm>\n" +
                "            </Cdtr>\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</ns2:Document>";
    }
    
    private void validateEncryptionStructure(String encryptedXml) {
        // Verify EncryptedData element exists (namespace is an attribute)
        assert encryptedXml.contains("<EncryptedData") && encryptedXml.contains("xmlns=\"http://www.w3.org/2001/04/xmlenc#\"") :
            "Should contain EncryptedData element with correct namespace";
        
        // Verify EncryptionMethod for content encryption
        assert encryptedXml.contains("<EncryptionMethod") :
            "Should contain EncryptionMethod element";
        
        // Verify KeyInfo element
        assert encryptedXml.contains("<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">") :
            "Should contain KeyInfo element with correct namespace";
        
        // Verify EncryptedKey element (namespace is an attribute)
        assert encryptedXml.contains("<EncryptedKey") && encryptedXml.contains("xmlns=\"http://www.w3.org/2001/04/xmlenc#\"") :
            "Should contain EncryptedKey element";
        
        // Verify CipherData element
        assert encryptedXml.contains("<CipherData>") :
            "Should contain CipherData element";
        
        // Verify CipherValue element
        assert encryptedXml.contains("<CipherValue>") :
            "Should contain CipherValue element";
        
        logger.debug("✅ Encryption structure validation passed");
    }
}
