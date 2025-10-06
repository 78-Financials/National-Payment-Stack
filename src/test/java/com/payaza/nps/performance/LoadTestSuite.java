package com.payaza.nps.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance and load tests
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class LoadTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalClientRepository clientRepository;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        // Setup test client for load testing
        testClient = new InternalClient();
        testClient.setClientId("LOAD_TEST_BANK");
        testClient.setClientName("Load Test Bank");
        testClient.setApiKey("load_test_api_key_12345");
        testClient.setActive(true);
        testClient.setTransactionPrefix("LDT");
        testClient.setRateLimit(10000); // High rate limit for load testing
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(testClient);
    }

    @Test
    void concurrentPaymentRequests_ShouldHandleLoad() throws Exception {
        // Given
        int numberOfRequests = 100;
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // When
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfRequests];
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    Pacs008RequestDto request = createPaymentRequest(requestId);
                    mockMvc.perform(post("/api/v1/payments/transfer")
                            .header("X-API-Key", testClient.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double requestsPerSecond = (double) numberOfRequests / (totalTime / 1000.0);

        // Then
        System.out.println("Load Test Results:");
        System.out.println("Total Requests: " + numberOfRequests);
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Requests per Second: " + String.format("%.2f", requestsPerSecond));
        
        // Assertions - adjust based on expected performance
        assert requestsPerSecond >= 10.0 : "Performance below expected threshold";
        
        executor.shutdown();
    }

    @Test
    void highVolumeIdentificationRequests_ShouldHandleLoad() throws Exception {
        // Given
        int numberOfRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // When
        CompletableFuture<Void>[] futures = new CompletableFuture[numberOfRequests];
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // Create identification request
                    String requestBody = String.format(
                        "{\"messageId\":\"MSG%d\",\"accountNumber\":\"%d\",\"bankCode\":\"001\"}",
                        requestId, 1000000000 + requestId
                    );
                    
                    mockMvc.perform(post("/api/v1/identification/verify")
                            .header("X-API-Key", testClient.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).get(20, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double requestsPerSecond = (double) numberOfRequests / (totalTime / 1000.0);

        // Then
        System.out.println("Identification Load Test Results:");
        System.out.println("Total Requests: " + numberOfRequests);
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Requests per Second: " + String.format("%.2f", requestsPerSecond));
        
        assert requestsPerSecond >= 5.0 : "Identification performance below expected threshold";
        
        executor.shutdown();
    }

    @Test
    void memoryUsage_ShouldRemainStable() throws Exception {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When - Perform multiple operations
        for (int i = 0; i < 1000; i++) {
            Pacs008RequestDto request = createPaymentRequest(i);
            try {
                mockMvc.perform(post("/api/v1/payments/transfer")
                        .header("X-API-Key", testClient.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            } catch (Exception e) {
                // Expected for some requests due to validation
            }
        }
        
        // Force garbage collection
        System.gc();
        Thread.sleep(1000);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Then
        System.out.println("Memory Usage Test Results:");
        System.out.println("Initial Memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final Memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory Increase: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // Memory increase should be reasonable (less than 100MB)
        assert memoryIncrease < 100 * 1024 * 1024 : "Memory usage increase too high: " + (memoryIncrease / 1024 / 1024) + " MB";
    }

    @Test
    void responseTime_ShouldMeetSLA() throws Exception {
        // Given
        Pacs008RequestDto request = createPaymentRequest(1);
        int numberOfRequests = 20;
        long totalResponseTime = 0;
        long maxResponseTime = 0;
        
        // When
        for (int i = 0; i < numberOfRequests; i++) {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/v1/payments/transfer")
                    .header("X-API-Key", testClient.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            totalResponseTime += responseTime;
            maxResponseTime = Math.max(maxResponseTime, responseTime);
            
            // Update request for next iteration
            request.setTransactionId("LDT-" + System.currentTimeMillis());
            request.setMessageId("MSG" + System.currentTimeMillis());
        }
        
        // Then
        double averageResponseTime = (double) totalResponseTime / numberOfRequests;
        
        System.out.println("Response Time Test Results:");
        System.out.println("Average Response Time: " + String.format("%.2f", averageResponseTime) + "ms");
        System.out.println("Maximum Response Time: " + maxResponseTime + "ms");
        
        // SLA: Average response time should be less than 2 seconds
        assert averageResponseTime < 2000 : "Average response time exceeds SLA: " + averageResponseTime + "ms";
        assert maxResponseTime < 5000 : "Maximum response time exceeds SLA: " + maxResponseTime + "ms";
    }

    private Pacs008RequestDto createPaymentRequest(int requestId) {
        Pacs008RequestDto request = new Pacs008RequestDto();
        request.setMessageId("MSG" + requestId + System.currentTimeMillis());
        request.setTransactionId("LDT-" + requestId + System.currentTimeMillis());
        request.setSenderInstitutionCode("001");
        request.setReceiverInstitutionCode("002");
        request.setSenderAccountNumber("123456789" + (requestId % 10));
        request.setReceiverAccountNumber("987654321" + (requestId % 10));
        request.setSenderAccountName("Load Test Sender " + requestId);
        request.setReceiverAccountName("Load Test Receiver " + requestId);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("NGN");
        request.setNarration("Load test payment " + requestId);
        return request;
    }
}
