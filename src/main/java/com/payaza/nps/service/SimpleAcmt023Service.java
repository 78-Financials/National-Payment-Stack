package com.payaza.nps.service;

import com.payaza.nps.dto.Acmt023RequestDto;
import com.payaza.nps.dto.Acmt023ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;

/**
 * Simplified Service for handling ACMT.023 Identification Verification Request
 */
@Service
public class SimpleAcmt023Service {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAcmt023Service.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    public Acmt023ResponseDto processIdentificationVerification(Acmt023RequestDto request) throws Exception {
        logger.info("Processing ACMT.023 identification verification for message: {}", request.getMessageId());

        try {
            // Step 1: Convert JSON request to ISO 20022 XML
            String xmlMessage = convertRequestToXml(request);
            logger.debug("Generated ACMT.023 XML message");

            // Step 2: Generate test keys for signing and encryption
            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();

            // Step 3: Sign the XML message
            String signedXml = xmlSignatureService.signXmlDocument(xmlMessage, signatureKeyPair.getPrivate());
            logger.debug("ACMT.023 XML message signed successfully");

            // Step 4: Encrypt the signed XML message
            String encryptedXml = xmlEncryptionService.encryptXmlDocument(signedXml, encryptionKeyPair.getPublic());
            logger.debug("ACMT.023 XML message encrypted successfully");

            // Step 5: Send to NPS API (mock implementation)
            String npsResponse = sendToNps(encryptedXml);
            logger.info("ACMT.023 message sent to NPS successfully");

            // Step 6: Convert response to DTO
            Acmt023ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("ACMT.023 identification verification processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing ACMT.023 identification verification: {}", e.getMessage(), e);
            throw new Exception("Failed to process identification verification: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Acmt023RequestDto request) throws Exception {
        // Create a simple XML template for ACMT.023
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04\">\n" +
                "    <IdVrfctnReq>\n" +
                "        <Assgnmt>\n" +
                "            <Id>%s</Id>\n" +
                "            <Assgnr>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </Assgnr>\n" +
                "            <Assgne>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>999999</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </Assgne>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "        </Assgnmt>\n" +
                "        <Vrfctn>\n" +
                "            <Id>%s</Id>\n" +
                "            <VrfctnDtTm>%s</VrfctnDtTm>\n" +
                "            <VrfctnTp>ACCT</VrfctnTp>\n" +
                "            <VrfctnRslt>PENDING</VrfctnRslt>\n" +
                "            <VrfctnDtls>\n" +
                "                <AcctNb>%s</AcctNb>\n" +
                "                <AcctNm>%s</AcctNm>\n" +
                "                <Amt>\n" +
                "                    <Ccy>%s</Ccy>\n" +
                "                    <Value>%s</Value>\n" +
                "                </Amt>\n" +
                "            </VrfctnDtls>\n" +
                "        </Vrfctn>\n" +
                "    </IdVrfctnReq>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getReferenceNumber(),
                LocalDateTime.now().toString(),
                request.getAccountNumber(),
                request.getAccountName(),
                request.getCurrency(),
                request.getAmount().toString());
    }

    private String sendToNps(String encryptedXml) throws Exception {
        // Mock NPS API call - in real implementation, this would call the actual NPS endpoint
        logger.info("Sending ACMT.023 to NPS API (mock implementation)");
        
        // Simulate processing time
        Thread.sleep(100);
        
        // Return mock response
        return "SUCCESS";
    }

    private Acmt023ResponseDto convertResponseToDto(Acmt023RequestDto request, String npsResponse) {
        Acmt023ResponseDto response = new Acmt023ResponseDto();
        response.setMessageId(request.getMessageId());
        response.setResponseCode("00");
        response.setResponseMessage("Identification verification completed successfully");
        response.setStatus("SUCCESS");
        response.setAccountVerified(true);
        response.setAccountName(request.getAccountName());
        response.setBankCode(request.getBankCode());
        response.setProcessedAt(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
