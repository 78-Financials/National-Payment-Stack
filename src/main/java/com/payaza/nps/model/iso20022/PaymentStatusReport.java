package com.payaza.nps.model.iso20022;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISO 20022 pacs.002 - Payment Status Report Message
 * Used for reporting payment status (ACSC - Accepted, RJCT - Rejected)
 */
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentStatusReport {

    @XmlElement(name = "FIToFIPmtStsRpt", required = true)
    private FIToFIPmtStsRpt fiToFIPmtStsRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FIToFIPmtStsRpt {
        
        @XmlElement(name = "GrpHdr", required = true)
        private GroupHeader53 grpHdr;
        
        @XmlElement(name = "OrgnlGrpInfAndSts")
        private OriginalGroupInformation29 orgnlGrpInfAndSts;
        
        @XmlElement(name = "TxInfAndSts")
        private List<PaymentTransaction110> txInfAndSts;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public GroupHeader53 getGrpHdr() { return grpHdr; }
        public void setGrpHdr(GroupHeader53 grpHdr) { this.grpHdr = grpHdr; }
        
        public OriginalGroupInformation29 getOrgnlGrpInfAndSts() { return orgnlGrpInfAndSts; }
        public void setOrgnlGrpInfAndSts(OriginalGroupInformation29 orgnlGrpInfAndSts) { this.orgnlGrpInfAndSts = orgnlGrpInfAndSts; }
        
        public List<PaymentTransaction110> getTxInfAndSts() { return txInfAndSts; }
        public void setTxInfAndSts(List<PaymentTransaction110> txInfAndSts) { this.txInfAndSts = txInfAndSts; }
        
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
        
        @XmlElement(name = "OrgnlMsgId", required = true)
        private String orgnlMsgId;
        
        @XmlElement(name = "OrgnlMsgNmId", required = true)
        private String orgnlMsgNmId;
        
        @XmlElement(name = "OrgnlCreDtTm")
        private String orgnlCreDtTm;

        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public String getCreDtTm() { return creDtTm; }
        public void setCreDtTm(String creDtTm) { this.creDtTm = creDtTm; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstgAgt() { return instgAgt; }
        public void setInstgAgt(BranchAndFinancialInstitutionIdentification6 instgAgt) { this.instgAgt = instgAgt; }
        
        public BranchAndFinancialInstitutionIdentification6 getInstdAgt() { return instdAgt; }
        public void setInstdAgt(BranchAndFinancialInstitutionIdentification6 instdAgt) { this.instdAgt = instdAgt; }
        
        public String getOrgnlMsgId() { return orgnlMsgId; }
        public void setOrgnlMsgId(String orgnlMsgId) { this.orgnlMsgId = orgnlMsgId; }
        
        public String getOrgnlMsgNmId() { return orgnlMsgNmId; }
        public void setOrgnlMsgNmId(String orgnlMsgNmId) { this.orgnlMsgNmId = orgnlMsgNmId; }
        
        public String getOrgnlCreDtTm() { return orgnlCreDtTm; }
        public void setOrgnlCreDtTm(String orgnlCreDtTm) { this.orgnlCreDtTm = orgnlCreDtTm; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OriginalGroupInformation29 {
        
        @XmlElement(name = "OrgnlMsgId", required = true)
        private String orgnlMsgId;
        
        @XmlElement(name = "OrgnlMsgNmId", required = true)
        private String orgnlMsgNmId;
        
        @XmlElement(name = "OrgnlCreDtTm")
        private String orgnlCreDtTm;
        
        @XmlElement(name = "OrgnlNbOfTxs")
        private String orgnlNbOfTxs;
        
        @XmlElement(name = "GrpSts")
        private GroupStatus3Code grpSts;
        
        @XmlElement(name = "StsRsnInf")
        private List<StatusReasonInformation12> stsRsnInf;
        
        @XmlElement(name = "NbOfTxsPerSts")
        private List<NumberOfTransactionsPerStatus5> nbOfTxsPerSts;

        // Getters and Setters
        public String getOrgnlMsgId() { return orgnlMsgId; }
        public void setOrgnlMsgId(String orgnlMsgId) { this.orgnlMsgId = orgnlMsgId; }
        
        public String getOrgnlMsgNmId() { return orgnlMsgNmId; }
        public void setOrgnlMsgNmId(String orgnlMsgNmId) { this.orgnlMsgNmId = orgnlMsgNmId; }
        
        public String getOrgnlCreDtTm() { return orgnlCreDtTm; }
        public void setOrgnlCreDtTm(String orgnlCreDtTm) { this.orgnlCreDtTm = orgnlCreDtTm; }
        
        public String getOrgnlNbOfTxs() { return orgnlNbOfTxs; }
        public void setOrgnlNbOfTxs(String orgnlNbOfTxs) { this.orgnlNbOfTxs = orgnlNbOfTxs; }
        
        public GroupStatus3Code getGrpSts() { return grpSts; }
        public void setGrpSts(GroupStatus3Code grpSts) { this.grpSts = grpSts; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PaymentTransaction110 {
        
        @XmlElement(name = "StsId")
        private String stsId;
        
        @XmlElement(name = "OrgnlGrpInf")
        private OriginalGroupInformation29 orgnlGrpInf;
        
        @XmlElement(name = "OrgnlInstrId")
        private String orgnlInstrId;
        
        @XmlElement(name = "OrgnlEndToEndId", required = true)
        private String orgnlEndToEndId;
        
        @XmlElement(name = "OrgnlTxId")
        private String orgnlTxId;
        
        @XmlElement(name = "OrgnlUETR")
        private String orgnlUETR;
        
        @XmlElement(name = "TxSts")
        private TransactionIndividualStatus3Code txSts;
        
        @XmlElement(name = "StsRsnInf")
        private List<StatusReasonInformation12> stsRsnInf;
        
        @XmlElement(name = "ChrgsInf")
        private List<Charges7> chrgsInf;
        
        @XmlElement(name = "AccptncDtTm")
        private String accptncDtTm;
        
        @XmlElement(name = "AcctSvcrRef")
        private String acctSvcrRef;
        
        @XmlElement(name = "ClrSysRef")
        private String clrSysRef;
        
        @XmlElement(name = "InstgAgt")
        private BranchAndFinancialInstitutionIdentification6 instgAgt;
        
        @XmlElement(name = "InstdAgt")
        private BranchAndFinancialInstitutionIdentification6 instdAgt;
        
        @XmlElement(name = "OrgnlTxRef")
        private OriginalTransactionReference28 orgnlTxRef;
        
        @XmlElement(name = "SplmtryData")
        private List<SupplementaryData1> splmtryData;

        // Getters and Setters
        public String getStsId() { return stsId; }
        public void setStsId(String stsId) { this.stsId = stsId; }
        
        public String getOrgnlInstrId() { return orgnlInstrId; }
        public void setOrgnlInstrId(String orgnlInstrId) { this.orgnlInstrId = orgnlInstrId; }
        
        public String getOrgnlEndToEndId() { return orgnlEndToEndId; }
        public void setOrgnlEndToEndId(String orgnlEndToEndId) { this.orgnlEndToEndId = orgnlEndToEndId; }
        
        public String getOrgnlTxId() { return orgnlTxId; }
        public void setOrgnlTxId(String orgnlTxId) { this.orgnlTxId = orgnlTxId; }
        
        public String getOrgnlUETR() { return orgnlUETR; }
        public void setOrgnlUETR(String orgnlUETR) { this.orgnlUETR = orgnlUETR; }
        
        public TransactionIndividualStatus3Code getTxSts() { return txSts; }
        public void setTxSts(TransactionIndividualStatus3Code txSts) { this.txSts = txSts; }
        
        public List<StatusReasonInformation12> getStsRsnInf() { return stsRsnInf; }
        public void setStsRsnInf(List<StatusReasonInformation12> stsRsnInf) { this.stsRsnInf = stsRsnInf; }
        
        public String getAccptncDtTm() { return accptncDtTm; }
        public void setAccptncDtTm(String accptncDtTm) { this.accptncDtTm = accptncDtTm; }
        
        public String getAcctSvcrRef() { return acctSvcrRef; }
        public void setAcctSvcrRef(String acctSvcrRef) { this.acctSvcrRef = acctSvcrRef; }
        
        public String getClrSysRef() { return clrSysRef; }
        public void setClrSysRef(String clrSysRef) { this.clrSysRef = clrSysRef; }
        
        public OriginalTransactionReference28 getOrgnlTxRef() { return orgnlTxRef; }
        public void setOrgnlTxRef(OriginalTransactionReference28 orgnlTxRef) { this.orgnlTxRef = orgnlTxRef; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class StatusReasonInformation12 {
        
        @XmlElement(name = "Orgtr")
        private PartyIdentification135 orgtr;
        
        @XmlElement(name = "Rsn")
        private StatusReason6Choice rsn;
        
        @XmlElement(name = "AddtlInf")
        private List<String> addtlInf;

        // Getters and Setters
        public PartyIdentification135 getOrgtr() { return orgtr; }
        public void setOrgtr(PartyIdentification135 orgtr) { this.orgtr = orgtr; }
        
        public StatusReason6Choice getRsn() { return rsn; }
        public void setRsn(StatusReason6Choice rsn) { this.rsn = rsn; }
        
        public List<String> getAddtlInf() { return addtlInf; }
        public void setAddtlInf(List<String> addtlInf) { this.addtlInf = addtlInf; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class StatusReason6Choice {
        
        @XmlElement(name = "Cd")
        private ExternalStatusReason1Code cd;
        
        @XmlElement(name = "Prtry")
        private String prtry;

        // Getters and Setters
        public ExternalStatusReason1Code getCd() { return cd; }
        public void setCd(ExternalStatusReason1Code cd) { this.cd = cd; }
        
        public String getPrtry() { return prtry; }
        public void setPrtry(String prtry) { this.prtry = prtry; }
    }

    // Enums for status codes
    @XmlType(name = "TransactionIndividualStatus3Code")
    @XmlEnum
    public enum TransactionIndividualStatus3Code {
        ACSC("ACSC"), // AcceptedSettlementCompleted
        RJCT("RJCT"), // Rejected
        PDNG("PDNG"), // Pending
        PART("PART"), // PartiallyAccepted
        CANC("CANC"), // Cancelled
        DUPL("DUPL"), // Duplicate
        NMAT("NMAT"), // NoMatch
        NAUT("NAUT"); // NotAuthorized

        private final String value;

        TransactionIndividualStatus3Code(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static TransactionIndividualStatus3Code fromValue(String v) {
            for (TransactionIndividualStatus3Code c: TransactionIndividualStatus3Code.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }

    @XmlType(name = "GroupStatus3Code")
    @XmlEnum
    public enum GroupStatus3Code {
        ACSP("ACSP"), // AcceptedSettlementInProcess
        ACSC("ACSC"), // AcceptedSettlementCompleted
        RJCT("RJCT"), // Rejected
        PART("PART"), // PartiallyAccepted
        PDNG("PDNG"); // Pending

        private final String value;

        GroupStatus3Code(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static GroupStatus3Code fromValue(String v) {
            for (GroupStatus3Code c: GroupStatus3Code.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }

    // Supporting classes
    public static class BranchAndFinancialInstitutionIdentification6 {}
    public static class NumberOfTransactionsPerStatus5 {}
    public static class Charges7 {}
    public static class OriginalTransactionReference28 {}
    public static class PartyIdentification135 {}
    public static class ExternalStatusReason1Code {}
    public static class SupplementaryData1 {}

    // Getters and Setters for main class
    public FIToFIPmtStsRpt getFiToFIPmtStsRpt() { return fiToFIPmtStsRpt; }
    public void setFiToFIPmtStsRpt(FIToFIPmtStsRpt fiToFIPmtStsRpt) { this.fiToFIPmtStsRpt = fiToFIPmtStsRpt; }
}
