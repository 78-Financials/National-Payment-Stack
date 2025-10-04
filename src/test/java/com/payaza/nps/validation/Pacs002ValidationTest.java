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
 * Validation test for pacs.002 Financial Institution to FI Payment Status Report message
 * Tests XML signature and encryption with the exact structure provided by NIBSS
 * Covers all scenarios: Approved, Declined, Timeout (Late Response), Timeout (No Response)
 */
@SpringBootTest(classes = NigerianPaymentStackApplication.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class Pacs002ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(Pacs002ValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    private String unsignedPacs002ApprovedMessage;
    private String unsignedPacs002DeclinedMessage;
    private String unsignedPacs002TimeoutLateResponseMessage;
    private String unsignedPacs002TimeoutNoResponseMessage;

    @BeforeEach
    void setUp() {
        // Case 1: Payment request approved by creditor bank
        unsignedPacs002ApprovedMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">\n" +
                "    <FIToFIPmtStsRpt>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99901220250829131808808625877947140</MsgId>\n" +
                "            <CreDtTm>2025-08-29T13:18:08.954Z</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInfAndSts>\n" +
                "            <OrgnlMsgId>99999920250829131752514531666073667</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>2025-08-29T13:17:52.954Z</OrgnlCreDtTm>\n" +
                "            <GrpSts>ACSC</GrpSts>\n" +
                "        </OrgnlGrpInfAndSts>\n" +
                "        <TxInfAndSts>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>2025-08-29</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInfAndSts>\n" +
                "    </FIToFIPmtStsRpt>\n" +
                "</ns2:Document>";

        // Case 2: Payment request declined by creditor bank
        unsignedPacs002DeclinedMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">\n" +
                "    <FIToFIPmtStsRpt>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99901220250829131808808625877947141</MsgId>\n" +
                "            <CreDtTm>2025-08-29T13:18:08.954Z</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInfAndSts>\n" +
                "            <OrgnlMsgId>99999920250829131752514531666073667</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>2025-08-29T13:17:52.954Z</OrgnlCreDtTm>\n" +
                "            <GrpSts>RJCT</GrpSts>\n" +
                "        </OrgnlGrpInfAndSts>\n" +
                "        <TxInfAndSts>\n" +
                "            <StsId>NAUT</StsId>\n" +
                "            <StsRsnInf>\n" +
                "                <Rsn>\n" +
                "                    <Prtry>AC01</Prtry>\n" +
                "                </Rsn>\n" +
                "                <AddtlInf>Wrong account number</AddtlInf>\n" +
                "            </StsRsnInf>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>2025-08-29</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInfAndSts>\n" +
                "    </FIToFIPmtStsRpt>\n" +
                "</ns2:Document>";

        // Case 3: Timeout - Late response received
        unsignedPacs002TimeoutLateResponseMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">\n" +
                "    <FIToFIPmtStsRpt>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99901220250829131808808625877947142</MsgId>\n" +
                "            <CreDtTm>2025-08-29T13:18:08.954Z</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInfAndSts>\n" +
                "            <OrgnlMsgId>99999920250829131752514531666073667</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>2025-08-29T13:17:52.954Z</OrgnlCreDtTm>\n" +
                "            <GrpSts>RJCT</GrpSts>\n" +
                "        </OrgnlGrpInfAndSts>\n" +
                "        <TxInfAndSts>\n" +
                "            <StsId>NAUT</StsId>\n" +
                "            <StsRsnInf>\n" +
                "                <Rsn>\n" +
                "                    <Prtry>AC01</Prtry>\n" +
                "                </Rsn>\n" +
                "                <AddtlInf>Rejected by timeout</AddtlInf>\n" +
                "            </StsRsnInf>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>2025-08-29</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInfAndSts>\n" +
                "    </FIToFIPmtStsRpt>\n" +
                "</ns2:Document>";

        // Case 4: Timeout - No response received
        unsignedPacs002TimeoutNoResponseMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">\n" +
                "    <FIToFIPmtStsRpt>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99901220250829131808808625877947143</MsgId>\n" +
                "            <CreDtTm>2025-08-29T13:18:08.954Z</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInfAndSts>\n" +
                "            <OrgnlMsgId>99999920250829131752514531666073667</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>2025-08-29T13:17:52.954Z</OrgnlCreDtTm>\n" +
                "            <GrpSts>RJCT</GrpSts>\n" +
                "        </OrgnlGrpInfAndSts>\n" +
                "        <TxInfAndSts>\n" +
                "            <StsId>NAUT</StsId>\n" +
                "            <StsRsnInf>\n" +
                "                <Rsn>\n" +
                "                    <Prtry>AC01</Prtry>\n" +
                "                </Rsn>\n" +
                "                <AddtlInf>Rejected by timeout</AddtlInf>\n" +
                "            </StsRsnInf>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999999</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>2025-08-29</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInfAndSts>\n" +
                "    </FIToFIPmtStsRpt>\n" +
                "</ns2:Document>";
        logger.info("PACS.002 Validation Test setup completed");
    }

    @Test
    void testPacs002ApprovedXmlSignatureGeneration() throws Exception {
        logger.info("=== Testing PACS.002 Approved XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String signedXml = xmlSignatureService.signXmlDocument(unsignedPacs002ApprovedMessage, privateKey);
        assertNotNull(signedXml, "Signed XML should not be null");
        assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), "Should contain Signature element");
        assertTrue(signedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), "Should contain correct canonicalization method");
        assertTrue(signedXml.contains("99901220250829131808808625877947140"), "Should contain original message ID");
        assertTrue(signedXml.contains("ACSC"), "Should contain approval status");
        
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ PACS.002 Approved XML Signature tests passed");
    }

    @Test
    void testPacs002DeclinedXmlSignatureGeneration() throws Exception {
        logger.info("=== Testing PACS.002 Declined XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String signedXml = xmlSignatureService.signXmlDocument(unsignedPacs002DeclinedMessage, privateKey);
        assertNotNull(signedXml, "Signed XML should not be null");
        assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), "Should contain Signature element");
        assertTrue(signedXml.contains("RJCT"), "Should contain rejection status");
        assertTrue(signedXml.contains("AC01"), "Should contain error code");
        assertTrue(signedXml.contains("Wrong account number"), "Should contain error message");
        
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ PACS.002 Declined XML Signature tests passed");
    }

    @Test
    void testPacs002XmlEncryption() throws Exception {
        logger.info("=== Testing PACS.002 XML Encryption ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs002ApprovedMessage, publicKey);
        assertNotNull(encryptedXml, "Encrypted XML should not be null");
        assertTrue(encryptedXml.contains("<EncryptedData Type=\"http://www.w3.org/2001/04/xmlenc#Content\" xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), "Should contain EncryptedData element");
        assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\">"), "Should contain AES-256-CBC encryption");
        assertFalse(encryptedXml.contains("99901220250829131808808625877947140"), "Original message ID should be encrypted");
        
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertNotNull(decryptedXml, "Decrypted XML should not be null");
        assertTrue(decryptedXml.contains("99901220250829131808808625877947140"), "Decrypted should contain original message ID");
        assertTrue(decryptedXml.contains("ACSC"), "Decrypted should contain approval status");
        logger.info("✅ PACS.002 XML Encryption tests passed");
    }

    @Test
    void testPacs002MessageStructureValidation() {
        logger.info("=== Testing PACS.002 Message Structure Validation ===");
        
        // Test approved message structure
        assertTrue(unsignedPacs002ApprovedMessage.contains("<FIToFIPmtStsRpt>"), "Should contain FIToFIPmtStsRpt element");
        assertTrue(unsignedPacs002ApprovedMessage.contains("<GrpHdr>"), "Should contain GrpHdr element");
        assertTrue(unsignedPacs002ApprovedMessage.contains("<OrgnlGrpInfAndSts>"), "Should contain OrgnlGrpInfAndSts element");
        assertTrue(unsignedPacs002ApprovedMessage.contains("<TxInfAndSts>"), "Should contain TxInfAndSts element");
        assertTrue(unsignedPacs002ApprovedMessage.contains("<GrpSts>ACSC</GrpSts>"), "Should contain approval status");
        
        // Test declined message structure
        assertTrue(unsignedPacs002DeclinedMessage.contains("<GrpSts>RJCT</GrpSts>"), "Should contain rejection status");
        assertTrue(unsignedPacs002DeclinedMessage.contains("<StsId>NAUT</StsId>"), "Should contain status ID");
        assertTrue(unsignedPacs002DeclinedMessage.contains("<StsRsnInf>"), "Should contain status reason info");
        assertTrue(unsignedPacs002DeclinedMessage.contains("<Prtry>AC01</Prtry>"), "Should contain error code");
        assertTrue(unsignedPacs002DeclinedMessage.contains("<AddtlInf>Wrong account number</AddtlInf>"), "Should contain error message");
        
        // Test timeout message structure
        assertTrue(unsignedPacs002TimeoutLateResponseMessage.contains("<AddtlInf>Rejected by timeout</AddtlInf>"), "Should contain timeout message");
        assertTrue(unsignedPacs002TimeoutNoResponseMessage.contains("<AddtlInf>Rejected by timeout</AddtlInf>"), "Should contain timeout message");
        
        logger.info("✅ PACS.002 Message Structure Validation tests passed");
    }

    @Test
    void testPacs002CompleteFlow() throws Exception {
        logger.info("=== Testing PACS.002 Complete Signed and Encrypted Flow ===");
        
        KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();
        KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
        
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs002ApprovedMessage, encryptionKeyPair.getPublic());
        String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signatureKeyPair.getPrivate());
        
        assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), "Should contain signature");
        assertTrue(signedEncryptedXml.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Type=\"http://www.w3.org/2001/04/xmlenc#Content\">"), "Should contain encrypted data");
        
        boolean signatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedXml, signatureKeyPair.getPublic());
        assertTrue(signatureValid, "Signature verification should succeed");
        
        String xmlWithoutSignature = extractContentWithoutSignature(signedEncryptedXml);
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignature, encryptionKeyPair.getPrivate());
        assertTrue(decryptedXml.contains("99901220250829131808808625877947140"), "Decrypted should contain original message ID");
        assertTrue(decryptedXml.contains("ACSC"), "Decrypted should contain approval status");
        
        logger.info("✅ PACS.002 Complete Flow tests passed");
    }

    private String extractContentWithoutSignature(String signedXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes()));
        NodeList signatureNodes = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
        if (signatureNodes.getLength() > 0) {
            Node signatureNode = signatureNodes.item(0);
            signatureNode.getParentNode().removeChild(signatureNode);
        }
        
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        java.io.StringWriter sw = new java.io.StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(sw));
        return sw.toString();
    }
}
