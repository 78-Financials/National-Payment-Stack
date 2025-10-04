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
 * Validation test for pacs.028 Financial Institution to FI Payment Status Request message
 * Tests XML signature and encryption with the exact structure provided by NIBSS
 */
@SpringBootTest(classes = NigerianPaymentStackApplication.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class Pacs028ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(Pacs028ValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    private String unsignedPacs028Message;

    @BeforeEach
    void setUp() {
        unsignedPacs028Message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06\">\n" +
                "    <FIToFIPmtStsReq>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>99999920250829174941709740087747292</MsgId>\n" +
                "            <CreDtTm>2025-08-18T09:05:46.973Z</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999057</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInf>\n" +
                "            <OrgnlMsgId>99905820250802112346977904433112345</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>2025-02-25T00:02:35.072Z</OrgnlCreDtTm>\n" +
                "        </OrgnlGrpInf>\n" +
                "        <TxInf>\n" +
                "            <StsReqId>99999920250829174941709740087747292</StsReqId>\n" +
                "            <OrgnlTxId>99905820250802112346977904433112345</OrgnlTxId>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999057</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999057</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999012</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>999012</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>2025-02-25</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInf>\n" +
                "    </FIToFIPmtStsReq>\n" +
                "</ns2:Document>";
        logger.info("PACS.028 Validation Test setup completed");
    }

    @Test
    void testPacs028XmlSignatureGeneration() throws Exception {
        logger.info("=== Testing PACS.028 XML Signature Generation ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String signedXml = xmlSignatureService.signXmlDocument(unsignedPacs028Message, privateKey);
        assertNotNull(signedXml, "Signed XML should not be null");
        assertTrue(signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), "Should contain Signature element");
        assertTrue(signedXml.contains("<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"), "Should contain correct canonicalization method");
        assertTrue(signedXml.contains("99999920250829174941709740087747292"), "Should contain original message ID");
        assertTrue(signedXml.contains("pacs.008.001.12"), "Should contain original message name ID");
        
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assertTrue(isValid, "XML signature verification should succeed");
        logger.info("✅ PACS.028 XML Signature tests passed");
    }

    @Test
    void testPacs028XmlEncryption() throws Exception {
        logger.info("=== Testing PACS.028 XML Encryption ===");
        KeyPair keyPair = xmlEncryptionService.generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs028Message, publicKey);
        assertNotNull(encryptedXml, "Encrypted XML should not be null");
        assertTrue(encryptedXml.contains("<EncryptedData Type=\"http://www.w3.org/2001/04/xmlenc#Content\" xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"), "Should contain EncryptedData element");
        assertTrue(encryptedXml.contains("<EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes256-cbc\">"), "Should contain AES-256-CBC encryption");
        assertFalse(encryptedXml.contains("99999920250829174941709740087747292"), "Original message ID should be encrypted");
        
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(encryptedXml, privateKey);
        assertNotNull(decryptedXml, "Decrypted XML should not be null");
        assertTrue(decryptedXml.contains("99999920250829174941709740087747292"), "Decrypted should contain original message ID");
        assertTrue(decryptedXml.contains("pacs.008.001.12"), "Decrypted should contain original message name ID");
        logger.info("✅ PACS.028 XML Encryption tests passed");
    }

    @Test
    void testPacs028MessageStructureValidation() {
        logger.info("=== Testing PACS.028 Message Structure Validation ===");
        
        assertTrue(unsignedPacs028Message.contains("<FIToFIPmtStsReq>"), "Should contain FIToFIPmtStsReq element");
        assertTrue(unsignedPacs028Message.contains("<GrpHdr>"), "Should contain GrpHdr element");
        assertTrue(unsignedPacs028Message.contains("<OrgnlGrpInf>"), "Should contain OrgnlGrpInf element");
        assertTrue(unsignedPacs028Message.contains("<TxInf>"), "Should contain TxInf element");
        assertTrue(unsignedPacs028Message.contains("<MsgId>99999920250829174941709740087747292</MsgId>"), "Should contain message ID");
        assertTrue(unsignedPacs028Message.contains("<OrgnlMsgId>99905820250802112346977904433112345</OrgnlMsgId>"), "Should contain original message ID");
        assertTrue(unsignedPacs028Message.contains("<OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>"), "Should contain original message name ID");
        assertTrue(unsignedPacs028Message.contains("<StsReqId>99999920250829174941709740087747292</StsReqId>"), "Should contain status request ID");
        assertTrue(unsignedPacs028Message.contains("<OrgnlTxId>99905820250802112346977904433112345</OrgnlTxId>"), "Should contain original transaction ID");
        
        logger.info("✅ PACS.028 Message Structure Validation tests passed");
    }

    @Test
    void testPacs028CompleteFlow() throws Exception {
        logger.info("=== Testing PACS.028 Complete Signed and Encrypted Flow ===");
        
        KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();
        KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
        
        String encryptedXml = xmlEncryptionService.encryptXmlDocument(unsignedPacs028Message, encryptionKeyPair.getPublic());
        String signedEncryptedXml = xmlSignatureService.signXmlDocument(encryptedXml, signatureKeyPair.getPrivate());
        
        assertTrue(signedEncryptedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"), "Should contain signature");
        assertTrue(signedEncryptedXml.contains("<EncryptedData xmlns=\"http://www.w3.org/2001/04/xmlenc#\" Type=\"http://www.w3.org/2001/04/xmlenc#Content\">"), "Should contain encrypted data");
        
        boolean signatureValid = xmlSignatureService.verifyXmlSignature(signedEncryptedXml, signatureKeyPair.getPublic());
        assertTrue(signatureValid, "Signature verification should succeed");
        
        String xmlWithoutSignature = extractContentWithoutSignature(signedEncryptedXml);
        String decryptedXml = xmlEncryptionService.decryptXmlDocument(xmlWithoutSignature, encryptionKeyPair.getPrivate());
        assertTrue(decryptedXml.contains("99999920250829174941709740087747292"), "Decrypted should contain original message ID");
        assertTrue(decryptedXml.contains("pacs.008.001.12"), "Decrypted should contain original message name ID");
        
        logger.info("✅ PACS.028 Complete Flow tests passed");
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
