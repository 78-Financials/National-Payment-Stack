package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs002RequestDto;
import com.payaza.nps.dto.Pacs002ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;

/**
 * Simplified Service for handling PACS.002 Payment Status Report
 */
@Service
public class SimplePacs002Service {

    private static final Logger logger = LoggerFactory.getLogger(SimplePacs002Service.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    public Pacs002ResponseDto processPaymentStatusReport(Pacs002RequestDto request) throws Exception {
        logger.info("Processing PACS.002 payment status report for message: {}", request.getMessageId());

        try {
            String xmlMessage = convertRequestToXml(request);
            logger.debug("Generated PACS.002 XML message");

            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();

            String signedXml = xmlSignatureService.signXmlDocument(xmlMessage, signatureKeyPair.getPrivate());
            logger.debug("PACS.002 XML message signed successfully");

            String encryptedXml = xmlEncryptionService.encryptXmlDocument(signedXml, encryptionKeyPair.getPublic());
            logger.debug("PACS.002 XML message encrypted successfully");

            String npsResponse = sendToNps(encryptedXml);
            logger.info("PACS.002 message sent to NPS successfully");

            Pacs002ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("PACS.002 payment status report processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing PACS.002 payment status report: {}", e.getMessage(), e);
            throw new Exception("Failed to process payment status report: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Pacs002RequestDto request) throws Exception {
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12\">\n" +
                "    <FIToFIPmtStsRpt>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInfAndSts>\n" +
                "            <OrgnlMsgId>%s</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>%s</OrgnlCreDtTm>\n" +
                "            <GrpSts>%s</GrpSts>\n" +
                "        </OrgnlGrpInfAndSts>\n" +
                "        <TxInfAndSts>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>%s</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInfAndSts>\n" +
                "    </FIToFIPmtStsRpt>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getInstgAgentMemberId() != null ? request.getInstgAgentMemberId() : "999058",
                request.getInstdAgentMemberId() != null ? request.getInstdAgentMemberId() : "999057",
                request.getOriginalMessageId(),
                request.getOriginalCreationDateTime() != null ? request.getOriginalCreationDateTime().toString() : LocalDateTime.now().toString(),
                request.getStatus(),
                request.getInstgAgentMemberId() != null ? request.getInstgAgentMemberId() : "999058",
                request.getInstdAgentMemberId() != null ? request.getInstdAgentMemberId() : "999057",
                request.getSettlementDate() != null ? request.getSettlementDate() : LocalDateTime.now().toLocalDate().toString() + "Z");
    }

    private String sendToNps(String encryptedXml) throws Exception {
        logger.info("Sending PACS.002 to NPS API (mock implementation)");
        Thread.sleep(100);
        return "SUCCESS";
    }

    private Pacs002ResponseDto convertResponseToDto(Pacs002RequestDto request, String npsResponse) {
        Pacs002ResponseDto response = new Pacs002ResponseDto();
        response.setMessageId(request.getMessageId());
        response.setOriginalMessageId(request.getOriginalMessageId());
        response.setResponseCode("00");
        response.setResponseMessage("Payment status report processed successfully");
        response.setStatus("ACTC");
        response.setPaymentStatus(request.getStatus());
        response.setStatusReason(request.getStatusReason());
        response.setStatusReasonCode(request.getStatusReasonCode());
        response.setAdditionalInformation(request.getAdditionalInformation());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setProcessedAt(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
