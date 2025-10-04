package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.config.NpsConfiguration;
import com.payaza.nps.dto.PaymentRequestDto;
import com.payaza.nps.dto.PaymentResponseDto;
import com.payaza.nps.model.PaymentType;
import com.payaza.nps.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NpsConfiguration npsConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testProcessPayment_Success() throws Exception {
        // Given
        PaymentRequestDto requestDto = createValidPaymentRequest();
        PaymentResponseDto responseDto = createSuccessResponse();

        when(paymentService.processPayment(any(PaymentRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY123456789"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.responseCode").value("00"));
    }

    @Test
    void testProcessPayment_InvalidRequest() throws Exception {
        // Given
        PaymentRequestDto requestDto = createInvalidPaymentRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    private PaymentRequestDto createValidPaymentRequest() {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentId("PAY123456789");
        requestDto.setTransactionId("TXN123456789");
        requestDto.setSenderAccount("1234567890");
        requestDto.setReceiverAccount("0987654321");
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setCurrency("NGN");
        requestDto.setPaymentPurpose("Test payment");
        requestDto.setPaymentType(PaymentType.TRANSFER);
        requestDto.setReferenceNumber("REF123456");
        return requestDto;
    }

    private PaymentRequestDto createInvalidPaymentRequest() {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        // Missing required fields to trigger validation error
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setCurrency("NGN");
        return requestDto;
    }

    private PaymentResponseDto createSuccessResponse() {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setPaymentId("PAY123456789");
        responseDto.setTransactionId("TXN123456789");
        responseDto.setStatus(com.payaza.nps.model.PaymentStatus.SUCCESS);
        responseDto.setResponseCode("00");
        responseDto.setResponseMessage("Payment successful");
        responseDto.setAmount(new BigDecimal("1000.00"));
        responseDto.setCurrency("NGN");
        return responseDto;
    }
}
