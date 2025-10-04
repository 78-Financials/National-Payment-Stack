package com.payaza.nps.service;

import com.payaza.nps.dto.*;
import com.payaza.nps.model.iso20022.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for converting between JSON DTOs and ISO 20022 XML messages
 */
@Service
public class NpsMessageConverter {

    private static final Logger logger = LoggerFactory.getLogger(NpsMessageConverter.class);
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convert JSON payment request to ISO 20022 pacs.008
     */
    public PaymentRequest convertToPaymentRequest(PaymentRequestJsonDto jsonDto) {
        logger.debug("Converting JSON payment request to ISO 20022 pacs.008");

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
        sttlmAmt.setCcy(jsonDto.getCurrency());
        sttlmAmt.setValue(jsonDto.getAmount());
        grpHdr.setSttlmAmt(sttlmAmt);

        fiToFICstmrCdtTrf.setGrpHdr(grpHdr);

        // Set credit transfer transaction information
        List<PaymentRequest.CreditTransferTransaction34> cdtTrfTxInf = new ArrayList<>();
        PaymentRequest.CreditTransferTransaction34 txInfo = new PaymentRequest.CreditTransferTransaction34();

        // Set payment identification
        PaymentRequest.PaymentIdentification6 pmtId = new PaymentRequest.PaymentIdentification6();
        pmtId.setEndToEndId(jsonDto.getEndToEndId());
        pmtId.setInstrId(jsonDto.getInstructionId() != null ? jsonDto.getInstructionId() : generateInstructionId());
        pmtId.setTxId(jsonDto.getTransactionId() != null ? jsonDto.getTransactionId() : generateTransactionId());
        txInfo.setPmtId(pmtId);

        // Set interbank settlement amount
        PaymentRequest.ActiveOrHistoricCurrencyAndAmount intrBkSttlmAmt = 
                new PaymentRequest.ActiveOrHistoricCurrencyAndAmount();
        intrBkSttlmAmt.setCcy(jsonDto.getCurrency());
        intrBkSttlmAmt.setValue(jsonDto.getAmount());
        txInfo.setIntrBkSttlmAmt(intrBkSttlmAmt);

        // Set debtor information
        PaymentRequest.PartyIdentification135 dbtr = new PaymentRequest.PartyIdentification135();
        if (jsonDto.getDebtorName() != null) {
            dbtr.setNm(jsonDto.getDebtorName());
        }
        txInfo.setDbtr(dbtr);

        // Set creditor information
        PaymentRequest.PartyIdentification135 cdtr = new PaymentRequest.PartyIdentification135();
        if (jsonDto.getCreditorName() != null) {
            cdtr.setNm(jsonDto.getCreditorName());
        }
        txInfo.setCdtr(cdtr);

        // Set remittance information
        if (jsonDto.getRemittanceInfo() != null || jsonDto.getUnstructuredRemittanceInfo() != null) {
            PaymentRequest.RemittanceInformation16 rmtInf = new PaymentRequest.RemittanceInformation16();
            List<String> ustrd = new ArrayList<>();
            
            if (jsonDto.getRemittanceInfo() != null) {
                ustrd.add(jsonDto.getRemittanceInfo());
            }
            if (jsonDto.getUnstructuredRemittanceInfo() != null) {
                ustrd.addAll(jsonDto.getUnstructuredRemittanceInfo());
            }
            
            rmtInf.setUstrd(ustrd);
            txInfo.setRmtInf(rmtInf);
        }

        cdtTrfTxInf.add(txInfo);
        fiToFICstmrCdtTrf.setCdtTrfTxInf(cdtTrfTxInf);

        request.setFiToFICstmrCdtTrf(fiToFICstmrCdtTrf);

        logger.debug("Successfully converted JSON payment request to ISO 20022 pacs.008");
        return request;
    }

    /**
     * Convert JSON payment status request to ISO 20022 pacs.028
     */
    public PaymentStatusRequest convertToPaymentStatusRequest(PaymentStatusRequestJsonDto jsonDto) {
        logger.debug("Converting JSON payment status request to ISO 20022 pacs.028");

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
        origGrp.setOrgnlMsgId(jsonDto.getOriginalMessageId());
        origGrp.setOrgnlMsgNmId("pacs.008.001.08");
        orgnlGrpInf.add(origGrp);
        fiToFIPmtStsReq.setOrgnlGrpInf(orgnlGrpInf);

        // Set transaction information
        List<PaymentStatusRequest.PaymentTransaction106> txInf = new ArrayList<>();
        PaymentStatusRequest.PaymentTransaction106 txInfo = new PaymentStatusRequest.PaymentTransaction106();
        txInfo.setStsReqId(generateStatusRequestId());
        txInfo.setOrgnlEndToEndId(jsonDto.getOriginalEndToEndId());
        
        if (jsonDto.getOriginalInstructionId() != null) {
            txInfo.setOrgnlInstrId(jsonDto.getOriginalInstructionId());
        }
        if (jsonDto.getOriginalTransactionId() != null) {
            txInfo.setOrgnlTxId(jsonDto.getOriginalTransactionId());
        }
        if (jsonDto.getOriginalUETR() != null) {
            txInfo.setOrgnlUETR(jsonDto.getOriginalUETR());
        }
        if (jsonDto.getOriginalClearSystemReference() != null) {
            txInfo.setOrgnlClrSysRef(jsonDto.getOriginalClearSystemReference());
        }
        
        txInf.add(txInfo);
        fiToFIPmtStsReq.setTxInf(txInf);

        request.setFiToFIPmtStsReq(fiToFIPmtStsReq);

        logger.debug("Successfully converted JSON payment status request to ISO 20022 pacs.028");
        return request;
    }

    /**
     * Convert JSON identification verification request to ISO 20022 acmt.023
     */
    public IdentificationVerificationRequest convertToIdentificationVerificationRequest(IdentificationVerificationRequestJsonDto jsonDto) {
        logger.debug("Converting JSON identification verification request to ISO 20022 acmt.023");

        IdentificationVerificationRequest request = new IdentificationVerificationRequest();
        IdentificationVerificationRequest.IdentificationVerificationRequestV02 requestV02 = 
                new IdentificationVerificationRequest.IdentificationVerificationRequestV02();

        // Set message header
        IdentificationVerificationRequest.MessageHeader msgHdr = 
                new IdentificationVerificationRequest.MessageHeader();
        msgHdr.setMsgId(generateMessageId());
        msgHdr.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        msgHdr.setMsgDefIdr("acmt.023.001.02");

        requestV02.setMsgHdr(msgHdr);

        // Set identification verification details
        IdentificationVerificationRequest.IdentificationVerification2 idVrfctn = 
                new IdentificationVerificationRequest.IdentificationVerification2();

        // Set case assignment
        IdentificationVerificationRequest.CaseAssignment5 assgnmt = 
                new IdentificationVerificationRequest.CaseAssignment5();
        assgnmt.setId(generateCaseId());
        assgnmt.setCreDtTm(LocalDateTime.now().format(ISO_DATE_TIME));
        idVrfctn.setAssgnmt(assgnmt);

        // Set case information
        IdentificationVerificationRequest.Case5 caseInfo = 
                new IdentificationVerificationRequest.Case5();
        caseInfo.setId(generateCaseId());
        caseInfo.setReopCaseIndctr(false);
        idVrfctn.setCase(caseInfo);

        // Set account identification
        IdentificationVerificationRequest.AccountIdentification4Choice acctId = 
                new IdentificationVerificationRequest.AccountIdentification4Choice();
        acctId.setOthr(new IdentificationVerificationRequest.GenericAccountIdentification1());
        acctId.getOthr().setId(jsonDto.getAccountId());
        idVrfctn.setAcctId(acctId);

        // Set account owner
        if (jsonDto.getAccountOwnerName() != null) {
            IdentificationVerificationRequest.PartyIdentification135 acctOwnr = 
                    new IdentificationVerificationRequest.PartyIdentification135();
            acctOwnr.setNm(jsonDto.getAccountOwnerName());
            idVrfctn.setAcctOwnr(acctOwnr);
        }

        // Set additional information
        if (jsonDto.getAdditionalInformation() != null) {
            idVrfctn.setAddtlInf(jsonDto.getAdditionalInformation());
        }

        requestV02.setIdVrfctn(idVrfctn);
        request.setIdVrfctnReq(requestV02);

        logger.debug("Successfully converted JSON identification verification request to ISO 20022 acmt.023");
        return request;
    }

    /**
     * Convert ISO 20022 pacs.002 to JSON payment response
     */
    public PaymentResponseJsonDto convertFromPaymentStatusReport(PaymentStatusReport statusReport) {
        logger.debug("Converting ISO 20022 pacs.002 to JSON payment response");

        PaymentResponseJsonDto response = new PaymentResponseJsonDto();

        if (statusReport.getFiToFIPmtStsRpt() != null && 
            statusReport.getFiToFIPmtStsRpt().getTxInfAndSts() != null && 
            !statusReport.getFiToFIPmtStsRpt().getTxInfAndSts().isEmpty()) {
            
            PaymentStatusReport.PaymentTransaction110 tx = statusReport.getFiToFIPmtStsRpt().getTxInfAndSts().get(0);
            
            response.setMessageId(statusReport.getFiToFIPmtStsRpt().getGrpHdr().getMsgId());
            response.setEndToEndId(tx.getOrgnlEndToEndId());
            response.setInstructionId(tx.getOrgnlInstrId());
            response.setTransactionId(tx.getOrgnlTxId());
            response.setStatus(tx.getTxSts().toString());
            response.setNpsReference(tx.getAcctSvcrRef());
            response.setClearingSystemReference(tx.getClrSysRef());
            response.setProcessedAt(LocalDateTime.now());
            
            // Set status reason if available
            if (tx.getStsRsnInf() != null && !tx.getStsRsnInf().isEmpty()) {
                PaymentStatusReport.StatusReasonInformation12 reason = tx.getStsRsnInf().get(0);
                if (reason.getRsn() != null) {
                    if (reason.getRsn().getCd() != null) {
                        response.setStatusCode(reason.getRsn().getCd().toString());
                    } else if (reason.getRsn().getPrtry() != null) {
                        response.setStatusCode(reason.getRsn().getPrtry());
                    }
                }
                if (reason.getAddtlInf() != null && !reason.getAddtlInf().isEmpty()) {
                    response.setStatusReason(reason.getAddtlInf().get(0));
                }
            }
        }

        logger.debug("Successfully converted ISO 20022 pacs.002 to JSON payment response");
        return response;
    }

    /**
     * Convert ISO 20022 acmt.024 to JSON identification verification response
     */
    public IdentificationVerificationResponseJsonDto convertFromIdentificationVerificationReport(IdentificationVerificationReport report) {
        logger.debug("Converting ISO 20022 acmt.024 to JSON identification verification response");

        IdentificationVerificationResponseJsonDto response = new IdentificationVerificationResponseJsonDto();

        if (report.getIdVrfctnRpt() != null) {
            response.setMessageId(report.getIdVrfctnRpt().getMsgHdr().getMsgId());
            response.setProcessedAt(LocalDateTime.now());

            if (report.getIdVrfctnRpt().getRptOrErr().getRpt() != null) {
                IdentificationVerificationReport.IdentificationVerificationReport3 rpt = 
                        report.getIdVrfctnRpt().getRptOrErr().getRpt();
                
                response.setAccountId(rpt.getAcctId().getOthr().getId());
                response.setVerificationStatus(rpt.getVrfctnSts().getSts());
                response.setVerificationReason(rpt.getVrfctnSts().getRsn());
                response.setAdditionalInformation(rpt.getAddtlInf());
                
                if (rpt.getAcctOwnr() != null) {
                    // Extract account owner name if needed
                }
            } else if (report.getIdVrfctnRpt().getRptOrErr().getErr() != null) {
                response.setVerificationStatus("ERROR");
                response.setVerificationReason(report.getIdVrfctnRpt().getRptOrErr().getErr().getDesc());
            }
        }

        logger.debug("Successfully converted ISO 20022 acmt.024 to JSON identification verification response");
        return response;
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

    /**
     * Generate unique case ID
     */
    private String generateCaseId() {
        return "CASE" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
