package com.payaza.nps.service;

import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.model.iso20022.PaymentRequest;
import com.payaza.nps.model.iso20022.PaymentStatusReport;
import com.payaza.nps.model.iso20022.PaymentStatusRequest;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling NPS Payment operations
 * Implements pacs.008 (Payment Request), pacs.002 (Payment Status Report), and pacs.028 (Payment Status Request)
 */
@Service
public class NpsPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(NpsPaymentService.class);
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private WebClient npsWebClient;

    @Autowired
    private NpsConfiguration npsConfig;

    @Autowired
    private NpsEncryptionService encryptionService;

    /**
     * Step 2: Originator Bank generates payment (pacs.008) and delivers to NPS
     * This handles Approved Payment, Declined Payment, and Timeout Payment flows
     */
    public String submitPaymentRequest(PaymentRequest request) {
        logger.info("Submitting payment request to NPS: {}", 
                   request.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId());

        try {
            // Convert to XML
            String xmlPayload = convertToXml(request);
            logger.debug("Generated payment request XML payload");

            // Encrypt the payload
            String encryptedPayload = encryptionService.encryptPayload(xmlPayload);

            // Send to NPS API
            String response = npsWebClient
                    .post()
                    .uri("/api/v1/payments/submit")
                    .headers(this::addAuthHeaders)
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(encryptedPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Payment request submitted successfully to NPS: {}", 
                       request.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId());
            return response;

        } catch (WebClientResponseException e) {
            logger.error("NPS API error for payment request {}: {}", 
                        request.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to submit payment request to NPS", e);
        } catch (Exception e) {
            logger.error("Unexpected error calling NPS API for payment request {}: {}", 
                        request.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to submit payment request to NPS", e);
        }
    }

    /**
     * Step 5: Beneficiary Bank verifies and delivers payment status (pacs.002/ACSC or pacs.002/RJCT)
     * This is called by the beneficiary bank via callback
     */
    public void processPaymentStatusReport(PaymentStatusReport report) {
        logger.info("Processing payment status report from NPS: {}", 
                   report.getFiToFIPmtStsRpt().getGrpHdr().getMsgId());

        try {
            // Process each transaction status
            if (report.getFiToFIPmtStsRpt().getTxInfAndSts() != null) {
                for (PaymentStatusReport.PaymentTransaction110 tx : report.getFiToFIPmtStsRpt().getTxInfAndSts()) {
                    String status = tx.getTxSts().toString();
                    String endToEndId = tx.getOrgnlEndToEndId();
                    
                    logger.info("Payment status for transaction {}: {}", endToEndId, status);
                    
                    // Process based on status
                    switch (status) {
                        case "ACSC": // Accepted Settlement Completed
                            logger.info("Payment accepted and settled: {}", endToEndId);
                            processAcceptedPayment(tx);
                            break;
                        case "RJCT": // Rejected
                            logger.warn("Payment rejected: {} - {}", endToEndId, getRejectionReason(tx));
                            processRejectedPayment(tx);
                            break;
                        case "NAUT": // Not Authorized
                            logger.warn("Payment not authorized: {}", endToEndId);
                            processUnauthorizedPayment(tx);
                            break;
                        default:
                            logger.info("Payment status: {} for transaction: {}", status, endToEndId);
                    }
                }
            }

            logger.info("Payment status report processed successfully");

        } catch (Exception e) {
            logger.error("Error processing payment status report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process payment status report", e);
        }
    }

    /**
     * Step 1: Request payment status (pacs.028) - can be sent by either Payment Sender or Payment Receiver
     */
    public PaymentStatusReport requestPaymentStatus(PaymentStatusRequest request) {
        logger.info("Requesting payment status from NPS: {}", 
                   request.getFiToFIPmtStsReq().getGrpHdr().getMsgId());

        try {
            // Convert to XML
            String xmlPayload = convertToXml(request);
            logger.debug("Generated payment status request XML payload");

            // Encrypt the payload
            String encryptedPayload = encryptionService.encryptPayload(xmlPayload);

            // Send to NPS API
            String response = npsWebClient
                    .post()
                    .uri("/api/v1/payments/status-request")
                    .headers(this::addAuthHeaders)
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(encryptedPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Decrypt and parse response
            String decryptedResponse = encryptionService.decryptPayload(response);
            PaymentStatusReport statusReport = convertFromXml(decryptedResponse, PaymentStatusReport.class);

            logger.info("Payment status request completed successfully");
            return statusReport;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Transaction not found for payment status request: {}", 
                           request.getFiToFIPmtStsReq().getGrpHdr().getMsgId());
                throw new RuntimeException("Transaction not found", e);
            }
            logger.error("NPS API error for payment status request {}: {}", 
                        request.getFiToFIPmtStsReq().getGrpHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to request payment status from NPS", e);
        } catch (Exception e) {
            logger.error("Unexpected error calling NPS API for payment status request {}: {}", 
                        request.getFiToFIPmtStsReq().getGrpHdr().getMsgId(), e.getMessage());
            throw new RuntimeException("Failed to request payment status from NPS", e);
        }
    }

    /**
     * Create payment request (pacs.008) for credit transfer
     */
    public PaymentRequest createPaymentRequest(
            String endToEndId, String debtorAccount, String creditorAccount, 
            BigDecimal amount, String currency, String debtorName, String creditorName,
            String remittanceInfo, String debtorBankId, String creditorBankId) {
        
        logger.info("Creating payment request for end-to-end ID: {}", endToEndId);

        PaymentRequest request = new PaymentRequest();
        PaymentRequest.FIToFICstmrCdtTrf fiToFICstmrCdtTrf = new PaymentRequest.FIToFICstmrCdtTrf();

        // Set group header
        PaymentRequest.GroupHeader93 grpHdr = new PaymentRequest.GroupHeader93();
        grpHdr.setMsgId(generateMessageId());
        grpHdr.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        grpHdr.setNbOfTxs("1");

        // Set settlement amount
        PaymentRequest.ActiveOrHistoricCurrencyAndAmount sttlmAmt = 
                new PaymentRequest.ActiveOrHistoricCurrencyAndAmount();
        sttlmAmt.setCcy(currency);
        sttlmAmt.setValue(amount);
        grpHdr.setSttlmAmt(sttlmAmt);

        // Set instructing agent (debtor bank)
        PaymentRequest.BranchAndFinancialInstitutionIdentification6 instgAgt = 
                new PaymentRequest.BranchAndFinancialInstitutionIdentification6();
        grpHdr.setInstgAgt(instgAgt);

        // Set instructed agent (creditor bank)
        PaymentRequest.BranchAndFinancialInstitutionIdentification6 instdAgt = 
                new PaymentRequest.BranchAndFinancialInstitutionIdentification6();
        grpHdr.setInstdAgt(instdAgt);

        fiToFICstmrCdtTrf.setGrpHdr(grpHdr);

        // Set credit transfer transaction information
        List<PaymentRequest.CreditTransferTransaction34> cdtTrfTxInf = new ArrayList<>();
        PaymentRequest.CreditTransferTransaction34 txInfo = new PaymentRequest.CreditTransferTransaction34();

        // Set payment identification
        PaymentRequest.PaymentIdentification6 pmtId = new PaymentRequest.PaymentIdentification6();
        pmtId.setEndToEndId(endToEndId);
        pmtId.setInstrId(generateInstructionId());
        pmtId.setTxId(generateTransactionId());
        txInfo.setPmtId(pmtId);

        // Set interbank settlement amount
        PaymentRequest.ActiveOrHistoricCurrencyAndAmount intrBkSttlmAmt = 
                new PaymentRequest.ActiveOrHistoricCurrencyAndAmount();
        intrBkSttlmAmt.setCcy(currency);
        intrBkSttlmAmt.setValue(amount);
        txInfo.setIntrBkSttlmAmt(intrBkSttlmAmt);

        // Set debtor information
        PaymentRequest.PartyIdentification135 dbtr = new PaymentRequest.PartyIdentification135();
        dbtr.setNm(debtorName);
        txInfo.setDbtr(dbtr);

        // Set creditor information
        PaymentRequest.PartyIdentification135 cdtr = new PaymentRequest.PartyIdentification135();
        cdtr.setNm(creditorName);
        txInfo.setCdtr(cdtr);

        // Set remittance information
        PaymentRequest.RemittanceInformation16 rmtInf = new PaymentRequest.RemittanceInformation16();
        List<String> ustrd = new ArrayList<>();
        ustrd.add(remittanceInfo);
        rmtInf.setUstrd(ustrd);
        txInfo.setRmtInf(rmtInf);

        cdtTrfTxInf.add(txInfo);
        fiToFICstmrCdtTrf.setCdtTrfTxInf(cdtTrfTxInf);

        request.setFiToFICstmrCdtTrf(fiToFICstmrCdtTrf);

        logger.info("Created payment request: {}", grpHdr.getMsgId());
        return request;
    }

    /**
     * Create payment status request (pacs.028)
     */
    public PaymentStatusRequest createPaymentStatusRequest(String originalMessageId, String originalEndToEndId) {
        logger.info("Creating payment status request for original message: {}", originalMessageId);

        PaymentStatusRequest request = new PaymentStatusRequest();
        PaymentStatusRequest.FIToFIPmtStsReq fiToFIPmtStsReq = new PaymentStatusRequest.FIToFIPmtStsReq();

        // Set group header
        PaymentStatusRequest.GroupHeader53 grpHdr = new PaymentStatusRequest.GroupHeader53();
        grpHdr.setMsgId(generateMessageId());
        grpHdr.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        fiToFIPmtStsReq.setGrpHdr(grpHdr);

        // Set original group information
        List<PaymentStatusRequest.OriginalGroupInformation27> orgnlGrpInf = new ArrayList<>();
        PaymentStatusRequest.OriginalGroupInformation27 origGrp = new PaymentStatusRequest.OriginalGroupInformation27();
        origGrp.setOrgnlMsgId(originalMessageId);
        origGrp.setOrgnlMsgNmId("pacs.008.001.08");
        origGrp.setOrgnlCreDtTm(LocalDateTime.now().minusHours(1).format(ISO_DATE_TIME));
        orgnlGrpInf.add(origGrp);
        fiToFIPmtStsReq.setOrgnlGrpInf(orgnlGrpInf);

        // Set transaction information
        List<PaymentStatusRequest.PaymentTransaction106> txInf = new ArrayList<>();
        PaymentStatusRequest.PaymentTransaction106 txInfo = new PaymentStatusRequest.PaymentTransaction106();
        txInfo.setStsReqId(generateStatusRequestId());
        txInfo.setOrgnlEndToEndId(originalEndToEndId);
        txInfo.setOrgnlInstrId(generateInstructionId());
        txInfo.setOrgnlTxId(generateTransactionId());
        txInf.add(txInfo);
        fiToFIPmtStsReq.setTxInf(txInf);

        request.setFiToFIPmtStsReq(fiToFIPmtStsReq);

        logger.info("Created payment status request: {}", grpHdr.getMsgId());
        return request;
    }

    /**
     * Process accepted payment
     */
    private void processAcceptedPayment(PaymentStatusReport.PaymentTransaction110 tx) {
        // Implement business logic for accepted payments
        logger.info("Processing accepted payment: {}", tx.getOrgnlEndToEndId());
        // Update payment status in database, send notifications, etc.
    }

    /**
     * Process rejected payment
     */
    private void processRejectedPayment(PaymentStatusReport.PaymentTransaction110 tx) {
        // Implement business logic for rejected payments
        logger.info("Processing rejected payment: {}", tx.getOrgnlEndToEndId());
        // Update payment status in database, send notifications, etc.
    }

    /**
     * Process unauthorized payment
     */
    private void processUnauthorizedPayment(PaymentStatusReport.PaymentTransaction110 tx) {
        // Implement business logic for unauthorized payments
        logger.info("Processing unauthorized payment: {}", tx.getOrgnlEndToEndId());
        // Update payment status in database, send notifications, etc.
    }

    /**
     * Get rejection reason from status report
     */
    private String getRejectionReason(PaymentStatusReport.PaymentTransaction110 tx) {
        if (tx.getStsRsnInf() != null && !tx.getStsRsnInf().isEmpty()) {
            PaymentStatusReport.StatusReasonInformation12 reason = tx.getStsRsnInf().get(0);
            if (reason.getRsn() != null) {
                if (reason.getRsn().getCd() != null) {
                    return reason.getRsn().getCd().toString();
                } else if (reason.getRsn().getPrtry() != null) {
                    return reason.getRsn().getPrtry();
                }
            }
            if (reason.getAddtlInf() != null && !reason.getAddtlInf().isEmpty()) {
                return reason.getAddtlInf().get(0);
            }
        }
        return "Unknown reason";
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
     * Generate unique instruction ID
     */
    private String generateInstructionId() {
        return "INSTR" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Generate unique status request ID
     */
    private String generateStatusRequestId() {
        return "STATREQ" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
