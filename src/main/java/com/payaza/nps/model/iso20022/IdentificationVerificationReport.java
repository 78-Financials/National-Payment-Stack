package com.payaza.nps.model.iso20022;

import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * ISO 20022 acmt.024 - Identification Verification Report Message
 * Response to identification verification request
 */
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:acmt.024.001.02")
@XmlAccessorType(XmlAccessType.FIELD)
public class IdentificationVerificationReport {

    @XmlElement(name = "IdVrfctnRpt", required = true)
    private IdentificationVerificationReportV02 idVrfctnRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IdentificationVerificationReportV02 {
        
        @XmlElement(name = "MsgHdr", required = true)
        private MessageHeader msgHdr;
        
        @XmlElement(name = "RptOrErr", required = true)
        private ReportOrError1Choice rptOrErr;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public MessageHeader getMsgHdr() { return msgHdr; }
        public void setMsgHdr(MessageHeader msgHdr) { this.msgHdr = msgHdr; }
        
        public ReportOrError1Choice getRptOrErr() { return rptOrErr; }
        public void setRptOrErr(ReportOrError1Choice rptOrErr) { this.rptOrErr = rptOrErr; }
        
        public List<SupplementaryData1> getSplmtryData() { return splmtryData; }
        public void setSplmtryData(List<SupplementaryData1> splmtryData) { this.splmtryData = splmtryData; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MessageHeader {
        
        @XmlElement(name = "MsgId", required = true)
        private String msgId;
        
        @XmlElement(name = "CreDtTm", required = true)
        private String creDtTm;
        
        @XmlElement(name = "MsgRcpt")
        private PartyIdentification125 msgRcpt;
        
        @XmlElement(name = "MsgSndr")
        private PartyIdentification125 msgSndr;
        
        @XmlElement(name = "MsgDefIdr")
        private String msgDefIdr;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
        
        public PartyIdentification125 getMsgRcpt() { return msgRcpt; }
        public void setMsgRcpt(PartyIdentification125 msgRcpt) { this.msgRcpt = msgRcpt; }
        
        public PartyIdentification125 getMsgSndr() { return msgSndr; }
        public void setMsgSndr(PartyIdentification125 msgSndr) { this.msgSndr = msgSndr; }
        
        public String getMsgDefIdr() { return msgDefIdr; }
        public void setMsgDefIdr(String msgDefIdr) { this.msgDefIdr = msgDefIdr; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReportOrError1Choice {
        
        @XmlElement(name = "Rpt")
        private IdentificationVerificationReport3 rpt;
        
        @XmlElement(name = "Err")
        private ErrorHandling3 err;

        // Getters and Setters
        public IdentificationVerificationReport3 getRpt() { return rpt; }
        public void setRpt(IdentificationVerificationReport3 rpt) { this.rpt = rpt; }
        
        public ErrorHandling3 getErr() { return err; }
        public void setErr(ErrorHandling3 err) { this.err = err; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IdentificationVerificationReport3 {
        
        @XmlElement(name = "Assgnmt", required = true)
        private CaseAssignment5 assgnmt;
        
        @XmlElement(name = "Case", required = true)
        private Case5 caseInfo;
        
        @XmlElement(name = "VrfctnSts", required = true)
        private VerificationStatus1 vrfctnSts;
        
        @XmlElement(name = "AcctId", required = true)
        private AccountIdentification4Choice acctId;
        
        @XmlElement(name = "AcctOwnr")
        private PartyIdentification135 acctOwnr;
        
        @XmlElement(name = "AcctSvcr")
        private BranchAndFinancialInstitutionIdentification6 acctSvcr;
        
        @XmlElement(name = "OrgnlTxRef")
        private OriginalTransactionReference32 orgnlTxRef;
        
        @XmlElement(name = "AddtlInf")
        private String addtlInf;

        // Getters and Setters
        public CaseAssignment5 getAssgnmt() { return assgnmt; }
        public void setAssgnmt(CaseAssignment5 assgnmt) { this.assgnmt = assgnmt; }
        
        public Case5 getCase() { return caseInfo; }
        public void setCase(Case5 caseInfo) { this.caseInfo = caseInfo; }
        
        public VerificationStatus1 getVrfctnSts() { return vrfctnSts; }
        public void setVrfctnSts(VerificationStatus1 vrfctnSts) { this.vrfctnSts = vrfctnSts; }
        
        public AccountIdentification4Choice getAcctId() { return acctId; }
        public void setAcctId(AccountIdentification4Choice acctId) { this.acctId = acctId; }
        
        public PartyIdentification135 getAcctOwnr() { return acctOwnr; }
        public void setAcctOwnr(PartyIdentification135 acctOwnr) { this.acctOwnr = acctOwnr; }
        
        public BranchAndFinancialInstitutionIdentification6 getAcctSvcr() { return acctSvcr; }
        public void setAcctSvcr(BranchAndFinancialInstitutionIdentification6 acctSvcr) { this.acctSvcr = acctSvcr; }
        
        public OriginalTransactionReference32 getOrgnlTxRef() { return orgnlTxRef; }
        public void setOrgnlTxRef(OriginalTransactionReference32 orgnlTxRef) { this.orgnlTxRef = orgnlTxRef; }
        
        public String getAddtlInf() { return addtlInf; }
        public void setAddtlInf(String addtlInf) { this.addtlInf = addtlInf; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class VerificationStatus1 {
        
        @XmlElement(name = "Sts", required = true)
        private String sts;
        
        @XmlElement(name = "Rsn")
        private String rsn;

        // Getters and Setters
        public String getSts() { return sts; }
        public void setSts(String sts) { this.sts = sts; }
        
        public String getRsn() { return rsn; }
        public void setRsn(String rsn) { this.rsn = rsn; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ErrorHandling3 {
        
        @XmlElement(name = "Err", required = true)
        private ErrorHandling5 err;
        
        @XmlElement(name = "Desc")
        private String desc;

        // Getters and Setters
        public ErrorHandling5 getErr() { return err; }
        public void setErr(ErrorHandling5 err) { this.err = err; }
        
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ErrorHandling5 {
        
        @XmlElement(name = "Cd", required = true)
        private String cd;
        
        @XmlElement(name = "Desc")
        private String desc;

        // Getters and Setters
        public String getCd() { return cd; }
        public void setCd(String cd) { this.cd = cd; }
        
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    // Reuse classes from IdentificationVerificationRequest
    public static class CaseAssignment5 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "Assgnr", required = true)
        private PartyIdentification135 assgnr;
        
        @XmlElement(name = "Assgne", required = true)
        private PartyIdentification135 assgne;
        
        @XmlElement(name = "CreDtTm", required = true)
        private String creDtTm;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public PartyIdentification135 getAssgnr() { return assgnr; }
        public void setAssgnr(PartyIdentification135 assgnr) { this.assgnr = assgnr; }
        
        public PartyIdentification135 getAssgne() { return assgne; }
        public void setAssgne(PartyIdentification135 assgne) { this.assgne = assgne; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
    }

    public static class Case5 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "Cretr", required = true)
        private PartyIdentification135 cretr;
        
        @XmlElement(name = "ReopCaseIndctr")
        private Boolean reopCaseIndctr;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public PartyIdentification135 getCretr() { return cretr; }
        public void setCretr(PartyIdentification135 cretr) { this.cretr = cretr; }
        
        public Boolean getReopCaseIndctr() { return reopCaseIndctr; }
        public void setReopCaseIndctr(Boolean reopCaseIndctr) { this.reopCaseIndctr = reopCaseIndctr; }
    }

    public static class AccountIdentification4Choice {
        @XmlElement(name = "IBAN")
        private String iban;
        
        @XmlElement(name = "Othr")
        private GenericAccountIdentification1 othr;

        public String getIban() { return iban; }
        public void setIban(String iban) { this.iban = iban; }
        
        public GenericAccountIdentification1 getOthr() { return othr; }
        public void setOthr(GenericAccountIdentification1 othr) { this.othr = othr; }
    }

    public static class PartyIdentification125 {
        @XmlElement(name = "Nm")
        private String nm;
        
        @XmlElement(name = "Id")
        private PartyIdentification135 id;

        public String getNm() { return nm; }
        public void setNm(String nm) { this.nm = nm; }
        
        public PartyIdentification135 getId() { return id; }
        public void setId(PartyIdentification135 id) { this.id = id; }
    }

    public static class PartyIdentification135 {
        @XmlElement(name = "Nm")
        private String nm;
        
        @XmlElement(name = "PstlAdr")
        private PostalAddress24 pstlAdr;
        
        @XmlElement(name = "Id")
        private PartyIdentification135 id;

        public String getNm() { return nm; }
        public void setNm(String nm) { this.nm = nm; }
        
        public PostalAddress24 getPstlAdr() { return pstlAdr; }
        public void setPstlAdr(PostalAddress24 pstlAdr) { this.pstlAdr = pstlAdr; }
        
        public PartyIdentification135 getId() { return id; }
        public void setId(PartyIdentification135 id) { this.id = id; }
    }

    public static class GenericAccountIdentification1 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "SchmeNm")
        private AccountSchemeName1Choice schmeNm;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public AccountSchemeName1Choice getSchmeNm() { return schmeNm; }
        public void setSchmeNm(AccountSchemeName1Choice schmeNm) { this.schmeNm = schmeNm; }
    }

    // Placeholder classes
    public static class BranchAndFinancialInstitutionIdentification6 {}
    public static class OriginalTransactionReference32 {}
    public static class PostalAddress24 {}
    public static class AccountSchemeName1Choice {}
    public static class SupplementaryData1 {}

    // Getters and Setters for main class
    public IdentificationVerificationReportV02 getIdVrfctnRpt() { return idVrfctnRpt; }
    public void setIdVrfctnRpt(IdentificationVerificationReportV02 idVrfctnRpt) { this.idVrfctnRpt = idVrfctnRpt; }
}
