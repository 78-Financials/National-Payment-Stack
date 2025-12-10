# 🚀 NPS Performance Testing & Optimization Guide

## Overview
This guide provides comprehensive performance testing strategies and optimization recommendations for the Nigerian Payment Stack (NPS) integration service.

## Performance Testing Strategy

### 1. Load Testing Scenarios

#### **Scenario 1: Normal Load**
- **Concurrent Users**: 100
- **Duration**: 30 minutes
- **Transaction Volume**: 1,000 transactions/minute
- **Expected Response Time**: < 2 seconds
- **Success Rate**: > 99%

#### **Scenario 2: Peak Load**
- **Concurrent Users**: 500
- **Duration**: 15 minutes
- **Transaction Volume**: 5,000 transactions/minute
- **Expected Response Time**: < 3 seconds
- **Success Rate**: > 98%

#### **Scenario 3: Stress Testing**
- **Concurrent Users**: 1,000
- **Duration**: 10 minutes
- **Transaction Volume**: 10,000 transactions/minute
- **Expected Response Time**: < 5 seconds
- **Success Rate**: > 95%

#### **Scenario 4: Endurance Testing**
- **Concurrent Users**: 200
- **Duration**: 8 hours
- **Transaction Volume**: 2,000 transactions/minute
- **Expected Response Time**: < 2 seconds
- **Success Rate**: > 99%

### 2. Performance Metrics

#### **Response Time Metrics**
- **API Response Time**: Average, 95th percentile, 99th percentile
- **Database Query Time**: Average query execution time
- **External Service Response Time**: NIBSS API response time
- **End-to-End Transaction Time**: Complete payment processing time

#### **Throughput Metrics**
- **Transactions Per Second (TPS)**: System throughput
- **Requests Per Second (RPS)**: API throughput
- **Database Transactions Per Second**: Database throughput
- **Network Throughput**: Network bandwidth utilization

#### **Resource Utilization Metrics**
- **CPU Usage**: Average and peak CPU utilization
- **Memory Usage**: Heap memory, non-heap memory
- **Database Connections**: Active and idle connections
- **Network I/O**: Network interface utilization
- **Disk I/O**: Disk read/write operations

#### **Error Metrics**
- **Error Rate**: Percentage of failed requests
- **Timeout Rate**: Percentage of timeout errors
- **Exception Rate**: Application exception frequency
- **Deadlock Rate**: Database deadlock frequency

### 3. Performance Testing Tools

#### **JMeter Configuration**
```xml
<!-- JMeter Test Plan for NPS Load Testing -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="NPS Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
          <elementProp name="API_KEY" elementType="Argument">
            <stringProp name="Argument.name">API_KEY</stringProp>
            <stringProp name="Argument.value">test_api_key_12345</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

#### **Gatling Configuration**
```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class NPSLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val paymentScenario = scenario("Payment Processing")
    .exec(http("Payment Request")
      .post("/api/v1/payments/transfer")
      .header("X-API-Key", "${api_key}")
      .body(StringBody("""
        {
          "messageId": "MSG${__Random(1000000, 9999999)}",
          "transactionId": "TST-${__Random(1000000, 9999999)}",
          "senderInstitutionCode": "001",
          "receiverInstitutionCode": "002",
          "senderAccountNumber": "${__Random(1000000000, 9999999999)}",
          "receiverAccountNumber": "${__Random(1000000000, 9999999999)}",
          "senderAccountName": "Test Sender",
          "receiverAccountName": "Test Receiver",
          "amount": 1000.00,
          "currency": "NGN",
          "narration": "Load test payment"
        }
      """))
      .check(status.is(200))
      .check(jsonPath("$.status").is("SUCCESS"))
    )

  setUp(
    paymentScenario.inject(
      rampUsers(100) during (30 seconds),
      constantUsers(100) during (5 minutes),
      rampUsers(0) during (30 seconds)
    )
  ).protocols(httpProtocol)
}
```

### 4. Performance Optimization Strategies

#### **Application-Level Optimizations**

##### **Database Optimizations**
```sql
-- Database Index Optimization
CREATE INDEX idx_payment_transactions_client_id ON payment_transactions_live(client_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions_live(status);
CREATE INDEX idx_payment_transactions_created_at ON payment_transactions_live(request_created_at);
CREATE INDEX idx_payment_transactions_transaction_id ON payment_transactions_live(transaction_id);

-- Query Optimization
EXPLAIN ANALYZE SELECT * FROM payment_transactions_live 
WHERE client_id = ? AND status = ? AND request_created_at > ?;

-- Connection Pool Optimization
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

##### **Caching Strategies**
```java
// Redis Caching Configuration
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

// Service Layer Caching
@Service
public class InternalClientRegistry {
    
    @Cacheable(value = "clients", key = "#clientId")
    public InternalClient getClientById(String clientId) {
        return clientRepository.findByClientId(clientId);
    }
    
    @CacheEvict(value = "clients", key = "#client.clientId")
    public void updateClient(InternalClient client) {
        clientRepository.save(client);
    }
}
```

##### **Asynchronous Processing**
```java
// Asynchronous Payment Processing
@Service
public class AsyncPaymentService {
    
    @Async("paymentTaskExecutor")
    public CompletableFuture<PaymentResponse> processPaymentAsync(PaymentRequest request) {
        // Process payment asynchronously
        return CompletableFuture.completedFuture(processPayment(request));
    }
    
    @Bean("paymentTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("payment-");
        executor.initialize();
        return executor;
    }
}
```

#### **Infrastructure Optimizations**

##### **JVM Tuning**
```bash
# JVM Performance Tuning Parameters
JAVA_OPTS="-Xms2g -Xmx4g \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+UseFastAccessorMethods \
-Dspring.profiles.active=production"
```

##### **Database Connection Pool Tuning**
```properties
# HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

##### **HTTP Client Optimization**
```java
// WebClient Configuration for High Performance
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .responseTimeout(Duration.ofSeconds(10))
                    .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                            .addHandlerLast(new WriteTimeoutHandler(10)))
            ))
            .build();
    }
}
```

### 5. Performance Monitoring

#### **Application Performance Monitoring (APM)**
```java
// Micrometer Metrics Configuration
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

// Custom Metrics
@Component
public class PaymentMetrics {
    
    private final Counter paymentCounter;
    private final Timer paymentTimer;
    private final Gauge activeTransactions;
    
    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.paymentCounter = Counter.builder("payments.total")
            .description("Total number of payments processed")
            .register(meterRegistry);
            
        this.paymentTimer = Timer.builder("payments.duration")
            .description("Payment processing duration")
            .register(meterRegistry);
            
        this.activeTransactions = Gauge.builder("payments.active")
            .description("Number of active transactions")
            .register(meterRegistry, this, PaymentMetrics::getActiveTransactionCount);
    }
    
    public void recordPayment() {
        paymentCounter.increment();
    }
    
    public void recordPaymentTime(Duration duration) {
        paymentTimer.record(duration);
    }
    
    private double getActiveTransactionCount() {
        // Return current active transaction count
        return getCurrentActiveTransactions();
    }
}
```

#### **Performance Dashboard**
```json
{
  "dashboard": {
    "title": "NPS Performance Dashboard",
    "panels": [
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "Average Response Time"
          }
        ]
      },
      {
        "title": "Throughput",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(payments_total[1m]) * 60",
            "legendFormat": "Transactions Per Minute"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{status!~\"2..\"}[5m]) / rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "Error Rate"
          }
        ]
      }
    ]
  }
}
```

### 6. Performance Testing Commands

#### **JMeter Load Testing**
```bash
# Run JMeter load test
jmeter -n -t nps_load_test.jmx -l results.jtl -e -o report/

# Run with specific parameters
jmeter -n -t nps_load_test.jmx \
  -Jthreads=100 \
  -Jduration=300 \
  -Jrampup=60 \
  -l results.jtl
```

#### **Gatling Load Testing**
```bash
# Run Gatling simulation
gatling.sh -s NPSLoadTest

# Run with specific configuration
gatling.sh -s NPSLoadTest -rf results/
```

#### **Artillery Load Testing**
```bash
# Run Artillery load test
artillery run nps_load_test.yml

# Run with specific configuration
artillery run nps_load_test.yml --output report.json
```

### 7. Performance Benchmarks

#### **Baseline Performance Targets**
- **API Response Time**: < 500ms (95th percentile)
- **Database Query Time**: < 100ms (average)
- **Throughput**: > 1,000 TPS
- **Error Rate**: < 0.1%
- **CPU Usage**: < 70% (average)
- **Memory Usage**: < 80% (heap)
- **Database Connections**: < 80% of pool

#### **Stress Test Limits**
- **Maximum Concurrent Users**: 1,000
- **Maximum TPS**: 5,000
- **Maximum Response Time**: 10 seconds
- **Maximum Error Rate**: 5%
- **Maximum CPU Usage**: 95%
- **Maximum Memory Usage**: 95%

### 8. Performance Optimization Checklist

#### **Application Optimizations**
- [ ] **Database Indexing**: Optimize database indexes
- [ ] **Query Optimization**: Optimize slow queries
- [ ] **Caching**: Implement Redis caching
- [ ] **Connection Pooling**: Optimize connection pools
- [ ] **Async Processing**: Implement async processing
- [ ] **Batch Processing**: Implement batch operations
- [ ] **Memory Management**: Optimize memory usage
- [ ] **Garbage Collection**: Tune GC parameters

#### **Infrastructure Optimizations**
- [ ] **Load Balancing**: Implement load balancing
- [ ] **Auto Scaling**: Configure auto scaling
- [ ] **Database Scaling**: Implement read replicas
- [ ] **CDN**: Implement content delivery network
- [ ] **Caching Layer**: Implement distributed caching
- [ ] **Network Optimization**: Optimize network configuration
- [ ] **Storage Optimization**: Optimize storage performance
- [ ] **Monitoring**: Implement comprehensive monitoring

### 9. Performance Testing Reports

#### **Test Report Template**
```markdown
# Performance Test Report

## Executive Summary
- **Test Duration**: 30 minutes
- **Peak TPS**: 2,500
- **Average Response Time**: 1.2 seconds
- **Error Rate**: 0.05%
- **System Status**: PASS

## Detailed Results
### Response Time Analysis
- **Average**: 1.2 seconds
- **95th Percentile**: 2.1 seconds
- **99th Percentile**: 3.5 seconds

### Throughput Analysis
- **Peak TPS**: 2,500
- **Sustained TPS**: 2,000
- **Total Transactions**: 3,600,000

### Resource Utilization
- **CPU Usage**: 65% (average), 85% (peak)
- **Memory Usage**: 70% (average), 80% (peak)
- **Database Connections**: 15/20 (75%)

## Recommendations
1. Implement Redis caching for frequently accessed data
2. Optimize database queries and indexes
3. Consider horizontal scaling for peak loads
4. Implement connection pooling optimization
```

## Conclusion

This performance testing guide provides comprehensive strategies for testing and optimizing the NPS integration service. Regular performance testing should be conducted to ensure the system meets performance requirements and can handle expected loads.

**Next Steps:**
1. Implement performance monitoring
2. Conduct baseline performance testing
3. Optimize identified bottlenecks
4. Establish performance SLAs
5. Implement automated performance testing
