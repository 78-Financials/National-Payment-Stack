package com.payaza.nps.model.iso20022;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISO 20022 pacs.008 - Payment Request Message
 * Used for sending payment requests from debtor bank to creditor bank
 */
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentRequest {

    @XmlElement(name = "FIToFICstmrCdtTrf", required = true)
    private FIToFICstmrCdtTrf fiToFICstmrCdtTrf;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FIToFICstmrCdtTrf {
        
        @XmlElement(name = "GrpHdr", required = true)
        private GroupHeader93 grpHdr;
        
        @XmlElement(name = "CdtTrfTxInf", required = true)
        private List<CreditTransferTransaction34> cdtTrfTxInf;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public GroupHeader93 getGrpHdr() { return grpHdr; }
        public void setGrpHdr(GroupHeader93 grpHdr) { this.grpHdr = grpHdr; }
        
        public List<CreditTransferTransaction34> getCdtTrfTxInf() { return cdtTrfTxInf; }
        public void setCdtTrfTxInf(List<CreditTransferTransaction34> cdtTrfTxInf) { this.cdtTrfTxInf = cdtTrfTxInf; }
        
        public List<SupplementaryData1> getSplmtryData() { return splmtryData; }
        public void setSplmtryData(List<SupplementaryData1> splmtryData) { this.splmtryData = splmtryData; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GroupHeader93 {
        
        @XmlElement(name = "MsgId", required = true)
        private String msgId;
        
        @XmlElement(name = "CreDtTm", required = true)
        private String creDtTm;
        
        @XmlElement(name = "NbOfTxs", required = true)
        private String nbOfTxs;
        
        @XmlElement(name = "SttlmAmt")
        private ActiveOrHistoricCurrencyAndAmount sttlmAmt;
        
        @XmlElement(name = "InstgAgt", required = true)
        private BranchAndFinancialInstitutionIdentification6 instgAgt;
        
        @XmlElement(name = "InstdAgt", required = true)
        private BranchAndFinancialInstitutionIdentification6 instdAgt;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
        
        public String getNbOfTxs() { return nbOfTxs; }
        public void setNbOfTxs(String nbOfTxs) { this.nbOfTxs = nbOfTxs; }
        
        public ActiveOrHistoricCurrencyAndAmount getSttlmAmt() { return sttlmAmt; }
        public void setSttlmAmt(ActiveOrHistoricCurrencyAndAmount sttlmAmt) { this.sttlmAmt = sttlmAmt; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstgAgt() { return instgAgt; }
        public void setInstgAgt(BranchAndFinancialInstitutionIdentification6 instgAgt) { this.instgAgt = instgAgt; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstdAgt() { return instdAgt; }
        public void setInstdAgt(BranchAndFinancialInstitutionIdentification6 instdAgt) { this.instdAgt = instdAgt; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreditTransferTransaction34 {
        
        @XmlElement(name = "PmtId", required = true)
        private PaymentIdentification6 pmtId;
        
        @XmlElement(name = "PmtTpInf")
        private PaymentTypeInformation26 pmtTpInf;
        
        @XmlElement(name = "IntrBkSttlmAmt", required = true)
        private ActiveOrHistoricCurrencyAndAmount intrBkSttlmAmt;
        
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
        
        @XmlElement(name = "SttlmPrty")
        private Priority3Code sttlmPrty;
        
        @XmlElement(name = "SttlmTmIndctn")
        private SettlementDateTimeIndication1 sttlmTmIndctn;
        
        @XmlElement(name = "SttlmTmReq")
        private SettlementTimeRequest2 sttlmTmReq;
        
        @XmlElement(name = "AccptncDtTm")
        private String accptncDtTm;
        
        @XmlElement(name = "PoolgAdjstmntDt")
        private String poolgAdjstmntDt;
        
        @XmlElement(name = "InstrPrty")
        private Priority2Code instrPrty;
        
        @XmlElement(name = "ChrgBr")
        private ChargeBearerType1Code chrgBr;
        
        @XmlElement(name = "ChrgsInf")
        private List<Charges7> chrgsInf;
        
        @XmlElement(name = "PrvsInstgAgt1")
        private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt1;
        
        @XmlElement(name = "PrvsInstgAgt1Acct")
        private CashAccount38 prvsInstgAgt1Acct;
        
        @XmlElement(name = "PrvsInstgAgt2")
        private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt2;
        
        @XmlElement(name = "PrvsInstgAgt2Acct")
        private CashAccount38 prvsInstgAgt2Acct;
        
        @XmlElement(name = "PrvsInstgAgt3")
        private BranchAndFinancialInstitutionIdentification6 prvsInstgAgt3;
        
        @XmlElement(name = "PrvsInstgAgt3Acct")
        private CashAccount38 prvsInstgAgt3Acct;
        
        @XmlElement(name = "InstgAgt")
        private BranchAndFinancialInstitutionIdentification6 instgAgt;
        
        @XmlElement(name = "InstdAgt")
        private BranchAndFinancialInstitutionIdentification6 instdAgt;
        
        @XmlElement(name = "IntrmyAgt1")
        private BranchAndFinancialInstitutionIdentification6 intrmyAgt1;
        
        @XmlElement(name = "IntrmyAgt1Acct")
        private CashAccount38 intrmyAgt1Acct;
        
        @XmlElement(name = "IntrmyAgt2")
        private BranchAndFinancialInstitutionIdentification6 intrmyAgt2;
        
        @XmlElement(name = "IntrmyAgt2Acct")
        private CashAccount38 intrmyAgt2Acct;
        
        @XmlElement(name = "IntrmyAgt3")
        private BranchAndFinancialInstitutionIdentification6 intrmyAgt3;
        
        @XmlElement(name = "IntrmyAgt3Acct")
        private CashAccount38 intrmyAgt3Acct;
        
        @XmlElement(name = "CdtrAgt")
        private BranchAndFinancialInstitutionIdentification6 cdtrAgt;
        
        @XmlElement(name = "CdtrAgtAcct")
        private CashAccount38 cdtrAgtAcct;
        
        @XmlElement(name = "Cdtr")
        private PartyIdentification135 cdtr;
        
        @XmlElement(name = "CdtrAcct")
        private CashAccount38 cdtrAcct;
        
        @XmlElement(name = "UltmtCdtr")
        private PartyIdentification135 ultmtCdtr;
        
        @XmlElement(name = "InitgPty")
        private PartyIdentification135 initgPty;
        
        @XmlElement(name = "InstrmForCdtrAgt")
        private List<InstructionForCreditorAgent1> instrmForCdtrAgt;
        
        @XmlElement(name = "InstrForNxtAgt")
        private List<InstructionForNextAgent1> instrForNxtAgt;
        
        @XmlElement(name = "Tax")
        private TaxInformation8 tax;
        
        @XmlElement(name = "RmtInf")
        private RemittanceInformation16 rmtInf;
        
        @XmlElement(name = "InstdAmt")
        private ActiveOrHistoricCurrencyAndAmount instdAmt;
        
        @XmlElement(name = "XchgRateInf")
        private ExchangeRate1 xchgRateInf;
        
        @XmlElement(name = "ChqInstr")
        private Cheque11 chqInstr;
        
        @XmlElement(name = "ClrSysRef")
        private String clrSysRef;
        
        @XmlElement(name = "Dbtr")
        private PartyIdentification135 dbtr;
        
        @XmlElement(name = "DbtrAcct")
        private CashAccount38 dbtrAcct;
        
        @XmlElement(name = "DbtrAgt")
        private BranchAndFinancialInstitutionIdentification6 dbtrAgt;
        
        @XmlElement(name = "DbtrAgtAcct")
        private CashAccount38 dbtrAgtAcct;
        
        @XmlElement(name = "SttlmInf")
        private SettlementInstruction7 sttlmInf;
        
        @XmlElement(name = "CdtTrfTx")
        private CreditTransferTransaction35 cdtTrfTx;

        // Getters and Setters for key fields
        public PaymentIdentification6 getPmtId() { return pmtId; }
        public void setPmtId(PaymentIdentification6 pmtId) { this.pmtId = pmtId; }
        
        public ActiveOrHistoricCurrencyAndAmount getIntrBkSttlmAmt() { return intrBkSttlmAmt; }
        public void setIntrBkSttlmAmt(ActiveOrHistoricCurrencyAndAmount intrBkSttlmAmt) { this.intrBkSttlmAmt = intrBkSttlmAmt; }
        
        public PartyIdentification135 getDbtr() { return dbtr; }
        public void setDbtr(PartyIdentification135 dbtr) { this.dbtr = dbtr; }
        
        public PartyIdentification135 getCdtr() { return cdtr; }
        public void setCdtr(PartyIdentification135 cdtr) { this.cdtr = cdtr; }
        
        public BranchAndFinancialInstitutionIdentification6 getDbtrAgt() { return dbtrAgt; }
        public void setDbtrAgt(BranchAndFinancialInstitutionIdentification6 dbtrAgt) { this.dbtrAgt = dbtrAgt; }
        
        public BranchAndFinancialInstitutionIdentification6 getCdtrAgt() { return cdtrAgt; }
        public void setCdtrAgt(BranchAndFinancialInstitutionIdentification6 cdtrAgt) { this.cdtrAgt = cdtrAgt; }
        
        public RemittanceInformation16 getRmtInf() { return rmtInf; }
        public void setRmtInf(RemittanceInformation16 rmtInf) { this.rmtInf = rmtInf; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PaymentIdentification6 {
        
        @XmlElement(name = "InstrId")
        private String instrId;
        
        @XmlElement(name = "EndToEndId", required = true)
        private String endToEndId;
        
        @XmlElement(name = "TxId")
        private String txId;
        
        @XmlElement(name = "ClrSysRef")
        private String clrSysRef;

        // Getters and Setters
        public String getInstrId() { return instrId; }
        public void setInstrId(String instrId) { this.instrId = instrId; }
        
        public String getEndToEndId() { return endToEndId; }
        public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
        
        public String getTxId() { return txId; }
        public void setTxId(String txId) { this.txId = txId; }
        
        public String getClrSysRef() { return clrSysRef; }
        public void setClrSysRef(String clrSysRef) { this.clrSysRef = clrSysRef; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActiveOrHistoricCurrencyAndAmount {
        
        @XmlElement(name = "Ccy", required = true)
        private String ccy;
        
        @XmlValue
        private BigDecimal value;

        // Getters and Setters
        public String getCcy() { return ccy; }
        public void setCcy(String ccy) { this.ccy = ccy; }
        
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PartyIdentification135 {
        
        @XmlElement(name = "Nm")
        private String nm;
        
        @XmlElement(name = "PstlAdr")
        private PostalAddress24 pstlAdr;
        
        @XmlElement(name = "Id")
        private Party38Choice id;
        
        @XmlElement(name = "CtryOfRes")
        private String ctryOfRes;
        
        @XmlElement(name = "CtctDtls")
        private Contact4 ctctDtls;

        // Getters and Setters
        public String getNm() { return nm; }
        public void setNm(String nm) { this.nm = nm; }
        
        public Party38Choice getId() { return id; }
        public void setId(Party38Choice id) { this.id = id; }
        
        public PostalAddress24 getPstlAdr() { return pstlAdr; }
        public void setPstlAdr(PostalAddress24 pstlAdr) { this.pstlAdr = pstlAdr; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Party38Choice {
        
        @XmlElement(name = "OrgId")
        private OrganisationIdentification29 orgId;
        
        @XmlElement(name = "PrvtId")
        private PersonIdentification13 prvtId;

        // Getters and Setters
        public OrganisationIdentification29 getOrgId() { return orgId; }
        public void setOrgId(OrganisationIdentification29 orgId) { this.orgId = orgId; }
        
        public PersonIdentification13 getPrvtId() { return prvtId; }
        public void setPrvtId(PersonIdentification13 prvtId) { this.prvtId = prvtId; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RemittanceInformation16 {
        
        @XmlElement(name = "Ustrd")
        private List<String> ustrd;
        
        @XmlElement(name = "Strd")
        private List<StructuredRemittanceInformation16> strd;

        // Getters and Setters
        public List<String> getUstrd() { return ustrd; }
        public void setUstrd(List<String> ustrd) { this.ustrd = ustrd; }
        
        public List<StructuredRemittanceInformation16> getStrd() { return strd; }
        public void setStrd(List<StructuredRemittanceInformation16> strd) { this.strd = strd; }
    }

    // Supporting classes - simplified for brevity
    public static class BranchAndFinancialInstitutionIdentification6 {}
    public static class PaymentTypeInformation26 {}
    public static class Priority3Code {}
    public static class SettlementDateTimeIndication1 {}
    public static class SettlementTimeRequest2 {}
    public static class Priority2Code {}
    public static class ChargeBearerType1Code {}
    public static class Charges7 {}
    public static class CashAccount38 {}
    public static class InstructionForCreditorAgent1 {}
    public static class InstructionForNextAgent1 {}
    public static class TaxInformation8 {}
    public static class StructuredRemittanceInformation16 {}
    public static class ExchangeRate1 {}
    public static class Cheque11 {}
    public static class SettlementInstruction7 {}
    public static class CreditTransferTransaction35 {}
    public static class PostalAddress24 {}
    public static class Contact4 {}
    public static class OrganisationIdentification29 {}
    public static class PersonIdentification13 {}
    public static class SupplementaryData1 {}

    // Getters and Setters for main class
    public FIToFICstmrCdtTrf getFiToFICstmrCdtTrf() { return fiToFICstmrCdtTrf; }
    public void setFiToFICstmrCdtTrf(FIToFICstmrCdtTrf fiToFICstmrCdtTrf) { this.fiToFICstmrCdtTrf = fiToFICstmrCdtTrf; }
}
