# Missing APIs Implementation Plan

## 📋 **Overview**

This document outlines the comprehensive plan for implementing the missing APIs identified during the NPS system review. The implementation is prioritized based on business impact and frontend requirements.

---

## 🎯 **Implementation Timeline**

### **Phase 1: Critical APIs (Weeks 1-2)**
- Authentication & User Management APIs
- Advanced Analytics & Reporting APIs
- Integration & Webhook Management APIs
- System Monitoring & Health APIs

### **Phase 2: Supporting Services (Weeks 3-4)**
- Service layer implementations
- Database entities and repositories
- Background job processing
- Testing and validation

### **Phase 3: Integration & Testing (Weeks 5-6)**
- Frontend integration
- End-to-end testing
- Performance optimization
- Documentation completion

---

## 🔐 **1. Authentication & User Management APIs**

### **Priority: CRITICAL**
**Impact**: Essential for frontend authentication and user management

### **APIs to Implement:**

#### **AuthController.java** ✅ Created
```http
POST /api/v1/auth/login                    # Client login
POST /api/v1/auth/refresh                  # Token refresh
GET  /api/v1/auth/profile                  # Get user profile
PUT  /api/v1/auth/profile                  # Update user profile
POST /api/v1/auth/change-password          # Change password
POST /api/v1/auth/forgot-password          # Request password reset
POST /api/v1/auth/reset-password           # Reset password with token
POST /api/v1/auth/logout                   # Logout
```

### **Required Services:**
- `AuthService.java` - JWT token management
- `UserService.java` - User profile management
- `PasswordResetService.java` - Password reset functionality

### **Required DTOs:** ✅ Created
- `LoginRequestDto.java`
- `LoginResponseDto.java`
- `UserProfileDto.java`
- `ChangePasswordRequestDto.java`

### **Database Changes:**
- Add `users` table for admin users
- Add `password_reset_tokens` table
- Add `user_sessions` table for session management

---

## 📊 **2. Advanced Analytics & Reporting APIs**

### **Priority: HIGH**
**Impact**: Essential for business intelligence and reporting

### **APIs to Implement:**

#### **ReportController.java** ✅ Created
```http
POST /api/v1/admin/reports/generate        # Generate custom report
GET  /api/v1/admin/reports/export/{id}     # Export report
GET  /api/v1/admin/reports/templates       # Get report templates
POST /api/v1/admin/reports/schedule        # Schedule report
GET  /api/v1/admin/reports/scheduled       # Get scheduled reports
DELETE /api/v1/admin/reports/scheduled/{id} # Cancel scheduled report
GET  /api/v1/admin/reports/history         # Get report history
GET  /api/v1/admin/reports/metrics/realtime # Real-time metrics
GET  /api/v1/admin/reports/metrics/performance # Performance metrics
```

### **Required Services:**
- `ReportService.java` - Report generation and management
- `ReportSchedulerService.java` - Scheduled report processing
- `ReportTemplateService.java` - Report template management
- `MetricsCollectionService.java` - Enhanced metrics collection

### **Required DTOs:**
- `ReportRequestDto.java`
- `ReportResponseDto.java`
- `ReportTemplateDto.java`
- `ScheduledReportDto.java`

### **Database Changes:**
- Add `reports` table
- Add `report_templates` table
- Add `scheduled_reports` table
- Add `report_history` table

---

## 🔗 **3. Integration & Webhook Management APIs**

### **Priority: HIGH**
**Impact**: Essential for external system integrations

### **APIs to Implement:**

#### **IntegrationController.java** ✅ Created
```http
GET  /api/v1/admin/integrations/webhooks           # Get webhook configs
POST /api/v1/admin/integrations/webhooks           # Create webhook
PUT  /api/v1/admin/integrations/webhooks/{id}      # Update webhook
DELETE /api/v1/admin/integrations/webhooks/{id}    # Delete webhook
POST /api/v1/admin/integrations/webhooks/{id}/test # Test webhook
GET  /api/v1/admin/integrations/webhooks/{id}/deliveries # Webhook deliveries
POST /api/v1/admin/integrations/webhooks/{id}/deliveries/{deliveryId}/retry # Retry delivery
GET  /api/v1/admin/integrations/rate-limits        # Get rate limits
PUT  /api/v1/admin/integrations/rate-limits/{clientId} # Update rate limits
GET  /api/v1/admin/integrations/rate-limits/{clientId}/usage # Get API usage
GET  /api/v1/admin/integrations/third-party        # Get third-party integrations
POST /api/v1/admin/integrations/third-party        # Create third-party integration
POST /api/v1/admin/integrations/third-party/{id}/test # Test integration
```

### **Required Services:**
- `IntegrationService.java` - Webhook and integration management
- `WebhookDeliveryService.java` - Webhook delivery tracking
- `RateLimitService.java` - API rate limiting
- `ThirdPartyIntegrationService.java` - External system integrations

### **Required DTOs:** ✅ Created
- `WebhookConfigDto.java`
- `IntegrationConfigDto.java`
- `RateLimitConfigDto.java`
- `WebhookDeliveryDto.java`

### **Database Changes:**
- Add `webhook_configurations` table
- Add `webhook_deliveries` table
- Add `rate_limit_configurations` table
- Add `third_party_integrations` table

---

## 🖥️ **4. System Monitoring & Health APIs**

### **Priority: MEDIUM**
**Impact**: Important for system administration and monitoring

### **APIs to Implement:**

#### **SystemController.java** ✅ Created
```http
GET  /api/v1/admin/system/health              # System health status
GET  /api/v1/admin/system/metrics             # System metrics
GET  /api/v1/admin/system/performance         # Performance metrics
GET  /api/v1/admin/system/database/health     # Database health
GET  /api/v1/admin/system/external-services/health # External services health
GET  /api/v1/admin/system/logs                # System logs
GET  /api/v1/admin/system/logs/search         # Search logs
GET  /api/v1/admin/system/logs/statistics     # Log statistics
GET  /api/v1/admin/system/logs/export         # Export logs
GET  /api/v1/admin/system/alerts              # System alerts
POST /api/v1/admin/system/alerts/{id}/acknowledge # Acknowledge alert
GET  /api/v1/admin/system/configuration       # System configuration
PUT  /api/v1/admin/system/configuration       # Update configuration
```

### **Required Services:**
- `SystemMonitoringService.java` - System health monitoring
- `LogAggregationService.java` - Log collection and search
- `PerformanceMonitoringService.java` - Performance metrics
- `HealthCheckService.java` - Health check endpoints

### **Required DTOs:**
- `SystemHealthDto.java`
- `SystemMetricsDto.java`
- `LogEntryDto.java`
- `SystemAlertDto.java`

### **Database Changes:**
- Add `system_metrics` table
- Add `log_entries` table
- Add `system_alerts` table
- Add `health_checks` table

---

## 🛠️ **Implementation Details**

### **1. Service Layer Architecture**

#### **Base Service Pattern**
```java
@Service
public abstract class BaseService<T, ID> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected abstract Repository<T, ID> getRepository();
    
    public T findById(ID id) {
        return getRepository().findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
    }
    
    public List<T> findAll() {
        return getRepository().findAll();
    }
    
    public T save(T entity) {
        return getRepository().save(entity);
    }
    
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }
}
```

#### **Authentication Service Implementation**
```java
@Service
public class AuthService {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private InternalClientRegistry clientRegistry;
    
    public LoginResponseDto login(LoginRequestDto request) {
        // Validate client credentials
        InternalClient client = clientRegistry.getClientByApiKey(request.getApiKey());
        if (client == null || !client.getClientId().equals(request.getClientId())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Generate JWT token
        String token = tokenProvider.generateToken(client);
        String refreshToken = tokenProvider.generateRefreshToken(client);
        
        return new LoginResponseDto(
            token,
            client.getClientId(),
            client.getClientName(),
            client.getClientType().name(),
            getClientPermissions(client),
            tokenProvider.getExpirationDate(token)
        );
    }
    
    public Map<String, String> refreshToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }
        
        String clientId = tokenProvider.getClientIdFromToken(token);
        InternalClient client = clientRegistry.getClientById(clientId);
        
        String newToken = tokenProvider.generateToken(client);
        return Map.of(
            "token", newToken,
            "expiresAt", tokenProvider.getExpirationDate(newToken).toString()
        );
    }
}
```

### **2. Database Schema Updates**

#### **Users Table**
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES internal_clients(client_id)
);
```

#### **Webhook Configurations Table**
```sql
CREATE TABLE webhook_configurations (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    api_key VARCHAR(255),
    event_types JSON NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    retry_attempts INT DEFAULT 3,
    timeout_seconds INT DEFAULT 30,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### **System Metrics Table**
```sql
CREATE TABLE system_metrics (
    id VARCHAR(36) PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_unit VARCHAR(20),
    tags JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_metric_name_timestamp (metric_name, timestamp)
);
```

### **3. Background Job Processing**

#### **Report Generation Job**
```java
@Component
public class ReportGenerationJob {
    
    @Autowired
    private ReportService reportService;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processScheduledReports() {
        List<ScheduledReport> scheduledReports = reportService.getDueReports();
        
        for (ScheduledReport report : scheduledReports) {
            try {
                reportService.generateScheduledReport(report);
            } catch (Exception e) {
                logger.error("Failed to generate scheduled report: {}", report.getId(), e);
            }
        }
    }
}
```

#### **Webhook Delivery Job**
```java
@Component
public class WebhookDeliveryJob {
    
    @Autowired
    private WebhookDeliveryService webhookDeliveryService;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void processWebhookDeliveries() {
        List<WebhookDelivery> pendingDeliveries = webhookDeliveryService.getPendingDeliveries();
        
        for (WebhookDelivery delivery : pendingDeliveries) {
            try {
                webhookDeliveryService.processDelivery(delivery);
            } catch (Exception e) {
                logger.error("Failed to process webhook delivery: {}", delivery.getId(), e);
            }
        }
    }
}
```

### **4. Testing Strategy**

#### **Unit Tests**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private InternalClientRegistry clientRegistry;
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void login_WithValidCredentials_ReturnsLoginResponse() {
        // Given
        LoginRequestDto request = new LoginRequestDto("CLIENT123", "api_key");
        InternalClient client = new InternalClient();
        client.setClientId("CLIENT123");
        client.setClientName("Test Client");
        
        when(clientRegistry.getClientByApiKey("api_key")).thenReturn(client);
        when(tokenProvider.generateToken(client)).thenReturn("jwt_token");
        
        // When
        LoginResponseDto response = authService.login(request);
        
        // Then
        assertThat(response.getClientId()).isEqualTo("CLIENT123");
        assertThat(response.getToken()).isEqualTo("jwt_token");
    }
}
```

#### **Integration Tests**
```java
@SpringBootTest
@AutoConfigureTestDatabase
class AuthControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void login_WithValidCredentials_ReturnsSuccess() {
        // Given
        LoginRequestDto request = new LoginRequestDto("CLIENT123", "valid_api_key");
        
        // When
        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
            "/api/v1/auth/login", request, LoginResponseDto.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getClientId()).isEqualTo("CLIENT123");
    }
}
```

---

## 📈 **Performance Considerations**

### **1. Caching Strategy**
- **Redis** for session management and rate limiting
- **In-memory caching** for frequently accessed data
- **Database query caching** for report templates

### **2. Database Optimization**
- **Indexing** on frequently queried columns
- **Partitioning** for large tables (logs, metrics)
- **Connection pooling** for database connections

### **3. Async Processing**
- **Message queues** for webhook deliveries
- **Background jobs** for report generation
- **Event-driven architecture** for real-time updates

---

## 🔒 **Security Considerations**

### **1. Authentication & Authorization**
- **JWT token** validation and refresh
- **Role-based access control** (RBAC)
- **API key** management and rotation

### **2. Data Protection**
- **Input validation** and sanitization
- **SQL injection** prevention
- **XSS protection** for web interfaces

### **3. Audit & Compliance**
- **Comprehensive logging** of all operations
- **Audit trail** for sensitive operations
- **Data retention** policies

---

## 📊 **Monitoring & Observability**

### **1. Metrics Collection**
- **Application metrics** (response times, error rates)
- **Business metrics** (transaction volumes, success rates)
- **System metrics** (CPU, memory, disk usage)

### **2. Logging Strategy**
- **Structured logging** with JSON format
- **Log aggregation** and search capabilities
- **Log retention** and archival policies

### **3. Alerting**
- **Real-time alerts** for critical issues
- **Escalation procedures** for different severity levels
- **Alert correlation** to reduce noise

---

## 🚀 **Deployment Strategy**

### **1. Environment Setup**
- **Development** environment for testing
- **Staging** environment for integration testing
- **Production** environment with monitoring

### **2. CI/CD Pipeline**
- **Automated testing** on code commits
- **Automated deployment** to staging
- **Manual approval** for production deployment

### **3. Rollback Strategy**
- **Database migration** rollback procedures
- **Application rollback** capabilities
- **Monitoring** for deployment success

---

## 📋 **Success Criteria**

### **1. Functional Requirements**
- ✅ All missing APIs implemented and tested
- ✅ Frontend integration completed
- ✅ Performance requirements met
- ✅ Security requirements satisfied

### **2. Non-Functional Requirements**
- **Response time** < 500ms for API calls
- **Availability** > 99.9% uptime
- **Scalability** support for 1000+ concurrent users
- **Maintainability** comprehensive documentation

### **3. Business Value**
- **Improved user experience** with complete functionality
- **Enhanced monitoring** and alerting capabilities
- **Better integration** with external systems
- **Comprehensive reporting** and analytics

---

This implementation plan provides a comprehensive roadmap for implementing all missing APIs while ensuring high quality, security, and performance standards. The phased approach allows for incremental delivery and testing, reducing risk and ensuring successful implementation.
