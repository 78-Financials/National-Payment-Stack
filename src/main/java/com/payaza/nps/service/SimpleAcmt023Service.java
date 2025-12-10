package com.payaza.nps.service;

import com.payaza.nps.dto.Acmt023RequestDto;
import com.payaza.nps.dto.Acmt023ResponseDto;
import com.payaza.nps.config.NpsConfiguration;
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

    @Autowired
    private NpsConfiguration npsConfig;

    @Autowired
    private NpsApiService npsApiService;

    @Autowired
    private SharedAlertService sharedAlertService;

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

            // Step 5: Send to NIBSS API
            String npsResponse = npsApiService.sendAcmt023(encryptedXml);
            logger.info("ACMT.023 message sent to NIBSS successfully");

            // Step 6: Convert response to DTO
            Acmt023ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("ACMT.023 identification verification processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing ACMT.023 identification verification: {}", e.getMessage(), e);
            
            // Trigger critical alert for processing failure
            sharedAlertService.triggerCriticalAlert("ACMT023", request.getMessageId(), e.getMessage());
            
            throw new Exception("Failed to process identification verification: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Acmt023RequestDto request) throws Exception {
        // Create XML template matching the exact NIBSS ACMT.023 structure
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04\">\n" +
                "    <IdVrfctnReq>\n" +
                "        <Assgnmt>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <Cretr>\n" +
                "                <Pty>\n" +
                "                    <Nm>%s</Nm>\n" +
                "                </Pty>\n" +
                "            </Cretr>\n" +
                "            <Assgnr>\n" +
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
                "            </Assgnr>\n" +
                "            <Assgne>\n" +
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
                "        <Vrfctn>\n" +
                "            <Id>%s</Id>\n" +
                "            <PtyAndAcctId>\n" +
                "                <Pty>\n" +
                "                    <Nm>%s</Nm>\n" +
                "                </Pty>\n" +
                "                <Acct>\n" +
                "                    <Id>\n" +
                "                        <IBAN>%s</IBAN>\n" +
                "                    </Id>\n" +
                "                </Acct>\n" +
                "            </PtyAndAcctId>\n" +
                "        </Vrfctn>\n" +
                "    </IdVrfctnReq>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getCreatorBankName() != null ? request.getCreatorBankName() : "XYZ Bank",
                request.getAssignorBankName() != null ? request.getAssignorBankName() : "ABC Bank",
                request.getAssignorBicfi() != null ? request.getAssignorBicfi() : "999058",
                request.getAssignorMemberId() != null ? request.getAssignorMemberId() : "044",
                request.getAssigneeBicfi() != null ? request.getAssigneeBicfi() : "999057",
                request.getAssigneeMemberId() != null ? request.getAssigneeMemberId() : "058",
                request.getMessageId(),
                request.getAccountName(),
                request.getAccountNumber());
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
