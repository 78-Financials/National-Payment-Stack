# 📚 NPS Technical Documentation

## Table of Contents
1. [System Architecture](#system-architecture)
2. [API Documentation](#api-documentation)
3. [Database Schema](#database-schema)
4. [Security Implementation](#security-implementation)
5. [Deployment Guide](#deployment-guide)
6. [Configuration Reference](#configuration-reference)
7. [Troubleshooting Guide](#troubleshooting-guide)
8. [Development Guide](#development-guide)

## System Architecture

### Overview
The Nigerian Payment Stack (NPS) Integration Service is a Spring Boot application that provides a RESTful API wrapper around NIBSS ISO 20022 payment messages. It handles payment processing, identification verification, and provides comprehensive analytics and monitoring capabilities.

### Architecture Diagram
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │   Admin Portal  │    │   Monitoring    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway / Load Balancer                 │
└─────────────────────────────────────────────────────────────────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                     NPS Integration Service                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │   REST API  │ │  Security   │ │  Analytics  │ │  Alerts  │ │
│  │  Controllers│ │   Layer     │ │   Engine    │ │  Engine  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │   Business  │ │   XML       │ │   Audit     │ │  Cache   │ │
│  │   Services  │ │ Processing  │ │   Service   │ │  Layer   │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
└─────────────────────────────────────────────────────────────────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Layer                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │ PostgreSQL  │ │    Redis    │ │   File      │ │   Logs   │ │
│  │  Database   │ │    Cache    │ │  Storage    │ │ Storage  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
└─────────────────────────────────────────────────────────────────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    External Systems                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │   NIBSS     │ │   Email     │ │    SMS      │ │  Slack   │ │
│  │   Switch    │ │   Service   │ │   Service   │ │  Webhook │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 14+
- **Cache**: Redis 6+
- **Build Tool**: Maven 3.8+
- **Container**: Docker & Kubernetes
- **Monitoring**: Micrometer, Prometheus, Grafana
- **Security**: Spring Security, OAuth2, JWT

## API Documentation

### Base URL
```
Production: https://nps-api.payaza.com
Staging:    https://nps-staging-api.payaza.com
Development: http://localhost:8080
```

### Authentication
All API endpoints (except callbacks) require authentication using API keys:

```http
X-API-Key: your_api_key_here
```

### Core Endpoints

#### Payment Processing

##### **POST /api/v1/payments/transfer**
Process a payment transfer request.

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "transactionId": "BAN-123456789",
  "senderInstitutionCode": "001",
  "receiverInstitutionCode": "002",
  "senderAccountNumber": "1234567890",
  "receiverAccountNumber": "0987654321",
  "senderAccountName": "John Doe",
  "receiverAccountName": "Jane Smith",
  "amount": 1000.00,
  "currency": "NGN",
  "narration": "Payment for services"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "transactionId": "BAN-123456789",
  "status": "SUCCESS",
  "responseCode": "00",
  "responseMessage": "Payment processed successfully",
  "processedAt": "2024-01-15T10:30:00Z"
}
```

##### **POST /api/v1/identification/verify**
Verify account identification.

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "accountNumber": "1234567890",
  "bankCode": "001"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "responseCode": "00",
  "responseMessage": "Account verified successfully",
  "status": "SUCCESS",
  "accountVerified": true,
  "accountName": "John Doe",
  "bankCode": "001",
  "processedAt": "2024-01-15T10:30:00Z"
}
```

### Admin Endpoints

#### Client Management

##### **GET /api/v1/admin/clients**
Get all internal clients.

**Response:**
```json
[
  {
    "id": 1,
    "clientId": "BANK001",
    "clientName": "First Bank",
    "apiKey": "fb_api_key_12345",
    "active": true,
    "transactionPrefix": "FB",
    "rateLimit": 1000,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
]
```

##### **POST /api/v1/admin/clients**
Create a new internal client.

**Request Body:**
```json
{
  "clientId": "NEW_BANK",
  "clientName": "New Bank",
  "transactionPrefix": "NB",
  "rateLimit": 500
}
```

##### **PUT /api/v1/admin/clients/{clientId}**
Update an existing client.

##### **DELETE /api/v1/admin/clients/{clientId}**
Delete a client.

##### **POST /api/v1/admin/clients/{clientId}/regenerate-api-key**
Regenerate API key for a client.

### Analytics Endpoints

#### **GET /api/v1/analytics/dashboard**
Get dashboard metrics.

**Response:**
```json
{
  "totalTransactions": 15000,
  "successfulTransactions": 14850,
  "failedTransactions": 150,
  "successRate": 99.0,
  "averageResponseTime": 1200,
  "totalVolume": 15000000.00,
  "activeClients": 25,
  "topPerformingBanks": [
    {
      "bankCode": "001",
      "bankName": "First Bank",
      "successRate": 99.5,
      "transactionCount": 5000
    }
  ]
}
```

#### **GET /api/v1/analytics/transactions**
Get transaction analytics with filtering.

**Query Parameters:**
- `startDate`: Start date (ISO 8601)
- `endDate`: End date (ISO 8601)
- `clientId`: Filter by client ID
- `status`: Filter by status
- `page`: Page number
- `size`: Page size

### Alert Management

#### **GET /api/v1/alerts/rules**
Get all alert rules.

#### **POST /api/v1/alerts/rules**
Create a new alert rule.

**Request Body:**
```json
{
  "name": "High Error Rate",
  "description": "Alert when error rate exceeds 5%",
  "metricName": "error_rate",
  "condition": "GREATER_THAN",
  "threshold": 5.0,
  "severity": "WARNING",
  "enabled": true,
  "evaluationInterval": 300
}
```

#### **GET /api/v1/alerts/active**
Get active alerts.

#### **POST /api/v1/alerts/{alertId}/acknowledge**
Acknowledge an alert.

### Callback Endpoints

#### **POST /api/v1/callbacks/nibss**
Receive callbacks from NIBSS (no authentication required).

**Request Body:**
```xml
<!-- ISO 20022 XML Message -->
```

## Database Schema

### Core Tables

#### **internal_clients**
```sql
CREATE TABLE internal_clients (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(50) UNIQUE NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    transaction_prefix VARCHAR(10),
    rate_limit INTEGER DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **payment_transactions_live**
```sql
CREATE TABLE payment_transactions_live (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    original_message_id VARCHAR(100),
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_bank VARCHAR(10),
    creditor_bank VARCHAR(10),
    debtor_account VARCHAR(50),
    creditor_account VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    failure_reason VARCHAR(255),
    request_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_received_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **payment_transactions_history**
```sql
CREATE TABLE payment_transactions_history (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    original_message_id VARCHAR(100),
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_bank VARCHAR(10),
    creditor_bank VARCHAR(10),
    debtor_account VARCHAR(50),
    creditor_account VARCHAR(50),
    status VARCHAR(20),
    failure_reason VARCHAR(255),
    request_created_at TIMESTAMP,
    response_received_at TIMESTAMP,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **audit_logs**
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    client_id VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    message TEXT,
    details TEXT,
    error_code VARCHAR(20),
    error_message TEXT,
    execution_time_ms BIGINT,
    session_id VARCHAR(100)
);
```

#### **alert_rules**
```sql
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    metric_name VARCHAR(100) NOT NULL,
    condition VARCHAR(20) NOT NULL,
    threshold DECIMAL(10,2) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    evaluation_interval INTEGER DEFAULT 300,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **alerts**
```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT REFERENCES alert_rules(id),
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    title VARCHAR(200) NOT NULL,
    message TEXT,
    metric_value DECIMAL(10,2),
    threshold_value DECIMAL(10,2),
    notification_count INTEGER DEFAULT 0,
    last_notification_at TIMESTAMP,
    acknowledged_by VARCHAR(50),
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Indexes
```sql
-- Performance indexes
CREATE INDEX idx_payment_live_client_id ON payment_transactions_live(client_id);
CREATE INDEX idx_payment_live_status ON payment_transactions_live(status);
CREATE INDEX idx_payment_live_created_at ON payment_transactions_live(request_created_at);
CREATE INDEX idx_payment_live_transaction_id ON payment_transactions_live(transaction_id);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_client_id ON audit_logs(client_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

CREATE INDEX idx_alerts_rule_id ON alerts(rule_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
```

## Security Implementation

### Authentication & Authorization
- **API Key Authentication**: All API endpoints require valid API keys
- **Role-Based Access Control**: Admin endpoints require admin role
- **Client Isolation**: Clients can only access their own data
- **Rate Limiting**: Per-client rate limiting to prevent abuse

### Data Protection
- **Encryption in Transit**: All communications use TLS 1.2+
- **Encryption at Rest**: Database encryption enabled
- **XML Security**: XML Digital Signatures and Encryption
- **Sensitive Data Masking**: PII data masked in logs and responses

### Security Headers
```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .and()
            )
            .build();
    }
}
```

## Deployment Guide

### Docker Deployment

#### **Dockerfile**
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/nps-integration-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **Docker Compose**
```yaml
version: '3.8'
services:
  nps-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/nps_db
      - SPRING_DATASOURCE_USERNAME=nps_user
      - SPRING_DATASOURCE_PASSWORD=nps_password
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=nps_db
      - POSTGRES_USER=nps_user
      - POSTGRES_PASSWORD=nps_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### Kubernetes Deployment

#### **Deployment YAML**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nps-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nps-service
  template:
    metadata:
      labels:
        app: nps-service
    spec:
      containers:
      - name: nps-service
        image: nps-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: nps-secrets
              key: database-url
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

## Configuration Reference

### Application Properties

#### **Database Configuration**
```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/nps_db
spring.datasource.username=nps_user
spring.datasource.password=nps_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### **Redis Configuration**
```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.timeout=2000ms
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
```

#### **NPS Configuration**
```properties
# NIBSS Configuration
nps.client-id=your_client_id
nps.client-secret=your_client_secret
nps.merchant-id=your_merchant_id
nps.base-url=https://nibss.com
nps.timeout-seconds=30

# Endpoints
nps.acmt023-endpoint=/acmt023
nps.acmt024-endpoint=/acmt024
nps.pacs008-endpoint=/pacs008
nps.pacs002-endpoint=/pacs002
nps.pacs028-endpoint=/pacs028
nps.participants-endpoint=/participants
```

#### **Security Configuration**
```properties
# Security Configuration
spring.security.user.name=admin
spring.security.user.password=admin_password
spring.security.user.roles=ADMIN

# CORS Configuration
cors.allowed-origins=https://admin.payaza.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
```

#### **Monitoring Configuration**
```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Logging Configuration
logging.level.com.payaza.nps=INFO
logging.level.org.springframework.security=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## Troubleshooting Guide

### Common Issues

#### **1. Database Connection Issues**
**Problem**: Cannot connect to PostgreSQL database
**Solution**: 
- Verify database is running
- Check connection string and credentials
- Ensure network connectivity
- Check firewall settings

#### **2. API Key Authentication Failures**
**Problem**: API requests returning 401 Unauthorized
**Solution**:
- Verify API key is correct
- Check client is active
- Ensure proper header format: `X-API-Key: your_key`

#### **3. XML Processing Errors**
**Problem**: XML signature/encryption failures
**Solution**:
- Verify XML format compliance
- Check certificate validity
- Ensure proper namespace declarations
- Validate XML schema

#### **4. Performance Issues**
**Problem**: Slow response times or high memory usage
**Solution**:
- Check database query performance
- Monitor memory usage and GC
- Verify connection pool settings
- Check for memory leaks

#### **5. Alert System Issues**
**Problem**: Alerts not triggering or notifications not sent
**Solution**:
- Verify alert rules configuration
- Check metrics collection
- Test notification channels
- Verify evaluation intervals

### Log Analysis

#### **Application Logs**
```bash
# View application logs
tail -f logs/nps-service.log

# Search for errors
grep "ERROR" logs/nps-service.log

# Search for specific client
grep "clientId.*BANK001" logs/nps-service.log
```

#### **Database Logs**
```sql
-- Check slow queries
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Check connection usage
SELECT count(*) as active_connections 
FROM pg_stat_activity 
WHERE state = 'active';
```

### Health Checks

#### **Application Health**
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics

# Check database connectivity
curl http://localhost:8080/actuator/health/db
```

#### **Database Health**
```sql
-- Check database size
SELECT pg_size_pretty(pg_database_size('nps_db'));

-- Check table sizes
SELECT schemaname,tablename,pg_size_pretty(size) 
FROM (
    SELECT schemaname,tablename,pg_total_relation_size(schemaname||'.'||tablename) as size
    FROM pg_tables 
    WHERE schemaname = 'public'
) t 
ORDER BY size DESC;
```

## Development Guide

### Setting Up Development Environment

#### **Prerequisites**
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

#### **Local Setup**
```bash
# Clone repository
git clone https://github.com/payaza/nps-integration-service.git
cd nps-integration-service

# Install dependencies
mvn clean install

# Set up database
createdb nps_db
psql nps_db < src/main/resources/schema.sql

# Start Redis
redis-server

# Run application
mvn spring-boot:run
```

### Code Structure

```
src/
├── main/
│   ├── java/com/payaza/nps/
│   │   ├── annotation/          # Custom annotations
│   │   ├── aspect/              # AOP aspects
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── filter/              # Servlet filters
│   │   ├── model/               # JPA entities
│   │   ├── repository/          # Data repositories
│   │   ├── security/            # Security configuration
│   │   ├── service/             # Business logic services
│   │   ├── validation/          # Validation logic
│   │   └── NigerianPaymentStackApplication.java
│   └── resources/
│       ├── application.properties
│       ├── db/migration/        # Flyway migrations
│       └── static/              # Static resources
└── test/
    ├── java/com/payaza/nps/     # Test classes
    └── resources/
        └── application-test.properties
```

### Coding Standards

#### **Java Coding Standards**
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods small and focused
- Use appropriate design patterns

#### **Spring Boot Best Practices**
- Use `@Service`, `@Repository`, `@Controller` annotations
- Implement proper exception handling
- Use `@Transactional` for database operations
- Follow dependency injection principles
- Use configuration properties for external configuration

#### **Database Best Practices**
- Use parameterized queries to prevent SQL injection
- Create appropriate indexes for performance
- Use transactions appropriately
- Follow database naming conventions
- Implement proper data validation

### Testing Guidelines

#### **Unit Testing**
- Test all public methods
- Use mocking for dependencies
- Aim for 80%+ code coverage
- Test both success and failure scenarios

#### **Integration Testing**
- Test complete workflows
- Use test database
- Test with real data scenarios
- Verify external integrations

#### **Performance Testing**
- Test with realistic data volumes
- Monitor resource usage
- Test concurrent operations
- Verify response time requirements

### Deployment Process

#### **Development to Production**
1. **Development**: Code changes in feature branches
2. **Testing**: Automated tests and manual testing
3. **Staging**: Deploy to staging environment
4. **UAT**: User acceptance testing
5. **Production**: Deploy to production with monitoring

#### **Release Process**
1. Create release branch from main
2. Update version numbers
3. Run full test suite
4. Create deployment artifacts
5. Deploy to staging for final testing
6. Deploy to production
7. Monitor deployment and rollback if needed

## Conclusion

This technical documentation provides comprehensive information about the NPS Integration Service architecture, APIs, database schema, security implementation, deployment procedures, and development guidelines. It serves as a reference for developers, administrators, and operations teams working with the system.

For additional support or questions, please contact the development team or refer to the project repository for the latest updates and documentation.
