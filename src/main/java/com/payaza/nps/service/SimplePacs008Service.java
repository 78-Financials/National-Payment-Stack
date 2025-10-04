package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;

/**
 * Simplified Service for handling PACS.008 Payment Request
 */
@Service
public class SimplePacs008Service {

    private static final Logger logger = LoggerFactory.getLogger(SimplePacs008Service.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    public Pacs008ResponseDto processPaymentRequest(Pacs008RequestDto request) throws Exception {
        logger.info("Processing PACS.008 payment request for message: {}", request.getMessageId());

        try {
            // Step 1: Convert JSON request to ISO 20022 XML
            String xmlMessage = convertRequestToXml(request);
            logger.debug("Generated PACS.008 XML message");

            // Step 2: Generate test keys for signing and encryption
            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();

            // Step 3: Sign the XML message
            String signedXml = xmlSignatureService.signXmlDocument(xmlMessage, signatureKeyPair.getPrivate());
            logger.debug("PACS.008 XML message signed successfully");

            // Step 4: Encrypt the signed XML message
            String encryptedXml = xmlEncryptionService.encryptXmlDocument(signedXml, encryptionKeyPair.getPublic());
            logger.debug("PACS.008 XML message encrypted successfully");

            // Step 5: Send to NPS API (mock implementation)
            String npsResponse = sendToNps(encryptedXml);
            logger.info("PACS.008 message sent to NPS successfully");

            // Step 6: Convert response to DTO
            Pacs008ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("PACS.008 payment request processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing PACS.008 payment request: {}", e.getMessage(), e);
            throw new Exception("Failed to process payment request: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Pacs008RequestDto request) throws Exception {
        // Create a simple XML template for PACS.008
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <SttlmPrty>NORM</SttlmPrty>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>%s</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>%s</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <TxId>%s</TxId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt>\n" +
                "                <Ccy>%s</Ccy>\n" +
                "                <Value>%s</Value>\n" +
                "            </IntrBkSttlmAmt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>%s</Nm>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>%s</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </Dbtr>\n" +
                "            <Cdtr>\n" +
                "                <Nm>%s</Nm>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>%s</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </Cdtr>\n" +
                "            <RmtInf>\n" +
                "                <Ustrd>%s</Ustrd>\n" +
                "            </RmtInf>\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getSenderInstitutionCode(),
                request.getReceiverInstitutionCode(),
                request.getTransactionId(),
                request.getCurrency(),
                request.getAmount().toString(),
                request.getSenderAccountName(),
                request.getSenderAccountNumber(),
                request.getReceiverAccountName(),
                request.getReceiverAccountNumber(),
                request.getPaymentPurpose());
    }

    private String sendToNps(String encryptedXml) throws Exception {
        logger.info("Sending PACS.008 to NPS API (mock implementation)");
        Thread.sleep(100);
        return "SUCCESS";
    }

    private Pacs008ResponseDto convertResponseToDto(Pacs008RequestDto request, String npsResponse) {
        Pacs008ResponseDto response = new Pacs008ResponseDto();
        response.setMessageId(request.getMessageId());
        response.setTransactionId(request.getTransactionId());
        response.setResponseCode("00");
        response.setResponseMessage("Payment request processed successfully");
        response.setStatus("SUCCESS");
        response.setNpsReference("NPS" + System.currentTimeMillis());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setSenderAccountNumber(request.getSenderAccountNumber());
        response.setReceiverAccountNumber(request.getReceiverAccountNumber());
        response.setProcessedAt(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
