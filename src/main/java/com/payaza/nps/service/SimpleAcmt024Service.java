package com.payaza.nps.service;

import com.payaza.nps.dto.Acmt024RequestDto;
import com.payaza.nps.dto.Acmt024ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;

/**
 * Simplified Service for handling ACMT.024 Identification Verification Report
 */
@Service
public class SimpleAcmt024Service {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAcmt024Service.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    public Acmt024ResponseDto processIdentificationVerificationReport(Acmt024RequestDto request) throws Exception {
        logger.info("Processing ACMT.024 identification verification report for message: {}", request.getMessageId());

        try {
            // Step 1: Convert JSON request to ISO 20022 XML
            String xmlMessage = convertRequestToXml(request);
            logger.debug("Generated ACMT.024 XML message");

            // Step 2: Generate test keys for signing and encryption
            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();

            // Step 3: Sign the XML message
            String signedXml = xmlSignatureService.signXmlDocument(xmlMessage, signatureKeyPair.getPrivate());
            logger.debug("ACMT.024 XML message signed successfully");

            // Step 4: Encrypt the signed XML message
            String encryptedXml = xmlEncryptionService.encryptXmlDocument(signedXml, encryptionKeyPair.getPublic());
            logger.debug("ACMT.024 XML message encrypted successfully");

            // Step 5: Send to NPS API (mock implementation)
            String npsResponse = sendToNps(encryptedXml);
            logger.info("ACMT.024 message sent to NPS successfully");

            // Step 6: Convert response to DTO
            Acmt024ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("ACMT.024 identification verification report processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing ACMT.024 identification verification report: {}", e.getMessage(), e);
            throw new Exception("Failed to process identification verification report: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Acmt024RequestDto request) throws Exception {
        // Create XML template matching the exact NIBSS ACMT.024 structure
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04\">\n" +
                "    <IdVrfctnRpt>\n" +
                "        <Assgnmt>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <Assgnr>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI>%s</BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>%s</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgnr>\n" +
                "            <Assgne>\n" +
                "                <Pty>\n" +
                "                    <Nm>%s</Nm>\n" +
                "                </Pty>\n" +
                "                <Agt>\n" +
                "                    <FinInstnId>\n" +
                "                        <BICFI>%s</BICFI>\n" +
                "                        <ClrSysMmbId>\n" +
                "                            <MmbId>%s</MmbId>\n" +
                "                        </ClrSysMmbId>\n" +
                "                    </FinInstnId>\n" +
                "                </Agt>\n" +
                "            </Assgne>\n" +
                "        </Assgnmt>\n" +
                "        <OrgnlAssgnmt>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "        </OrgnlAssgnmt>\n" +
                "        <Rpt>\n" +
                "            <OrgnlId>%s</OrgnlId>\n" +
                "            <Vrfctn>%s</Vrfctn>\n" +
                "            <OrgnlPtyAndAcctId>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>%s</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </OrgnlPtyAndAcctId>\n" +
                "            <UpdtdPtyAndAcctId>\n" +
                "                <Pty>\n" +
                "                    <Nm>%s</Nm>\n" +
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
                "                        <IdValue>%s</IdValue>\n" +
                "                        <AccountTier>1</AccountTier>\n" +
                "                    </CreditorInfo>\n" +
                "                    <TransactionInfo>\n" +
                "                        <RiskRating>%s</RiskRating>\n" +
                "                    </TransactionInfo>\n" +
                "                </CustomData>\n" +
                "            </Envlp>\n" +
                "        </SplmtryData>\n" +
                "    </IdVrfctnRpt>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getAssignorBicfi() != null ? request.getAssignorBicfi() : "999058",
                request.getAssignorMemberId() != null ? request.getAssignorMemberId() : "044",
                request.getAssigneeBankName() != null ? request.getAssigneeBankName() : "XYZ Bank",
                request.getAssigneeBicfi() != null ? request.getAssigneeBicfi() : "999057",
                request.getAssigneeMemberId() != null ? request.getAssigneeMemberId() : "058",
                request.getMessageId(),
                request.getOriginalCreationDateTime() != null ? request.getOriginalCreationDateTime().toString() : LocalDateTime.now().toString(),
                request.getMessageId(),
                request.getVerificationResult() != null ? request.getVerificationResult().toString() : "true",
                request.getAccountNumber(),
                request.getAccountName(),
                request.getBvn() != null ? request.getBvn() : "2211232346",
                request.getRiskRating() != null ? request.getRiskRating() : "R000000000000000000B9");
    }

    private String sendToNps(String encryptedXml) throws Exception {
        logger.info("Sending ACMT.024 to NPS API (mock implementation)");
        Thread.sleep(100);
        return "SUCCESS";
    }

    private Acmt024ResponseDto convertResponseToDto(Acmt024RequestDto request, String npsResponse) {
        Acmt024ResponseDto response = new Acmt024ResponseDto();
        response.setMessageId(request.getMessageId());
        response.setOriginalMessageId(request.getOriginalMessageId());
        response.setResponseCode("00");
        response.setResponseMessage("Identification verification report processed successfully");
        response.setStatus("SUCCESS");
        response.setVerificationStatus(request.getVerificationStatus());
        response.setAccountVerified("SUCCESS".equalsIgnoreCase(request.getVerificationStatus()));
        response.setAccountName(request.getAccountName());
        response.setBankCode(request.getBankCode());
        response.setProcessedAt(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
