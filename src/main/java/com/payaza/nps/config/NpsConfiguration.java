package com.payaza.nps.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Configuration properties for Nigerian Payment Stack integration
 */
@Configuration
@ConfigurationProperties(prefix = "nps")
@Validated
public class NpsConfiguration {

    @NotBlank
    private String baseUrl = "https://nps.nibss-plc.com.ng";

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String merchantId;

    @NotNull
    private Integer timeoutSeconds = 30;

    @NotNull
    private Boolean enableEncryption = true;

    @NotBlank
    private String encryptionKey;

    private String callbackUrl;

    // NIBSS Endpoint URLs
    private String acmt023Endpoint = "/acmt023";
    private String acmt024Endpoint = "/acmt024";
    private String pacs008Endpoint = "/pacs008";
    private String pacs002Endpoint = "/pacs002";
    private String pacs028Endpoint = "/pacs028";
    private String participantsEndpoint = "/participants";

    // Cryptographic keys (loaded from configuration)
    private PrivateKey privateKey;
    private PublicKey npsPublicKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Boolean getEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(Boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    // Endpoint URL getters and setters
    public String getAcmt023Endpoint() {
        return baseUrl + acmt023Endpoint;
    }

    public void setAcmt023Endpoint(String acmt023Endpoint) {
        this.acmt023Endpoint = acmt023Endpoint;
    }

    public String getAcmt024Endpoint() {
        return baseUrl + acmt024Endpoint;
    }

    public void setAcmt024Endpoint(String acmt024Endpoint) {
        this.acmt024Endpoint = acmt024Endpoint;
    }

    public String getPacs008Endpoint() {
        return baseUrl + pacs008Endpoint;
    }

    public void setPacs008Endpoint(String pacs008Endpoint) {
        this.pacs008Endpoint = pacs008Endpoint;
    }

    public String getPacs002Endpoint() {
        return baseUrl + pacs002Endpoint;
    }

    public void setPacs002Endpoint(String pacs002Endpoint) {
        this.pacs002Endpoint = pacs002Endpoint;
    }

    public String getPacs028Endpoint() {
        return baseUrl + pacs028Endpoint;
    }

    public void setPacs028Endpoint(String pacs028Endpoint) {
        this.pacs028Endpoint = pacs028Endpoint;
    }

    public String getParticipantsEndpoint() {
        return baseUrl + participantsEndpoint;
    }

    public void setParticipantsEndpoint(String participantsEndpoint) {
        this.participantsEndpoint = participantsEndpoint;
    }

    // Cryptographic key getters and setters
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getNpsPublicKey() {
        return npsPublicKey;
    }

    public void setNpsPublicKey(PublicKey npsPublicKey) {
        this.npsPublicKey = npsPublicKey;
    }
}
