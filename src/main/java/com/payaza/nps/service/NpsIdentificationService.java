package com.payaza.nps.service;

import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.model.iso20022.IdentificationVerificationRequest;
import com.payaza.nps.model.iso20022.IdentificationVerificationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for handling NPS Identification Verification requests (acmt.023) and reports (acmt.024)
 * This implements the proper NPS flow as described in the documentation
 */
@Service
public class NpsIdentificationService {

    private static final Logger logger = LoggerFactory.getLogger(NpsIdentificationService.class);
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private WebClient npsWebClient;

    @Autowired
    private NpsConfiguration npsConfig;

    @Autowired
    private NpsEncryptionService encryptionService;

    /**
     * Step 2: Originator Bank generates ID verification request (acmt.023) and delivers to NPS
     */
    public String sendIdentificationVerificationRequest(IdentificationVerificationRequest request) {
        logger.info("Sending identification verification request to NPS: {}", 
                   request.getIdVrfctnReq().getMsgHdr().getMsgId());

        try {
            // Convert to XML
            String xmlPayload = convertToXml(request);
            logger.debug("Generated XML payload: {}", xmlPayload);

            // Encrypt the payload
            String encryptedPayload = encryptionService.encryptPayload(xmlPayload);

            // Send to NPS API
            String response = npsWebClient
                    .post()
                    .uri("/api/v1/identification/verify")
                    .headers(this::addAuthHeaders)
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(encryptedPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Identification verification request submitted successfully to NPS");
            return response;

        } catch (WebClientResponseException e) {
            logger.error("NPS API error for identification request {}: {}", 
                        request.getIdVrfctnReq().getMsgHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to submit identification verification request to NPS", e);
        } catch (Exception e) {
            logger.error("Unexpected error calling NPS API for identification request {}: {}", 
                        request.getIdVrfctnReq().getMsgHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to submit identification verification request to NPS", e);
        }
    }

    /**
     * Step 5: Beneficiary Bank verifies and delivers ID verification report (acmt.024)
     * This is called by the beneficiary bank via callback
     */
    public void processIdentificationVerificationReport(IdentificationVerificationReport report) {
        logger.info("Processing identification verification report from NPS: {}", 
                   report.getIdVrfctnRpt().getMsgHdr().getMsgId());

        try {
            // Log the verification status
            if (report.getIdVrfctnRpt().getRptOrErr().getRpt() != null) {
                String status = report.getIdVrfctnRpt().getRptOrErr().getRpt().getVrfctnSts().getSts();
                String reason = report.getIdVrfctnRpt().getRptOrErr().getRpt().getVrfctnSts().getRsn();
                
                logger.info("Identification verification status: {} - {}", status, reason);
                
                // Process based on status
                switch (status) {
                    case "VERIFIED":
                        logger.info("Account verification successful");
                        break;
                    case "NOT_VERIFIED":
                        logger.warn("Account verification failed: {}", reason);
                        break;
                    case "ERROR":
                        logger.error("Account verification error: {}", reason);
                        break;
                    default:
                        logger.warn("Unknown verification status: {}", status);
                }
            } else if (report.getIdVrfctnRpt().getRptOrErr().getErr() != null) {
                logger.error("Identification verification error: {} - {}", 
                           report.getIdVrfctnRpt().getRptOrErr().getErr().getErr().getCd(),
                           report.getIdVrfctnRpt().getRptOrErr().getErr().getDesc());
            }

        } catch (Exception e) {
            logger.error("Error processing identification verification report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process identification verification report", e);
        }
    }

    /**
     * Create identification verification request for account verification
     */
    public IdentificationVerificationRequest createIdentificationVerificationRequest(
            String accountId, String accountOwnerName, String beneficiaryBankId, 
            String originalTransactionRef) {
        
        logger.info("Creating identification verification request for account: {}", accountId);

        IdentificationVerificationRequest request = new IdentificationVerificationRequest();
        IdentificationVerificationRequest.IdentificationVerificationRequestV02 requestV02 = 
                new IdentificationVerificationRequest.IdentificationVerificationRequestV02();

        // Set message header
        IdentificationVerificationRequest.MessageHeader msgHdr = 
                new IdentificationVerificationRequest.MessageHeader();
        msgHdr.setMsgId(generateMessageId());
        msgHdr.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        msgHdr.setMsgDefIdr("acmt.023.001.02");

        // Set message sender (originator bank)
        IdentificationVerificationRequest.PartyIdentification125 msgSndr = 
                new IdentificationVerificationRequest.PartyIdentification125();
        msgSndr.setNm(npsConfig.getMerchantId()); // Using merchant ID as bank identifier
        msgHdr.setMsgSndr(msgSndr);

        // Set message recipient (NPS)
        IdentificationVerificationRequest.PartyIdentification125 msgRcpt = 
                new IdentificationVerificationRequest.PartyIdentification125();
        msgRcpt.setNm("NPS"); // NPS system identifier
        msgHdr.setMsgRcpt(msgRcpt);

        requestV02.setMsgHdr(msgHdr);

        // Set identification verification details
        IdentificationVerificationRequest.IdentificationVerification2 idVrfctn = 
                new IdentificationVerificationRequest.IdentificationVerification2();

        // Set case assignment
        IdentificationVerificationRequest.CaseAssignment5 assgnmt = 
                new IdentificationVerificationRequest.CaseAssignment5();
        assgnmt.setId(generateCaseId());
        assgnmt.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        
        // Set assigner (originator bank)
        IdentificationVerificationRequest.PartyIdentification135 assgnr = 
                new IdentificationVerificationRequest.PartyIdentification135();
        assgnr.setNm(npsConfig.getMerchantId());
        assgnmt.setAssgnr(assgnr);
        
        // Set assignee (NPS)
        IdentificationVerificationRequest.PartyIdentification135 assgne = 
                new IdentificationVerificationRequest.PartyIdentification135();
        assgne.setNm("NPS");
        assgnmt.setAssgne(assgne);
        
        idVrfctn.setAssgnmt(assgnmt);

        // Set case information
        IdentificationVerificationRequest.Case5 caseInfo = 
                new IdentificationVerificationRequest.Case5();
        caseInfo.setId(generateCaseId());
        
        IdentificationVerificationRequest.PartyIdentification135 cretr = 
                new IdentificationVerificationRequest.PartyIdentification135();
        cretr.setNm(npsConfig.getMerchantId());
        caseInfo.setCretr(cretr);
        caseInfo.setReopCaseIndctr(false);
        
        idVrfctn.setCase(caseInfo);

        // Set account identification
        IdentificationVerificationRequest.AccountIdentification4Choice acctId = 
                new IdentificationVerificationRequest.AccountIdentification4Choice();
        acctId.setOthr(new IdentificationVerificationRequest.GenericAccountIdentification1());
        acctId.getOthr().setId(accountId);
        idVrfctn.setAcctId(acctId);

        // Set account owner
        if (accountOwnerName != null) {
            IdentificationVerificationRequest.PartyIdentification135 acctOwnr = 
                    new IdentificationVerificationRequest.PartyIdentification135();
            acctOwnr.setNm(accountOwnerName);
            idVrfctn.setAcctOwnr(acctOwnr);
        }

        // Set additional information
        idVrfctn.setAddtlInf("Account verification request for payment processing");

        requestV02.setIdVrfctn(idVrfctn);
        request.setIdVrfctnReq(requestV02);

        logger.info("Created identification verification request: {}", msgHdr.getMsgId());
        return request;
    }

    /**
     * Add authentication headers for NPS API
     */
    private void addAuthHeaders(HttpHeaders headers) {
        headers.add("X-Client-Id", npsConfig.getClientId());
        headers.add("X-Client-Secret", npsConfig.getClientSecret());
        headers.add("X-Merchant-Id", npsConfig.getMerchantId());
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        headers.add("X-Timestamp", timestamp);
        
        if (npsConfig.getEnableEncryption()) {
            String signature = encryptionService.generateSignature(npsConfig.getClientId(), timestamp);
            headers.add("X-Signature", signature);
        }
    }

    /**
     * Convert object to XML string
     */
    private String convertToXml(Object object) throws Exception {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    /**
     * Convert XML string to object
     */
    public <T> T convertFromXml(String xml, Class<T> clazz) throws Exception {
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        StringReader reader = new StringReader(xml);
        return clazz.cast(unmarshaller.unmarshal(reader));
    }

    /**
     * Generate unique message ID
     */
    private String generateMessageId() {
        return "MSG" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Generate unique case ID
     */
    private String generateCaseId() {
        return "CASE" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Test encryption modes
     */
    public void testEncryptionModes() {
        encryptionService.testEncryptionModes();
    }
}
