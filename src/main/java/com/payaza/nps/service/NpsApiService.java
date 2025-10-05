package com.payaza.nps.service;

import com.payaza.nps.config.NpsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * NPS API Service
 * 
 * Handles actual HTTP calls to NIBSS endpoints:
 * - ACMT.023 (Identification Verification Request)
 * - ACMT.024 (Identification Verification Report)
 * - PACS.008 (Payment Request)
 * - PACS.002 (Payment Status Report)
 * - PACS.028 (Payment Status Request)
 * - Get Participants
 */
@Service
public class NpsApiService {

    private static final Logger logger = LoggerFactory.getLogger(NpsApiService.class);

    @Autowired
    private NpsConfiguration npsConfig;

    private final WebClient webClient;

    public NpsApiService() {
        this.webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .build();
    }

    /**
     * Send ACMT.023 (Identification Verification Request) to NIBSS
     */
    public String sendAcmt023(String signedEncryptedXml) throws Exception {
        logger.info("Sending ACMT.023 to NIBSS endpoint: {}", npsConfig.getAcmt023Endpoint());
        
        try {
            String response = webClient.post()
                .uri(npsConfig.getAcmt023Endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .bodyValue(signedEncryptedXml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("ACMT.023 sent successfully to NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error sending ACMT.023 to NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to send ACMT.023 to NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error sending ACMT.023 to NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to send ACMT.023 to NIBSS: " + e.getMessage(), e);
        }
    }

    /**
     * Send ACMT.024 (Identification Verification Report) to NIBSS
     */
    public String sendAcmt024(String signedEncryptedXml) throws Exception {
        logger.info("Sending ACMT.024 to NIBSS endpoint: {}", npsConfig.getAcmt024Endpoint());
        
        try {
            String response = webClient.post()
                .uri(npsConfig.getAcmt024Endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .bodyValue(signedEncryptedXml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("ACMT.024 sent successfully to NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error sending ACMT.024 to NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to send ACMT.024 to NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error sending ACMT.024 to NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to send ACMT.024 to NIBSS: " + e.getMessage(), e);
        }
    }

    /**
     * Send PACS.008 (Payment Request) to NIBSS
     */
    public String sendPacs008(String signedEncryptedXml) throws Exception {
        logger.info("Sending PACS.008 to NIBSS endpoint: {}", npsConfig.getPacs008Endpoint());
        
        try {
            String response = webClient.post()
                .uri(npsConfig.getPacs008Endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .bodyValue(signedEncryptedXml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("PACS.008 sent successfully to NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error sending PACS.008 to NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to send PACS.008 to NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error sending PACS.008 to NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to send PACS.008 to NIBSS: " + e.getMessage(), e);
        }
    }

    /**
     * Send PACS.002 (Payment Status Report) to NIBSS
     */
    public String sendPacs002(String signedEncryptedXml) throws Exception {
        logger.info("Sending PACS.002 to NIBSS endpoint: {}", npsConfig.getPacs002Endpoint());
        
        try {
            String response = webClient.post()
                .uri(npsConfig.getPacs002Endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .bodyValue(signedEncryptedXml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("PACS.002 sent successfully to NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error sending PACS.002 to NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to send PACS.002 to NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error sending PACS.002 to NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to send PACS.002 to NIBSS: " + e.getMessage(), e);
        }
    }

    /**
     * Send PACS.028 (Payment Status Request) to NIBSS
     */
    public String sendPacs028(String signedEncryptedXml) throws Exception {
        logger.info("Sending PACS.028 to NIBSS endpoint: {}", npsConfig.getPacs028Endpoint());
        
        try {
            String response = webClient.post()
                .uri(npsConfig.getPacs028Endpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .bodyValue(signedEncryptedXml)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("PACS.028 sent successfully to NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error sending PACS.028 to NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to send PACS.028 to NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error sending PACS.028 to NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to send PACS.028 to NIBSS: " + e.getMessage(), e);
        }
    }

    /**
     * Get participants list from NIBSS
     */
    public String getParticipants() throws Exception {
        logger.info("Getting participants from NIBSS endpoint: {}", npsConfig.getParticipantsEndpoint());
        
        try {
            String response = webClient.get()
                .uri(npsConfig.getParticipantsEndpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + npsConfig.getClientSecret())
                .header("X-Client-Id", npsConfig.getClientId())
                .header("X-Merchant-Id", npsConfig.getMerchantId())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(npsConfig.getTimeoutSeconds()))
                .block();
            
            logger.info("Participants retrieved successfully from NIBSS");
            return response;
            
        } catch (WebClientResponseException e) {
            logger.error("Error getting participants from NIBSS: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Failed to get participants from NIBSS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error getting participants from NIBSS: {}", e.getMessage(), e);
            throw new Exception("Failed to get participants from NIBSS: " + e.getMessage(), e);
        }
    }
}
