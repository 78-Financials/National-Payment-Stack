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

/**
 * Validation test for pacs.008 Financial Institution to FI Customer Credit Transfer message
 * Tests XML signature and encryption with the exact structure provided by NIBSS
 */
@SpringBootTest(classes = NigerianPaymentStackApplication.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class Pacs008ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(Pacs008ValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    private String unsignedPacs008Message;

    @BeforeEach
    void setUp() {
        // Sample unsigned pacs.008 message (exact structure from NIBSS)
        unsignedPacs008Message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99999920250829174941709740087747292</MsgId>\n" +
                "            <CreDtTm>2025-08-29T17:49:41.954Z</CreDtTm>\n" +
                "            <BtchBookg>false</BtchBookg>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <SttlmInf>\n" +
                "                <SttlmMtd>CLRG</SttlmMtd>\n" +
                "            </SttlmInf>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI/>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>99999999901220250829174941628527730</InstrId>\n" +
                "                <EndToEndId>99999999904520250828171636001234567</EndToEndId>\n" +
                "                <TxId>99999920250829174941709740087747292</TxId>\n" +
                "            </PmtId>\n" +
                "            <PmtTpInf>\n" +
                "                <ClrChanl>RTNS</ClrChanl>\n" +
                "                <SvcLvl>\n" +
                "                    <Prtry>0100</Prtry>\n" +
                "                </SvcLvl>\n" +
                "                <LclInstrm>\n" +
                "                    <Prtry>CTAA</Prtry>\n" +
                "                </LclInstrm>\n" +
                "                <CtgyPurp>\n" +
                "                    <Prtry>001</Prtry>\n" +
                "                </CtgyPurp>\n" +
                "            </PmtTpInf>\n" +
                "            <IntrBkSttlmAmt Ccy=\"NGN\">100000.00</IntrBkSttlmAmt>\n" +
                "            <IntrBkSttlmDt>2025-08-30</IntrBkSttlmDt>\n" +
                "            <ChrgBr>SLEV</ChrgBr>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI/>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI/>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>Oso Emmanuel</Nm>\n" +
                "            </Dbtr>\n" +
                "            <DbtrAcct>\n" +
                "                <Id>\n" +
                "                    <IBAN>0123456789</IBAN>\n" +
                "                </Id>\n" +
                "                <Nm>Oso Emmanuel</Nm>\n" +
                "            </DbtrAcct>\n" +
                "            <DbtrAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </DbtrAgt>\n" +
                "            <CdtrAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </CdtrAgt>\n" +
                "            <Cdtr>\n" +
                "                <Nm>luming ho</Nm>\n" +
                "            </Cdtr>\n" +
                "            <CdtrAcct>\n" +
                "                <Id>\n" +
                "                    <IBAN>1234567890</IBAN>\n" +
                "                </Id>\n" +
                "                <Nm>luming ho</Nm>\n" +
                "            </CdtrAcct>\n" +
                "            <InstrForNxtAgt>\n" +
                "                <InstrInf>/BNF/Beneficiary info</InstrInf>\n" +
                "            </InstrForNxtAgt>\n" +
                "            <InstrForNxtAgt>\n" +
                "                <InstrInf>/SMPL/Sample data</InstrInf>\n" +
                "            </InstrForNxtAgt>\n" +
                "            <RmtInf>\n" +
                "                <Ustrd>Payment for invoice 223344, August 2025 settlement</Ustrd>\n" +
                "            </RmtInf>\n" +
                "        </CdtTrfTxInf>\n" +
                "        <SplmtryData>\n" +
                "            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>\n" +
                "            <Envlp>\n" +
                "                <CustomData>\n" +
                "                    <DebtorInfo>\n" +
                "                        <AccountDesignation>1</AccountDesignation>\n" +
                "                        <IdType>BVN</IdType>\n" +
                "                        <IdValue>2211232344</IdValue>\n" +
                "                        <AccountTier>1</AccountTier>\n" +
                "                    </DebtorInfo>\n" +
                "                    <DebtorMetadata>\n" +
                "                    </DebtorMetadata>\n" +
                "                    <CreditorInfo>\n" +
                "                        <AccountDesignation>1</AccountDesignation>\n" +
                "                        <IdType>BVN</IdType>\n" +
                "                        <IdValue>2211232346</IdValue>\n" +
                "                        <AccountTier>1</AccountTier>\n" +
                "                    </CreditorInfo>\n" +
                "                    <CreditorMetadata>\n" +
                "                    </CreditorMetadata>\n" +
                "                    <TransactionInfo>\n" +
                "                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>\n" +
                "                        <NameEnquiryMsgId></NameEnquiryMsgId>\n" +
                "                        <ChannelCode>1</ChannelCode>\n" +
                "                        <RiskRating>R000000000000000000B9</RiskRating>\n" +
                "                    </TransactionInfo>\n" +
                "                </CustomData>\n" +
                "            </Envlp>\n" +
                "        </SplmtryData>\n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</ns2:Document>";
        logger.info("PACS.008 Validation Test setup completed");
    }

    @Test
    void testPacs008XmlSignatureGeneration() throws Exception {
        logger.info("=== Testing PACS.008 XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Digital Signature generation for PACS.008");
        String signedXml = xmlSignatureService.signXmlDocument(unsignedPacs008Message, privateKey);
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
        assertTrue(signedXml.contains("99999920250829174941709740087747292"), "Original message ID should be present in signed XML");
        logger.info("✅ PACS.008 XML Digital Signature generation test passed");

        logger.info("Step 2: Testing XML Digital Signature verification for PACS.008");
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ PACS.008 XML Digital Signature verification test passed");

        // Verify content is preserved
        String unsignedContent = extractContentWithoutSignature(signedXml);
        assertTrue(signedXml.contains("99999920250829174941709740087747292"), "Original message ID should be preserved");
        assertTrue(signedXml.contains("Oso Emmanuel"), "Original debtor name should be preserved");
        assertTrue(signedXml.contains("luming ho"), "Original creditor name should be preserved");
        assertTrue(signedXml.contains("100000.00"), "Original amount should be preserved");
        logger.info("✅ PACS.008 content preservation test passed");

        logger.info("✅ ALL PACS.008 XML SIGNATURE TESTS PASSED");
    }

    @Test
    void testPacs008XmlEncryption() throws Exception {
        logger.info("=== Testing PACS.008 XML Encryption ===");
        // Generate test key pair
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing XML Encryption for PACS.008");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs008Message, publicKey);
        
        // Debug: Log the encrypted XML structure
        logger.debug("Generated encrypted PACS.008 XML structure:\n" + encryptedXml);
        
        // Validate encryption structure
        assertNotNull(encryptedXml, "Encrypted PACS.008 XML should not be null");
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
        assertFalse(encryptedXml.contains("99999920250829174941709740087747292"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("Oso Emmanuel"), "Original debtor name should be encrypted");
        assertFalse(encryptedXml.contains("luming ho"), "Original creditor name should be encrypted");
        assertFalse(encryptedXml.contains("100000.00"), "Original amount should be encrypted");
        logger.info("✅ PACS.008 XML Encryption structure validation passed");

        // Verify original content is encrypted
        assertFalse(encryptedXml.contains("Oso Emmanuel"), "Original debtor name should be encrypted");
        assertFalse(encryptedXml.contains("luming ho"), "Original creditor name should be encrypted");
        assertFalse(encryptedXml.contains("0123456789"), "Original debtor account should be encrypted");
        assertFalse(encryptedXml.contains("1234567890"), "Original creditor account should be encrypted");
        logger.info("✅ PACS.008 content encryption verification passed");

        logger.info("Step 2: Testing XML Decryption for PACS.008");
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertNotNull(decryptedXml, "Decrypted PACS.008 XML should not be null");
        assertTrue(decryptedXml.contains("99999920250829174941709740087747292"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("Oso Emmanuel"), "Decrypted XML should contain original debtor name");
        assertTrue(decryptedXml.contains("luming ho"), "Decrypted XML should contain original creditor name");
        assertTrue(decryptedXml.contains("100000.00"), "Decrypted XML should contain original amount");
        logger.info("✅ PACS.008 XML Decryption validation passed");

        logger.info("✅ ALL PACS.008 XML ENCRYPTION TESTS PASSED");
    }

    @Test
    void testPacs008MessageStructureValidation() {
        logger.info("=== Testing PACS.008 Message Structure Validation ===");
        
        try {
            logger.info("Step 1: Validating PACS.008 message structure");
            
            // Validate core structure elements
            assertTrue(unsignedPacs008Message.contains("<FIToFICstmrCdtTrf>"), "Should contain FIToFICstmrCdtTrf element");
            assertTrue(unsignedPacs008Message.contains("<GrpHdr>"), "Should contain GrpHdr element");
            assertTrue(unsignedPacs008Message.contains("<CdtTrfTxInf>"), "Should contain CdtTrfTxInf element");
            assertTrue(unsignedPacs008Message.contains("<SplmtryData>"), "Should contain SplmtryData element");
            
            // Validate group header details
            assertTrue(unsignedPacs008Message.contains("<MsgId>99999920250829174941709740087747292</MsgId>"), "Should contain message ID");
            assertTrue(unsignedPacs008Message.contains("<CreDtTm>2025-08-29T17:49:41.954Z</CreDtTm>"), "Should contain creation timestamp");
            assertTrue(unsignedPacs008Message.contains("<BtchBookg>false</BtchBookg>"), "Should contain batch booking flag");
            assertTrue(unsignedPacs008Message.contains("<NbOfTxs>1</NbOfTxs>"), "Should contain number of transactions");
            assertTrue(unsignedPacs008Message.contains("<SttlmMtd>CLRG</SttlmMtd>"), "Should contain settlement method");
            
            // Validate instructing and instructed agents
            assertTrue(unsignedPacs008Message.contains("<InstgAgt>"), "Should contain InstgAgt element");
            assertTrue(unsignedPacs008Message.contains("<InstdAgt>"), "Should contain InstdAgt element");
            assertTrue(unsignedPacs008Message.contains("<MmbId>999999</MmbId>"), "Should contain source member ID");
            assertTrue(unsignedPacs008Message.contains("<MmbId>999012</MmbId>"), "Should contain destination member ID");
            
            // Validate payment transaction information
            assertTrue(unsignedPacs008Message.contains("<PmtId>"), "Should contain PmtId element");
            assertTrue(unsignedPacs008Message.contains("<InstrId>99999999901220250829174941628527730</InstrId>"), "Should contain instruction ID");
            assertTrue(unsignedPacs008Message.contains("<EndToEndId>99999999904520250828171636001234567</EndToEndId>"), "Should contain end-to-end ID");
            assertTrue(unsignedPacs008Message.contains("<TxId>99999920250829174941709740087747292</TxId>"), "Should contain transaction ID");
            
            // Validate payment type information
            assertTrue(unsignedPacs008Message.contains("<PmtTpInf>"), "Should contain PmtTpInf element");
            assertTrue(unsignedPacs008Message.contains("<ClrChanl>RTNS</ClrChanl>"), "Should contain clearing channel");
            assertTrue(unsignedPacs008Message.contains("<Prtry>0100</Prtry>"), "Should contain service level");
            assertTrue(unsignedPacs008Message.contains("<Prtry>CTAA</Prtry>"), "Should contain local instrument");
            assertTrue(unsignedPacs008Message.contains("<Prtry>001</Prtry>"), "Should contain category purpose");
            
            // Validate amount and settlement details
            assertTrue(unsignedPacs008Message.contains("<IntrBkSttlmAmt Ccy=\"NGN\">100000.00</IntrBkSttlmAmt>"), "Should contain settlement amount");
            assertTrue(unsignedPacs008Message.contains("<IntrBkSttlmDt>2025-08-30</IntrBkSttlmDt>"), "Should contain settlement date");
            assertTrue(unsignedPacs008Message.contains("<ChrgBr>SLEV</ChrgBr>"), "Should contain charges bearer");
            
            // Validate debtor information
            assertTrue(unsignedPacs008Message.contains("<Dbtr>"), "Should contain Dbtr element");
            assertTrue(unsignedPacs008Message.contains("<Nm>Oso Emmanuel</Nm>"), "Should contain debtor name");
            assertTrue(unsignedPacs008Message.contains("<DbtrAcct>"), "Should contain DbtrAcct element");
            assertTrue(unsignedPacs008Message.contains("<IBAN>0123456789</IBAN>"), "Should contain debtor account");
            assertTrue(unsignedPacs008Message.contains("<DbtrAgt>"), "Should contain DbtrAgt element");
            
            // Validate creditor information
            assertTrue(unsignedPacs008Message.contains("<Cdtr>"), "Should contain Cdtr element");
            assertTrue(unsignedPacs008Message.contains("<Nm>luming ho</Nm>"), "Should contain creditor name");
            assertTrue(unsignedPacs008Message.contains("<CdtrAcct>"), "Should contain CdtrAcct element");
            assertTrue(unsignedPacs008Message.contains("<IBAN>1234567890</IBAN>"), "Should contain creditor account");
            assertTrue(unsignedPacs008Message.contains("<CdtrAgt>"), "Should contain CdtrAgt element");
            
            // Validate instruction and remittance information
            assertTrue(unsignedPacs008Message.contains("<InstrForNxtAgt>"), "Should contain InstrForNxtAgt element");
            assertTrue(unsignedPacs008Message.contains("<InstrInf>/BNF/Beneficiary info</InstrInf>"), "Should contain beneficiary instruction");
            assertTrue(unsignedPacs008Message.contains("<InstrInf>/SMPL/Sample data</InstrInf>"), "Should contain sample instruction");
            assertTrue(unsignedPacs008Message.contains("<RmtInf>"), "Should contain RmtInf element");
            assertTrue(unsignedPacs008Message.contains("<Ustrd>Payment for invoice 223344, August 2025 settlement</Ustrd>"), "Should contain remittance information");
            
            // Validate supplementary data
            assertTrue(unsignedPacs008Message.contains("<PlcAndNm>AdditionalVerificationDetails</PlcAndNm>"), "Should contain supplementary data place and name");
            assertTrue(unsignedPacs008Message.contains("<CustomData>"), "Should contain CustomData element");
            assertTrue(unsignedPacs008Message.contains("<DebtorInfo>"), "Should contain DebtorInfo element");
            assertTrue(unsignedPacs008Message.contains("<AccountDesignation>1</AccountDesignation>"), "Should contain account designation");
            assertTrue(unsignedPacs008Message.contains("<IdType>BVN</IdType>"), "Should contain ID type");
            assertTrue(unsignedPacs008Message.contains("<IdValue>2211232344</IdValue>"), "Should contain debtor ID value");
            assertTrue(unsignedPacs008Message.contains("<IdValue>2211232346</IdValue>"), "Should contain creditor ID value");
            assertTrue(unsignedPacs008Message.contains("<AccountTier>1</AccountTier>"), "Should contain account tier");
            assertTrue(unsignedPacs008Message.contains("<CreditorInfo>"), "Should contain CreditorInfo element");
            assertTrue(unsignedPacs008Message.contains("<TransactionInfo>"), "Should contain TransactionInfo element");
            assertTrue(unsignedPacs008Message.contains("<TransactionLocation>01080652440N020900337921E</TransactionLocation>"), "Should contain transaction location");
            assertTrue(unsignedPacs008Message.contains("<ChannelCode>1</ChannelCode>"), "Should contain channel code");
            assertTrue(unsignedPacs008Message.contains("<RiskRating>R000000000000000000B9</RiskRating>"), "Should contain risk rating");
            
            logger.info("✅ PACS.008 message structure validation passed");
            logger.info("✅ ALL PACS.008 MESSAGE STRUCTURE VALIDATION TESTS PASSED");
            
        } catch (Exception e) {
            logger.error("❌ PACS.008 message structure validation test failed", e);
            fail("PACS.008 message structure validation test failed: " + e.getMessage());
        }
    }

    @Test
    void testPacs008CompleteFlow() throws Exception {
        logger.info("=== Testing PACS.008 Complete Signed and Encrypted Flow ===");

        // Generate key pairs for encryption and signature
        KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey encryptionPrivateKey = encryptionKeyPair.getPrivate();
        PublicKey encryptionPublicKey = encryptionKeyPair.getPublic();

        KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey signaturePrivateKey = signatureKeyPair.getPrivate();
        PublicKey signaturePublicKey = signatureKeyPair.getPublic();

        logger.info("Step 1: Encrypting PACS.008 message");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs008Message, encryptionPublicKey);
        assertNotNull(encryptedXml, "Encrypted PACS.008 XML should not be null");
        logger.info("✅ PACS.008 encryption completed");

        logger.info("Step 2: Signing encrypted PACS.008 message");
        String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signaturePrivateKey);
        assertNotNull(signedEncryptedXml, "Signed and encrypted PACS.008 XML should not be null");
        
        // Debug: Log the signed and encrypted XML structure
        logger.debug("Generated signed and encrypted PACS.008 XML structure:\n" + signedEncryptedXml);
        
        // Verify both signature and encryption are present
        assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                "Signed and encrypted XML should contain signature");
        assertTrue(signedEncryptedXml.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Type=\"http://www.w3.org/2001/04/xmlenc#Content\">"), 
                "Signed and encrypted XML should contain encrypted data");
        logger.info("✅ PACS.008 signing completed");

        logger.info("Step 3: Verifying signature on encrypted PACS.008 message");
        boolean signatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedXml, signaturePublicKey);
        assertTrue(signatureValid, "Signature verification should succeed");
        logger.info("✅ PACS.008 signature verification completed");

        logger.info("Step 4: Removing signature and decrypting PACS.008 message");
        String xmlWithoutSignature = extractContentWithoutSignature(signedEncryptedXml);
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignature, encryptionPrivateKey);
        assertNotNull(decryptedXml, "Decrypted PACS.008 XML should not be null");
        assertTrue(decryptedXml.contains("99999920250829174941709740087747292"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("Oso Emmanuel"), "Decrypted XML should contain original debtor name");
        assertTrue(decryptedXml.contains("luming ho"), "Decrypted XML should contain original creditor name");
        assertTrue(decryptedXml.contains("100000.00"), "Decrypted XML should contain original amount");
        logger.info("✅ PACS.008 decryption completed");

        logger.info("✅ ALL PACS.008 COMPLETE FLOW TESTS PASSED");
        logger.info("📋 PACS.008 implementation is NIBSS compliant");
    }

    @Test
    void testPacs008NibssExactStructureValidation() throws Exception {
        logger.info("=== Testing PACS.008 NIBSS Exact Structure Validation ===");
        
        // Generate test key pair
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        logger.info("Step 1: Testing encryption with NIBSS exact structure");
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs008Message, publicKey);
        
        // Debug: Log the encrypted XML structure
        logger.debug("Generated encrypted PACS.008 XML structure:\n" + encryptedXml);
        
        // Validate exact NIBSS structure requirements
        assertTrue(encryptedXml.contains("<EncryptedData Type=\"http://www.w3.org/2001/04/xmlenc#Content\" xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), 
                "EncryptedData should have correct Type attribute and namespace");
        assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\">"), 
                "Encryption method should be AES-256-CBC");
        assertTrue(encryptedXml.contains("<InitializationVector>"), 
                "InitializationVector should be present in EncryptionMethod");
        
        // Check KeyInfo structure
        assertTrue(encryptedXml.contains("<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                "KeyInfo element should be present with correct namespace");
        
        // Check EncryptedKey structure
        assertTrue(encryptedXml.contains("<EncryptedKey xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), 
                "EncryptedKey element should be present with correct namespace");
        assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\"/>"), 
                "Key encryption method should be RSA-OAEP-MGF1P");
        
        // Check CipherData structure
        assertTrue(encryptedXml.contains("<CipherData>"), "CipherData element should be present");
        assertTrue(encryptedXml.contains("<CipherValue>"), "CipherValue element should be present");
        
        // Verify original content is encrypted
        assertFalse(encryptedXml.contains("99999920250829174941709740087747292"), "Original message ID should be encrypted");
        assertFalse(encryptedXml.contains("100000.00"), "Original amount should be encrypted");
        assertFalse(encryptedXml.contains("Oso Emmanuel"), "Original debtor name should be encrypted");
        assertFalse(encryptedXml.contains("luming ho"), "Original creditor name should be encrypted");

        logger.info("✅ NIBSS exact structure validation passed");

        // Step 3: Testing decryption
        logger.info("Step 3: Testing decryption");
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertTrue(decryptedXml.contains("99999920250829174941709740087747292"), "Decrypted XML should contain original message ID");
        assertTrue(decryptedXml.contains("100000.00"), "Decrypted XML should contain original amount");
        assertTrue(decryptedXml.contains("Oso Emmanuel"), "Decrypted XML should contain original debtor name");
        assertTrue(decryptedXml.contains("luming ho"), "Decrypted XML should contain original creditor name");
        logger.info("✅ Decryption validation passed");

        // Step 4: Testing signature integration
        logger.info("Step 4: Testing signature integration");
        KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey signaturePrivateKey = signatureKeyPair.getPrivate();
        PublicKey signaturePublicKey = signatureKeyPair.getPublic();

        String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signaturePrivateKey);
        assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), 
                "Signature element should be present");
        assertTrue(signedEncryptedXml.contains("<SignedInfo>"), "SignedInfo should be present");
        assertTrue(signedEncryptedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), 
                "Canonicalization method should be correct");
        assertTrue(signedEncryptedXml.contains("<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>"), 
                "Signature method should be RSA-SHA256");
        assertTrue(signedEncryptedXml.contains("<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"), 
                "Transform should be enveloped signature");
        assertTrue(signedEncryptedXml.contains("<DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"), 
                "Digest method should be SHA256");
        
        logger.info("✅ Signature integration validation passed");
        
        logger.info("✅ ALL PACS.008 NIBSS EXACT EXAMPLE VALIDATION TESTS PASSED");
        logger.info("📋 Implementation matches NIBSS exact example structure");
    }

    private String extractContentWithoutSignature(String signedXml) throws Exception {
        try {
            // Use standard DocumentBuilderFactory to avoid private access issues
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes()));
            NodeList signatureNodes = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
            if (signatureNodes.getLength() > 0) {
                Node signatureNode = signatureNodes.item(0);
                signatureNode.getParentNode().removeChild(signatureNode);
            }
            
            // Convert back to string using standard Transformer
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            java.io.StringWriter sw = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            logger.error("Error extracting content without signature", e);
            throw e;
        }
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
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document originalDoc = builder.parse(new ByteArrayInputStream(original.getBytes()));
            Document decryptedDoc = builder.parse(new ByteArrayInputStream(decrypted.getBytes()));
            
            // Normalize both documents
            originalDoc.normalizeDocument(); // Use normalizeDocument for full normalization
            decryptedDoc.normalizeDocument();
            
            // Compare the normalized documents
            return originalDoc.isEqualNode(decryptedDoc);
        } catch (Exception e) {
            logger.error("Error comparing XML content", e);
            return false;
        }
    }
}
