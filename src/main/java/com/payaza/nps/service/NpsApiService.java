package com.payaza.nps.service;

import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.dto.PaymentResponseDto;
import com.payaza.nps.model.PaymentRequest;
import com.payaza.nps.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for communicating with NPS API
 */
@Service
public class NpsApiService {

    private static final Logger logger = LoggerFactory.getLogger(NpsApiService.class);

    @Autowired
    private WebClient npsWebClient;

    @Autowired
    private NpsConfiguration npsConfig;

    @Autowired
    private EncryptionService encryptionService;

    /**
     * Send payment request to NPS API
     */
    public PaymentResponseDto sendPaymentRequest(PaymentRequest paymentRequest) {
        logger.info("Sending payment request to NPS API: {}", paymentRequest.getPaymentId());

        try {
            // Prepare request payload
            Map<String, Object> requestPayload = preparePaymentPayload(paymentRequest);

            // Send request to NPS API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = npsWebClient
                    .post()
                    .uri("/api/v1/payments")
                    .headers(this::addAuthHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parsePaymentResponse(response, paymentRequest);

        } catch (WebClientResponseException e) {
            logger.error("NPS API error for payment {}: {}", paymentRequest.getPaymentId(), e.getMessage());
            return createErrorResponse(paymentRequest, "API_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error calling NPS API for payment {}: {}", paymentRequest.getPaymentId(), e.getMessage());
            return createErrorResponse(paymentRequest, "SYSTEM_ERROR", e.getMessage());
        }
    }

    /**
     * Cancel payment via NPS API
     */
    public PaymentResponseDto cancelPayment(PaymentRequest paymentRequest) {
        logger.info("Cancelling payment via NPS API: {}", paymentRequest.getPaymentId());

        try {
            Map<String, Object> cancelPayload = Map.of(
                    "paymentId", paymentRequest.getPaymentId(),
                    "transactionId", paymentRequest.getTransactionId(),
                    "reason", "Cancelled by user"
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = npsWebClient
                    .post()
                    .uri("/api/v1/payments/cancel")
                    .headers(this::addAuthHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(cancelPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parsePaymentResponse(response, paymentRequest);

        } catch (WebClientResponseException e) {
            logger.error("NPS API cancellation error for payment {}: {}", paymentRequest.getPaymentId(), e.getMessage());
            return createErrorResponse(paymentRequest, "CANCEL_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error cancelling payment {}: {}", paymentRequest.getPaymentId(), e.getMessage());
            return createErrorResponse(paymentRequest, "SYSTEM_ERROR", e.getMessage());
        }
    }

    /**
     * Check payment status via NPS API
     */
    public PaymentResponseDto checkPaymentStatus(String paymentId) {
        logger.info("Checking payment status via NPS API: {}", paymentId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = npsWebClient
                    .get()
                    .uri("/api/v1/payments/{paymentId}/status", paymentId)
                    .headers(this::addAuthHeaders)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseStatusResponse(response, paymentId);

        } catch (WebClientResponseException e) {
            logger.error("NPS API status check error for payment {}: {}", paymentId, e.getMessage());
            return createStatusErrorResponse(paymentId, "STATUS_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error checking payment status {}: {}", paymentId, e.getMessage());
            return createStatusErrorResponse(paymentId, "SYSTEM_ERROR", e.getMessage());
        }
    }

    /**
     * Prepare payment payload for NPS API
     */
    private Map<String, Object> preparePaymentPayload(PaymentRequest paymentRequest) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("paymentId", paymentRequest.getPaymentId());
        payload.put("transactionId", paymentRequest.getTransactionId());
        payload.put("merchantId", npsConfig.getMerchantId());
        payload.put("senderAccount", paymentRequest.getSenderAccount());
        payload.put("receiverAccount", paymentRequest.getReceiverAccount());
        payload.put("amount", paymentRequest.getAmount().toString());
        payload.put("currency", paymentRequest.getCurrency());
        payload.put("paymentPurpose", paymentRequest.getPaymentPurpose() != null ? paymentRequest.getPaymentPurpose() : "");
        payload.put("paymentType", paymentRequest.getPaymentType().name());
        payload.put("referenceNumber", paymentRequest.getReferenceNumber() != null ? paymentRequest.getReferenceNumber() : "");
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("callbackUrl", npsConfig.getCallbackUrl() != null ? npsConfig.getCallbackUrl() : "");
        return payload;
    }

    /**
     * Add authentication headers
     */
    private void addAuthHeaders(HttpHeaders headers) {
        headers.add("X-Client-Id", npsConfig.getClientId());
        headers.add("X-Client-Secret", npsConfig.getClientSecret());
        headers.add("X-Merchant-Id", npsConfig.getMerchantId());
        
        // Add timestamp for request signing
        String timestamp = String.valueOf(System.currentTimeMillis());
        headers.add("X-Timestamp", timestamp);
        
        // Add signature if encryption is enabled
        if (npsConfig.getEnableEncryption()) {
            String signature = encryptionService.generateSignature(npsConfig.getClientId(), timestamp);
            headers.add("X-Signature", signature);
        }
    }

    /**
     * Parse payment response from NPS API
     */
    private PaymentResponseDto parsePaymentResponse(Map<String, Object> response, PaymentRequest paymentRequest) {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId(paymentRequest.getPaymentId());
        responseDto.setTransactionId(paymentRequest.getTransactionId());
        responseDto.setCreatedAt(LocalDateTime.now());

        if (response != null) {
            responseDto.setNpsReference((String) response.get("npsReference"));
            responseDto.setResponseCode((String) response.get("responseCode"));
            responseDto.setResponseMessage((String) response.get("responseMessage"));
            
            String status = (String) response.get("status");
            responseDto.setStatus(mapStringToStatus(status));
            
            if (responseDto.getStatus() == PaymentStatus.SUCCESS || responseDto.getStatus() == PaymentStatus.FAILED) {
                responseDto.setProcessedAt(LocalDateTime.now());
            }
        } else {
            responseDto.setStatus(PaymentStatus.FAILED);
            responseDto.setResponseCode("NO_RESPONSE");
            responseDto.setResponseMessage("No response received from NPS API");
        }

        return responseDto;
    }

    /**
     * Parse status response from NPS API
     */
    private PaymentResponseDto parseStatusResponse(Map<String, Object> response, String paymentId) {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId(paymentId);
        responseDto.setCreatedAt(LocalDateTime.now());

        if (response != null) {
            responseDto.setTransactionId((String) response.get("transactionId"));
            responseDto.setNpsReference((String) response.get("npsReference"));
            responseDto.setResponseCode((String) response.get("responseCode"));
            responseDto.setResponseMessage((String) response.get("responseMessage"));
            
            String status = (String) response.get("status");
            responseDto.setStatus(mapStringToStatus(status));
        } else {
            responseDto.setStatus(PaymentStatus.FAILED);
            responseDto.setResponseCode("NO_RESPONSE");
            responseDto.setResponseMessage("No response received from NPS API");
        }

        return responseDto;
    }

    /**
     * Map string status to PaymentStatus enum
     */
    private PaymentStatus mapStringToStatus(String status) {
        if (status == null) {
            return PaymentStatus.FAILED;
        }

        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED" -> PaymentStatus.SUCCESS;
            case "PENDING" -> PaymentStatus.PENDING;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            case "FAILED", "ERROR" -> PaymentStatus.FAILED;
            case "CANCELLED" -> PaymentStatus.CANCELLED;
            case "TIMEOUT" -> PaymentStatus.TIMEOUT;
            case "REJECTED" -> PaymentStatus.REJECTED;
            default -> PaymentStatus.FAILED;
        };
    }

    /**
     * Create error response for payment
     */
    private PaymentResponseDto createErrorResponse(PaymentRequest paymentRequest, String errorCode, String errorMessage) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(paymentRequest.getPaymentId());
        response.setTransactionId(paymentRequest.getTransactionId());
        response.setStatus(PaymentStatus.FAILED);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    /**
     * Create error response for status check
     */
    private PaymentResponseDto createStatusErrorResponse(String paymentId, String errorCode, String errorMessage) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(paymentId);
        response.setStatus(PaymentStatus.FAILED);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}
