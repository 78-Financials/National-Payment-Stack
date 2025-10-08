package com.payaza.nps.validation;

import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.service.Pacs008XmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating PACS.008 callback message parsing and decryption
 * 
 * This test validates that all fields from the PACS.008 XML are correctly
 * extracted and parsed into the Pacs008ResponseDto object.
 */
@SpringBootTest(classes = {
    com.payaza.nps.service.Pacs008XmlParser.class,
    com.payaza.nps.service.NpsXmlDecryptionService.class
})
@ActiveProfiles("test")
@Import(com.payaza.nps.config.TestApplicationConfig.class)
@TestPropertySource(properties = {
    "logging.level.com.payaza.nps=DEBUG"
})
public class Pacs008CallbackValidationTest {

    private Pacs008XmlParser pacs008XmlParser;

    @BeforeEach
    void setUp() {
        pacs008XmlParser = new Pacs008XmlParser();
    }

    @Test
    void testParsePacs008Xml() throws Exception {
        // Test parsing PACS.008 XML
        String pacs008Xml = getPacs008Xml();
        
        Pacs008ResponseDto response = pacs008XmlParser.parsePacs008Xml(pacs008Xml);
        
        // Validate parsed data
        assertNotNull(response);
        assertEquals("99999920250829174941709740087747292", response.getMessageId());
        assertEquals("99999999901220250829174941628527730", response.getInstructionId());
        assertEquals("99999999904520250828171636001234567", response.getEndToEndId());
        assertEquals("99999920250829174941709740087747292", response.getTransactionId());
        
        // Validate Amount and Currency
        assertNotNull(response.getAmount());
        assertEquals(0, response.getAmount().compareTo(new java.math.BigDecimal("100000.00")));
        assertEquals("NGN", response.getCurrency());
        
        // Validate Settlement Information
        assertEquals("2025-08-30", response.getSettlementDate());
        assertEquals("SLEV", response.getChargeBearer());
        assertEquals(false, response.getBatchBooking());
        assertEquals("1", response.getNumberOfTransactions());
        assertEquals("CLRG", response.getSettlementMethod());
        assertEquals("2025-08-29T17:49:41.954Z", response.getCreationDateTime());
        
        // Validate Agent Information
        assertEquals("999999", response.getInstgAgentBicfi());
        assertEquals("999999", response.getInstgAgentMemberId());
        assertEquals("", response.getInstdAgentBicfi()); // Empty in sample XML
        assertEquals("999012", response.getInstdAgentMemberId());
        assertEquals("999999", response.getDbtrAgentMemberId());
        assertEquals("999012", response.getCdtrAgentMemberId());
        
        // Validate Party Information
        assertEquals("Oso Emmanuel", response.getSenderAccountName());
        assertEquals("0123456789", response.getSenderAccountNumber());
        assertEquals("luming ho", response.getReceiverAccountName());
        assertEquals("1234567890", response.getReceiverAccountNumber());
        
        // Validate Payment Type Information
        assertEquals("RTNS", response.getClearingChannel());
        assertEquals("0100", response.getServiceLevel());
        assertEquals("CTAA", response.getLocalInstrument());
        assertEquals("001", response.getCategoryPurpose());
        
        // Validate Instructions and Remittance
        assertNotNull(response.getInstructionsForNextAgent());
        assertTrue(response.getInstructionsForNextAgent().contains("Beneficiary info"));
        assertTrue(response.getInstructionsForNextAgent().contains("Sample data"));
        assertEquals("Payment for invoice 223344, August 2025 settlement", response.getRemittanceInformation());
        
        // Validate Supplementary Data
        assertEquals("2211232344", response.getDebtorBvn());
        assertEquals("1", response.getDebtorAccountDesignation());
        assertEquals("1", response.getDebtorAccountTier());
        assertEquals("2211232346", response.getCreditorBvn());
        assertEquals("1", response.getCreditorAccountDesignation());
        assertEquals("1", response.getCreditorAccountTier());
        assertEquals("01080652440N020900337921E", response.getTransactionLocation());
        assertEquals("", response.getNameEnquiryMsgId()); // Empty in sample XML
        assertEquals("1", response.getChannelCode());
        assertEquals("R000000000000000000B9", response.getRiskRating());
        
        System.out.println("✅ PACS.008 XML parsed successfully");
        System.out.println("Message ID: " + response.getMessageId());
        System.out.println("Transaction ID: " + response.getTransactionId());
        System.out.println("Amount: " + response.getAmount() + " " + response.getCurrency());
        System.out.println("Sender: " + response.getSenderAccountName() + " (" + response.getSenderAccountNumber() + ")");
        System.out.println("Receiver: " + response.getReceiverAccountName() + " (" + response.getReceiverAccountNumber() + ")");
        
        // Display Payment Information
        System.out.println("--- Payment Information ---");
        System.out.println("Instruction ID: " + response.getInstructionId());
        System.out.println("End-to-End ID: " + response.getEndToEndId());
        System.out.println("Settlement Date: " + response.getSettlementDate());
        System.out.println("Charge Bearer: " + response.getChargeBearer());
        System.out.println("Batch Booking: " + response.getBatchBooking());
        System.out.println("Number of Transactions: " + response.getNumberOfTransactions());
        System.out.println("Settlement Method: " + response.getSettlementMethod());
        System.out.println("Creation DateTime: " + response.getCreationDateTime());
        
        // Display Agent Information
        System.out.println("--- Agent Information ---");
        System.out.println("Instructing Agent BICFI: " + response.getInstgAgentBicfi());
        System.out.println("Instructing Agent Member ID: " + response.getInstgAgentMemberId());
        System.out.println("Instructed Agent BICFI: " + response.getInstdAgentBicfi());
        System.out.println("Instructed Agent Member ID: " + response.getInstdAgentMemberId());
        System.out.println("Debtor Agent Member ID: " + response.getDbtrAgentMemberId());
        System.out.println("Creditor Agent Member ID: " + response.getCdtrAgentMemberId());
        
        // Display Party Information
        System.out.println("--- Party Information ---");
        System.out.println("Sender Account Name: " + response.getSenderAccountName());
        System.out.println("Sender Account Number: " + response.getSenderAccountNumber());
        System.out.println("Receiver Account Name: " + response.getReceiverAccountName());
        System.out.println("Receiver Account Number: " + response.getReceiverAccountNumber());
        
        // Display Payment Type Information
        System.out.println("--- Payment Type Information ---");
        System.out.println("Clearing Channel: " + response.getClearingChannel());
        System.out.println("Service Level: " + response.getServiceLevel());
        System.out.println("Local Instrument: " + response.getLocalInstrument());
        System.out.println("Category Purpose: " + response.getCategoryPurpose());
        
        // Display Instructions and Remittance
        System.out.println("--- Instructions and Remittance ---");
        System.out.println("Instructions for Next Agent: " + response.getInstructionsForNextAgent());
        System.out.println("Remittance Information: " + response.getRemittanceInformation());
        
        // Display Supplementary Data
        System.out.println("--- Supplementary Data ---");
        System.out.println("Debtor BVN: " + response.getDebtorBvn());
        System.out.println("Debtor Account Designation: " + response.getDebtorAccountDesignation());
        System.out.println("Debtor Account Tier: " + response.getDebtorAccountTier());
        System.out.println("Creditor BVN: " + response.getCreditorBvn());
        System.out.println("Creditor Account Designation: " + response.getCreditorAccountDesignation());
        System.out.println("Creditor Account Tier: " + response.getCreditorAccountTier());
        System.out.println("Transaction Location: " + response.getTransactionLocation());
        System.out.println("Name Enquiry Message ID: " + response.getNameEnquiryMsgId());
        System.out.println("Channel Code: " + response.getChannelCode());
        System.out.println("Risk Rating: " + response.getRiskRating());
    }

    private String getPacs008Xml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
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
                "                        <!-- <AnyOtherData>1</AnyOtherData > -->\n" +
                "                    </DebtorMetadata>\n" +
                "                    <CreditorInfo>\n" +
                "                        <AccountDesignation>1</AccountDesignation>\n" +
                "                        <IdType>BVN</IdType>\n" +
                "                        <IdValue>2211232346</IdValue>\n" +
                "                        <AccountTier>1</AccountTier>\n" +
                "                    </CreditorInfo>\n" +
                "                    <CreditorMetadata>\n" +
                "                        <!-- <AnyOtherData>...</AnyOtherData> -->\n" +
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
    }
}
