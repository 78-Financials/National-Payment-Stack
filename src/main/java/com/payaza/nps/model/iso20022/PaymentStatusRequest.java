package com.payaza.nps.model.iso20022;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISO 20022 pacs.028 - Payment Status Request Message
 * Used to request the status of a previously sent payment instruction
 */
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.028.001.04")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentStatusRequest {

    @XmlElement(name = "FIToFIPmtStsReq", required = true)
    private FIToFIPmtStsReq fiToFIPmtStsReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FIToFIPmtStsReq {
        
        @XmlElement(name = "GrpHdr", required = true)
        private GroupHeader53 grpHdr;
        
        @XmlElement(name = "OrgnlGrpInf", required = true)
        private List<OriginalGroupInformation27> orgnlGrpInf;
        
        @XmlElement(name = "TxInf", required = true)
        private List<PaymentTransaction106> txInf;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public GroupHeader53 getGrpHdr() { return grpHdr; }
        public void setGrpHdr(GroupHeader53 grpHdr) { this.grpHdr = grpHdr; }
        
        public List<OriginalGroupInformation27> getOrgnlGrpInf() { return orgnlGrpInf; }
        public void setOrgnlGrpInf(List<OriginalGroupInformation27> orgnlGrpInf) { this.orgnlGrpInf = orgnlGrpInf; }
        
        public List<PaymentTransaction106> getTxInf() { return txInf; }
        public void setTxInf(List<PaymentTransaction106> txInf) { this.txInf = txInf; }
        
        public List<SupplementaryData1> getSplmtryData() { return splmtryData; }
        public void setSplmtryData(List<SupplementaryData1> splmtryData) { this.splmtryData = splmtryData; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GroupHeader53 {
        
        @XmlElement(name = "MsgId", required = true)
        private String msgId;
        
        @XmlElement(name = "CreDtTm", required = true)
        private String creDtTm;
        
        @XmlElement(name = "InstgAgt")
        private BranchAndFinancialInstitutionIdentification6 instgAgt;
        
        @XmlElement(name = "InstdAgt")
        private BranchAndFinancialInstitutionIdentification6 instdAgt;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstgAgt() { return instgAgt; }
        public void setInstgAgt(BranchAndFinancialInstitutionIdentification6 instgAgt) { this.instgAgt = instgAgt; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstdAgt() { return instdAgt; }
        public void setInstdAgt(BranchAndFinancialInstitutionIdentification6 instdAgt) { this.instdAgt = instdAgt; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OriginalGroupInformation27 {
        
        @XmlElement(name = "OrgnlMsgId", required = true)
        private String orgnlMsgId;
        
        @XmlElement(name = "OrgnlMsgNmId", required = true)
        private String orgnlMsgNmId;
        
        @XmlElement(name = "OrgnlCreDtTm")
        private String orgnlCreDtTm;
        
        @XmlElement(name = "OrgnlNbOfTxs")
        private String orgnlNbOfTxs;

        // Getters and Setters
        public String getOrgnlMsgId() { return orgnlMsgId; }
        public void setOrgnlMsgId(String orgnlMsgId) { this.orgnlMsgId = orgnlMsgId; }
        
        public String getOrgnlMsgNmId() { return orgnlMsgNmId; }
        public void setOrgnlMsgNmId(String orgnlMsgNmId) { this.orgnlMsgNmId = orgnlMsgNmId; }
        
        public String getOrgnlCreDtTm() { return orgnlCreDtTm; }
        public void setOrgnlCreDtTm(String orgnlCreDtTm) { this.orgnlCreDtTm = orgnlCreDtTm; }
        
        public String getOrgnlNbOfTxs() { return orgnlNbOfTxs; }
        public void setOrgnlNbOfTxs(String orgnlNbOfTxs) { this.orgnlNbOfTxs = orgnlNbOfTxs; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PaymentTransaction106 {
        
        @XmlElement(name = "StsReqId")
        private String stsReqId;
        
        @XmlElement(name = "OrgnlGrpInf")
        private OriginalGroupInformation27 orgnlGrpInf;
        
        @XmlElement(name = "OrgnlInstrId")
        private String orgnlInstrId;
        
        @XmlElement(name = "OrgnlEndToEndId", required = true)
        private String orgnlEndToEndId;
        
        @XmlElement(name = "OrgnlTxId")
        private String orgnlTxId;
        
        @XmlElement(name = "OrgnlUETR")
        private String orgnlUETR;
        
        @XmlElement(name = "OrgnlClrSysRef")
        private String orgnlClrSysRef;
        
        @XmlElement(name = "OrgnlIntrBkSttlmAmt")
        private ActiveOrHistoricCurrencyAndAmount orgnlIntrBkSttlmAmt;
        
        @XmlElement(name = "OrgnlIntrBkSttlmDt")
        private String orgnlIntrBkSttlmDt;
        
        @XmlElement(name = "Assgnr")
        private BranchAndFinancialInstitutionIdentification6 assgnr;
        
        @XmlElement(name = "Assgne")
        private BranchAndFinancialInstitutionIdentification6 assgne;
        
        @XmlElement(name = "OrgnlTxRef")
        private OriginalTransactionReference28 orgnlTxRef;

        // Getters and Setters
        public String getStsReqId() { return stsReqId; }
        public void setStsReqId(String stsReqId) { this.stsReqId = stsReqId; }
        
        public String getOrgnlInstrId() { return orgnlInstrId; }
        public void setOrgnlInstrId(String orgnlInstrId) { this.orgnlInstrId = orgnlInstrId; }
        
        public String getOrgnlEndToEndId() { return orgnlEndToEndId; }
        public void setOrgnlEndToEndId(String orgnlEndToEndId) { this.orgnlEndToEndId = orgnlEndToEndId; }
        
        public String getOrgnlTxId() { return orgnlTxId; }
        public void setOrgnlTxId(String orgnlTxId) { this.orgnlTxId = orgnlTxId; }
        
        public String getOrgnlUETR() { return orgnlUETR; }
        public void setOrgnlUETR(String orgnlUETR) { this.orgnlUETR = orgnlUETR; }
        
        public String getOrgnlClrSysRef() { return orgnlClrSysRef; }
        public void setOrgnlClrSysRef(String orgnlClrSysRef) { this.orgnlClrSysRef = orgnlClrSysRef; }
        
        public ActiveOrHistoricCurrencyAndAmount getOrgnlIntrBkSttlmAmt() { return orgnlIntrBkSttlmAmt; }
        public void setOrgnlIntrBkSttlmAmt(ActiveOrHistoricCurrencyAndAmount orgnlIntrBkSttlmAmt) { this.orgnlIntrBkSttlmAmt = orgnlIntrBkSttlmAmt; }
        
        public String getOrgnlIntrBkSttlmDt() { return orgnlIntrBkSttlmDt; }
        public void setOrgnlIntrBkSttlmDt(String orgnlIntrBkSttlmDt) { this.orgnlIntrBkSttlmDt = orgnlIntrBkSttlmDt; }
        
        public BranchAndFinancialInstitutionIdentification6 getAssgnr() { return assgnr; }
        public void setAssgnr(BranchAndFinancialInstitutionIdentification6 assgnr) { this.assgnr = assgnr; }
        
        public BranchAndFinancialInstitutionIdentification6 getAssgne() { return assgne; }
        public void setAssgne(BranchAndFinancialInstitutionIdentification6 assgne) { this.assgne = assgne; }
        
        public OriginalTransactionReference28 getOrgnlTxRef() { return orgnlTxRef; }
        public void setOrgnlTxRef(OriginalTransactionReference28 orgnlTxRef) { this.orgnlTxRef = orgnlTxRef; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActiveOrHistoricCurrencyAndAmount {
        
        @XmlElement(name = "Ccy", required = true)
        private String ccy;
        
        @XmlValue
        private java.math.BigDecimal value;

        // Getters and Setters
        public String getCcy() { return ccy; }
        public void setCcy(String ccy) { this.ccy = ccy; }
        
        public java.math.BigDecimal getValue() { return value; }
        public void setValue(java.math.BigDecimal value) { this.value = value; }
    }

    // Supporting classes
    public static class BranchAndFinancialInstitutionIdentification6 {}
    public static class OriginalTransactionReference28 {}
    public static class SupplementaryData1 {}

    // Getters and Setters for main class
    public FIToFIPmtStsReq getFiToFIPmtStsReq() { return fiToFIPmtStsReq; }
    public void setFiToFIPmtStsReq(FIToFIPmtStsReq fiToFIPmtStsReq) { this.fiToFIPmtStsReq = fiToFIPmtStsReq; }
}
