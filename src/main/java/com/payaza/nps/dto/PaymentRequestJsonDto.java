package com.payaza.nps.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * JSON DTO for payment request API
 */
public class PaymentRequestJsonDto {

    @NotBlank(message = "End-to-end ID is required")
    private String endToEndId;

    @NotBlank(message = "Debtor account is required")
    private String debtorAccount;

    @NotBlank(message = "Creditor account is required")
    private String creditorAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Size(max = 70, message = "Debtor name cannot exceed 70 characters")
    private String debtorName;

    @Size(max = 70, message = "Creditor name cannot exceed 70 characters")
    private String creditorName;

    @Size(max = 140, message = "Remittance information cannot exceed 140 characters")
    private String remittanceInfo;

    @NotBlank(message = "Debtor bank ID is required")
    private String debtorBankId;

    @NotBlank(message = "Creditor bank ID is required")
    private String creditorBankId;

    private String debtorBankName;
    private String creditorBankName;
    private String instructionId;
    private String transactionId;
    private String chargeBearer;
    private List<String> unstructuredRemittanceInfo;

    // Constructors
    public PaymentRequestJsonDto() {}

    public PaymentRequestJsonDto(String endToEndId, String debtorAccount, String creditorAccount, 
                               BigDecimal amount, String currency) {
        this.endToEndId = endToEndId;
        this.debtorAccount = debtorAccount;
        this.creditorAccount = creditorAccount;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and Setters
    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(String creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public String getRemittanceInfo() {
        return remittanceInfo;
    }

    public void setRemittanceInfo(String remittanceInfo) {
        this.remittanceInfo = remittanceInfo;
    }

    public String getDebtorBankId() {
        return debtorBankId;
    }

    public void setDebtorBankId(String debtorBankId) {
        this.debtorBankId = debtorBankId;
    }

    public String getCreditorBankId() {
        return creditorBankId;
    }

    public void setCreditorBankId(String creditorBankId) {
        this.creditorBankId = creditorBankId;
    }

    public String getDebtorBankName() {
        return debtorBankName;
    }

    public void setDebtorBankName(String debtorBankName) {
        this.debtorBankName = debtorBankName;
    }

    public String getCreditorBankName() {
        return creditorBankName;
    }

    public void setCreditorBankName(String creditorBankName) {
        this.creditorBankName = creditorBankName;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getChargeBearer() {
        return chargeBearer;
    }

    public void setChargeBearer(String chargeBearer) {
        this.chargeBearer = chargeBearer;
    }

    public List<String> getUnstructuredRemittanceInfo() {
        return unstructuredRemittanceInfo;
    }

    public void setUnstructuredRemittanceInfo(List<String> unstructuredRemittanceInfo) {
        this.unstructuredRemittanceInfo = unstructuredRemittanceInfo;
    }
}
