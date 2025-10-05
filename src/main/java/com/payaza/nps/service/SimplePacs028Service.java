package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs028RequestDto;
import com.payaza.nps.dto.Pacs028ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;

/**
 * Simplified Service for handling PACS.028 Payment Status Request
 */
@Service
public class SimplePacs028Service {

    private static final Logger logger = LoggerFactory.getLogger(SimplePacs028Service.class);

    @Autowired
    private NpsXmlSignatureService xmlSignatureService;

    @Autowired
    private NpsXmlEncryptionService xmlEncryptionService;

    public Pacs028ResponseDto processPaymentStatusRequest(Pacs028RequestDto request) throws Exception {
        logger.info("Processing PACS.028 payment status request for message: {}", request.getMessageId());

        try {
            String xmlMessage = convertRequestToXml(request);
            logger.debug("Generated PACS.028 XML message");

            KeyPair signatureKeyPair = xmlEncryptionService.generateTestKeyPair();
            KeyPair encryptionKeyPair = xmlEncryptionService.generateTestKeyPair();

            String signedXml = xmlSignatureService.signXmlDocument(xmlMessage, signatureKeyPair.getPrivate());
            logger.debug("PACS.028 XML message signed successfully");

            String encryptedXml = xmlEncryptionService.encryptXmlDocument(signedXml, encryptionKeyPair.getPublic());
            logger.debug("PACS.028 XML message encrypted successfully");

            String npsResponse = sendToNps(encryptedXml);
            logger.info("PACS.028 message sent to NPS successfully");

            Pacs028ResponseDto response = convertResponseToDto(request, npsResponse);
            logger.info("PACS.028 payment status request processed successfully");

            return response;

        } catch (Exception e) {
            logger.error("Error processing PACS.028 payment status request: {}", e.getMessage(), e);
            throw new Exception("Failed to process payment status request: " + e.getMessage(), e);
        }
    }

    private String convertRequestToXml(Pacs028RequestDto request) throws Exception {
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06\">\n" +
                "    <FIToFIPmtStsReq>\n" +
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
                "        </GrpHdr>\n" +
                "        <OrgnlGrpInf>\n" +
                "            <OrgnlMsgId>%s</OrgnlMsgId>\n" +
                "            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>\n" +
                "            <OrgnlCreDtTm>%s</OrgnlCreDtTm>\n" +
                "        </OrgnlGrpInf>\n" +
                "        <TxInf>\n" +
                "            <StsReqId>%s</StsReqId>\n" +
                "            <OrgnlTxId>%s</OrgnlTxId>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>%s</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>%s</BICFI>\n" +
                "                    <ClrSysMmbId>\n" +
                "                        <MmbId>%s</MmbId>\n" +
                "                    </ClrSysMmbId>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <OrgnlTxRef>\n" +
                "                <IntrBkSttlmDt>%s</IntrBkSttlmDt>\n" +
                "            </OrgnlTxRef>\n" +
                "        </TxInf>\n" +
                "    </FIToFIPmtStsReq>\n" +
                "</ns2:Document>",
                request.getMessageId(),
                LocalDateTime.now().toString(),
                request.getInstgAgentMemberId() != null ? request.getInstgAgentMemberId() : "999057",
                request.getOriginalMessageId(),
                request.getOriginalCreationDateTime() != null ? request.getOriginalCreationDateTime().toString() : LocalDateTime.now().toString(),
                request.getStatusRequestId(),
                request.getOriginalTransactionId(),
                request.getInstgAgentBicfi() != null ? request.getInstgAgentBicfi() : "999057",
                request.getInstgAgentMemberId() != null ? request.getInstgAgentMemberId() : "999057",
                request.getInstdAgentBicfi() != null ? request.getInstdAgentBicfi() : "999012",
                request.getInstdAgentMemberId() != null ? request.getInstdAgentMemberId() : "999012",
                request.getSettlementDate() != null ? request.getSettlementDate() : LocalDateTime.now().toLocalDate().toString());
    }

    private String sendToNps(String encryptedXml) throws Exception {
        logger.info("Sending PACS.028 to NPS API (mock implementation)");
        Thread.sleep(100);
        return "SUCCESS";
    }

    private Pacs028ResponseDto convertResponseToDto(Pacs028RequestDto request, String npsResponse) {
        Pacs028ResponseDto response = new Pacs028ResponseDto();
        response.setMessageId(request.getMessageId());
        response.setOriginalMessageId(request.getOriginalMessageId());
        response.setResponseCode("00");
        response.setResponseMessage("Payment status request processed successfully");
        response.setStatus("PENDING");
        response.setStatusRequestId(request.getStatusRequestId());
        response.setOriginalTransactionId(request.getOriginalTransactionId());
        response.setProcessedAt(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
