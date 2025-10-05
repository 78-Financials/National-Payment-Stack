package com.payaza.nps.validation;

import com.payaza.nps.service.NpsXmlSignatureService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Test to validate that our signature implementation matches the exact NIBSS specification
 * 
 * This test uses the exact example from the NIBSS signature guide to ensure compliance:
 * - Exclusive Canonicalization (http://www.w3.org/2001/10/xml-exc-c14n#)
 * - SHA-256 digest algorithm
 * - RSA-SHA256 signature algorithm
 * - Enveloped signature format
 */
@SpringBootTest
public class NibssSignatureValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(NibssSignatureValidationTest.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    /**
     * Test signature creation with the exact XML structure from NIBSS guide
     */
    @Test
    public void testNibssSignatureCompliance() throws Exception {
        logger.info("🧪 Testing NIBSS signature compliance with exact specification");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        
        // Create the exact XML structure from NIBSS guide (unsigned)
        String unsignedXml = createNibssSampleXml();
        logger.debug("Original unsigned XML:\n{}", unsignedXml);
        
        // Sign the XML using our service
        String signedXml = xmlSignatureService.signXmlDocument(unsignedXml, privateKey);
        logger.debug("Signed XML:\n{}", signedXml);
        
        // Verify the signature structure matches NIBSS specification
        validateSignatureStructure(signedXml);
        
        // Verify the signature is valid
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assert isValid : "Signature verification should succeed";
        
        logger.info("✅ NIBSS signature compliance test passed");
    }
    
    /**
     * Test that our signature produces the expected canonicalization method
     */
    @Test
    public void testCanonicalizationMethod() throws Exception {
        logger.info("🧪 Testing canonicalization method compliance");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        
        // Create sample XML
        String unsignedXml = createNibssSampleXml();
        
        // Sign the XML
        String signedXml = xmlSignatureService.signXmlDocument(unsignedXml, privateKey);
        
        // Verify canonicalization method is correct (using NIBSS-compliant method)
        assert signedXml.contains("Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"") :
            "Should use Exclusive Canonicalization method as per NIBSS spec";
        
        // Verify signature method is correct
        assert signedXml.contains("Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"") :
            "Should use RSA-SHA256 signature method";
        
        // Verify digest method is correct
        assert signedXml.contains("Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"") :
            "Should use SHA-256 digest method";
        
        // Verify enveloped signature transform
        assert signedXml.contains("Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"") :
            "Should use enveloped signature transform";
        
        logger.info("✅ Canonicalization method test passed");
    }
    
    /**
     * Test signature verification with the exact NIBSS example
     */
    @Test
    public void testNibssExampleSignature() throws Exception {
        logger.info("🧪 Testing with NIBSS example signature structure");
        
        // Generate test key pair
        KeyPair keyPair = generateTestKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        
        // Create XML matching NIBSS structure
        String unsignedXml = createNibssSampleXml();
        
        // Sign the XML
        String signedXml = xmlSignatureService.signXmlDocument(unsignedXml, privateKey);
        
        // Verify the signature contains all required elements
        validateSignatureStructure(signedXml);
        
        // Verify the signature is valid
        boolean isValid = xmlSignatureService.verifyXmlSignature(signedXml, publicKey);
        assert isValid : "NIBSS example signature should be valid";
        
        logger.info("✅ NIBSS example signature test passed");
    }
    
    private KeyPair generateTestKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    
    private String createNibssSampleXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>025-092-78199-001-00002</MsgId>\n" +
                "            <CreDtTm>2025-04-02T21:43:19.267Z</CreDtTm>\n" +
                "            <BtchBookg>false</BtchBookg>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <SttlmInf>\n" +
                "                <SttlmMtd>CLRG</SttlmMtd>\n" +
                "            </SttlmInf>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>INSTRUCTION-ID-2</InstrId>\n" +
                "                <EndToEndId>E2E-ID-2</EndToEndId>\n" +
                "                <TxId>025-092-78199-001-00002</TxId>\n" +
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
                "            <IntrBkSttlmAmt Ccy=\"NGN\">71000</IntrBkSttlmAmt>\n" +
                "            <IntrBkSttlmDt>2025-02-25Z</IntrBkSttlmDt>\n" +
                "            <ChrgBr>SLEV</ChrgBr>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>ABCBANK</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>XYZBANK</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>DEBTOR PERSON NAME 1</Nm>\n" +
                "            </Dbtr>\n" +
                "            <DbtrAcct>\n" +
                "                <Id>\n" +
                "                    <IBAN>NG22ART00000000000130493410</IBAN>\n" +
                "                </Id>\n" +
                "            </DbtrAcct>\n" +
                "            <DbtrAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>ABCBANK</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </DbtrAgt>\n" +
                "            <CdtrAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>XYZBANK</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </CdtrAgt>\n" +
                "            <Cdtr>\n" +
                "                <Nm>CREDITOR PERSON NAME</Nm>\n" +
                "            </Cdtr>\n" +
                "            <CdtrAcct>\n" +
                "                <Id>\n" +
                "                    <IBAN>NG95BAHL00000000000146403815</IBAN>\n" +
                "                </Id>\n" +
                "            </CdtrAcct>\n" +
                "            <InstrForNxtAgt>\n" +
                "                <InstrInf>/BNF/Beneficiary info</InstrInf>\n" +
                "            </InstrForNxtAgt>\n" +
                "            <InstrForNxtAgt>\n" +
                "                <InstrInf>/SMPL/Sample data</InstrInf>\n" +
                "            </InstrForNxtAgt>\n" +
                "            <RmtInf>\n" +
                "                <Ustrd>String of 140 chars</Ustrd>\n" +
                "            </RmtInf>\n" +
                "        </CdtTrfTxInf>\n" +
                "        <SplmtryData>\n" +
                "            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>\n" +
                "                <Envlp>\n" +
                "                    <CustomData>\n" +
                "                        <DebtorInfo>\n" +
                "                            <AccountDesignation>1</AccountDesignation>\n" +
                "                            <IdType>bvn</IdType> \n" +
                "                            <IdValue>2211232344</IdValue>\n" +
                "                            <AccountTier>1</AccountTier>\n" +
                "                        </DebtorInfo>\n" +
                "                        <DebtorMetadata>\n" +
                "                                <!-- <AnyOtherData>1</AnyOtherData > -->\n" +
                "                        </DebtorMetadata>\n" +
                "                        <CreditorInfo>\n" +
                "                            <AccountDesignation>1</AccountDesignation >\n" +
                "                            <IdType>bvn</IdType>\n" +
                "                            <IdValue>2211232346</IdValue>\n" +
                "                            <AccountTier>1</AccountTier>\n" +
                "                        </CreditorInfo>\n" +
                "                        <CreditorMetadata>\n" +
                "                            <!-- <AnyOtherData>...</AnyOtherData> -->\n" +
                "                        </CreditorMetadata>\n" +
                "                        <TransactionInfo>\n" +
                "                            <TransactionLocation>01080652440N020900337921E</TransactionLocation>\n" +
                "                            <NameEnquiryMsgId></NameEnquiryMsgId>\n" +
                "                            <ChannelCode>1</ChannelCode>\n" +
                "                            <RiskRating>R000000000000000000B9</RiskRating>\n" +
                "                        </TransactionInfo>\n" +
                "                    </CustomData>\n" +
                "                </Envlp>\n" +
                "        </SplmtryData>        \n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</ns2:Document>";
    }
    
    private void validateSignatureStructure(String signedXml) {
        // Verify signature element exists
        assert signedXml.contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">") :
            "Should contain signature element with correct namespace";
        
        // Verify SignedInfo structure
        assert signedXml.contains("<SignedInfo>") :
            "Should contain SignedInfo element";
        
        // Verify CanonicalizationMethod
        assert signedXml.contains("<CanonicalizationMethod") :
            "Should contain CanonicalizationMethod element";
        
        // Verify SignatureMethod
        assert signedXml.contains("<SignatureMethod") :
            "Should contain SignatureMethod element";
        
        // Verify Reference
        assert signedXml.contains("<Reference URI=\"\">") :
            "Should contain Reference element with empty URI (enveloped signature)";
        
        // Verify Transforms
        assert signedXml.contains("<Transforms>") :
            "Should contain Transforms element";
        
        // Verify DigestMethod
        assert signedXml.contains("<DigestMethod") :
            "Should contain DigestMethod element";
        
        // Verify DigestValue
        assert signedXml.contains("<DigestValue>") :
            "Should contain DigestValue element";
        
        // Verify SignatureValue
        assert signedXml.contains("<SignatureValue>") :
            "Should contain SignatureValue element";
        
        logger.debug("✅ Signature structure validation passed");
    }
}
