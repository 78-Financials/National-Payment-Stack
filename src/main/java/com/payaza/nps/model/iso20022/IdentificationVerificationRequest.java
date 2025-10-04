package com.payaza.nps.model.iso20022;

import jakarta.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISO 20022 acmt.023 - Identification Verification Request Message
 * Used for verifying beneficiary account details before payment processing
 */
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:acmt.023.001.02")
@XmlAccessorType(XmlAccessType.FIELD)
public class IdentificationVerificationRequest {

    @XmlElement(name = "IdVrfctnReq", required = true)
    private IdentificationVerificationRequestV02 idVrfctnReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IdentificationVerificationRequestV02 {
        
        @XmlElement(name = "MsgHdr", required = true)
        private MessageHeader msgHdr;
        
        @XmlElement(name = "IdVrfctn", required = true)
        private IdentificationVerification2 idVrfctn;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public MessageHeader getMsgHdr() { return msgHdr; }
        public void setMsgHdr(MessageHeader msgHdr) { this.msgHdr = msgHdr; }
        
        public IdentificationVerification2 getIdVrfctn() { return idVrfctn; }
        public void setIdVrfctn(IdentificationVerification2 idVrfctn) { this.idVrfctn = idVrfctn; }
        
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
    public static class IdentificationVerification2 {
        
        @XmlElement(name = "Assgnmt", required = true)
        private CaseAssignment5 assgnmt;
        
        @XmlElement(name = "Case", required = true)
        private Case5 caseInfo;
        
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

    // Supporting classes for ISO 20022 structure
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CaseAssignment5 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "Assgnr", required = true)
        private PartyIdentification135 assgnr;
        
        @XmlElement(name = "Assgne", required = true)
        private PartyIdentification135 assgne;
        
        @XmlElement(name = "CreDtTm", required = true)
        private String creDtTm;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public PartyIdentification135 getAssgnr() { return assgnr; }
        public void setAssgnr(PartyIdentification135 assgnr) { this.assgnr = assgnr; }
        
        public PartyIdentification135 getAssgne() { return assgne; }
        public void setAssgne(PartyIdentification135 assgne) { this.assgne = assgne; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Case5 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "Cretr", required = true)
        private PartyIdentification135 cretr;
        
        @XmlElement(name = "ReopCaseIndctr")
        private Boolean reopCaseIndctr;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public PartyIdentification135 getCretr() { return cretr; }
        public void setCretr(PartyIdentification135 cretr) { this.cretr = cretr; }
        
        public Boolean getReopCaseIndctr() { return reopCaseIndctr; }
        public void setReopCaseIndctr(Boolean reopCaseIndctr) { this.reopCaseIndctr = reopCaseIndctr; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AccountIdentification4Choice {
        @XmlElement(name = "IBAN")
        private String iban;
        
        @XmlElement(name = "Othr")
        private GenericAccountIdentification1 othr;

        // Getters and Setters
        public String getIban() { return iban; }
        public void setIban(String iban) { this.iban = iban; }
        
        public GenericAccountIdentification1 getOthr() { return othr; }
        public void setOthr(GenericAccountIdentification1 othr) { this.othr = othr; }
    }

    // Additional supporting classes would be defined here...
    // For brevity, I'll include key ones and reference others

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PartyIdentification125 {
        @XmlElement(name = "Nm")
        private String nm;
        
        @XmlElement(name = "Id")
        private PartyIdentification135 id;

        // Getters and Setters
        public String getNm() { return nm; }
        public void setNm(String nm) { this.nm = nm; }
        
        public PartyIdentification135 getId() { return id; }
        public void setId(PartyIdentification135 id) { this.id = id; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PartyIdentification135 {
        @XmlElement(name = "Nm")
        private String nm;
        
        @XmlElement(name = "PstlAdr")
        private PostalAddress24 pstlAdr;
        
        @XmlElement(name = "Id")
        private PartyIdentification135 id;

        // Getters and Setters
        public String getNm() { return nm; }
        public void setNm(String nm) { this.nm = nm; }
        
        public PostalAddress24 getPstlAdr() { return pstlAdr; }
        public void setPstlAdr(PostalAddress24 pstlAdr) { this.pstlAdr = pstlAdr; }
        
        public PartyIdentification135 getId() { return id; }
        public void setId(PartyIdentification135 id) { this.id = id; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GenericAccountIdentification1 {
        @XmlElement(name = "Id", required = true)
        private String id;
        
        @XmlElement(name = "SchmeNm")
        private AccountSchemeName1Choice schmeNm;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public AccountSchemeName1Choice getSchmeNm() { return schmeNm; }
        public void setSchmeNm(AccountSchemeName1Choice schmeNm) { this.schmeNm = schmeNm; }
    }

    // Placeholder classes for other required elements
    public static class BranchAndFinancialInstitutionIdentification6 {}
    public static class OriginalTransactionReference32 {}
    public static class PostalAddress24 {}
    public static class AccountSchemeName1Choice {}
    public static class SupplementaryData1 {}

    // Getters and Setters for main class
    public IdentificationVerificationRequestV02 getIdVrfctnReq() { return idVrfctnReq; }
    public void setIdVrfctnReq(IdentificationVerificationRequestV02 idVrfctnReq) { this.idVrfctnReq = idVrfctnReq; }
}
