# NPS Integration Service - Phase 2 Implementation

## 🚀 EOD Processing & Historical Analytics with Kubernetes Support

### Overview

Phase 2 extends the Nigerian Payment Stack (NPS) integration service with comprehensive analytics, automated end-of-day processing, and Kubernetes-safe distributed operations. This implementation provides real-time monitoring, historical reporting, and intelligent data management for high-volume payment processing.

---

## ✨ Key Features

### 🔒 **Kubernetes-Safe Distributed Operations**
- **Distributed Lock Service** prevents multiple pods from running EOD simultaneously
- **Pod Identification** with unique hostname-based IDs for tracking
- **Lock Expiration** and automatic cleanup to prevent deadlocks
- **Audit Logging** for all distributed operations

### 📊 **Comprehensive Analytics Platform**
- **Real-time Dashboard** with live transaction monitoring
- **Historical Analytics** with configurable date ranges and periods
- **Bank Performance Analysis** with success rates and processing times
- **Client Activity Tracking** and error pattern identification
- **Volume Trends** (daily, weekly, monthly) and transaction patterns

### 🕐 **Intelligent EOD Processing**
- **Automatic Archival** of completed transactions to history tables
- **Bank Performance Aggregation** with daily metrics calculation
- **Batch Processing** for handling large transaction volumes
- **Scheduled Operations** with intelligent detection of processing needs
- **Manual Triggers** for specific date processing

### 🗄️ **Optimized Data Management**
- **Live Transaction Tracking** for current day operations
- **Historical Archive** for long-term analytics and reporting
- **Performance-Optimized Queries** with strategic indexing
- **Flyway Migrations** for version-controlled database schema

---

## 🏗️ Architecture

### Data Flow Architecture
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PACS.008      │───▶│  Live Tracking   │───▶│  Real-time      │
│   Requests      │    │  (Current Day)   │    │  Dashboard      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PACS.002      │───▶│  Status Updates  │───▶│  Live Status    │
│   Callbacks     │    │  & Completion    │    │  Tracking       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   EOD           │───▶│  Data Archival   │───▶│  Historical     │
│   Processing    │    │  & Aggregation   │    │  Analytics      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Kubernetes Coordination
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Pod A         │───▶│  Distributed     │───▶│  EOD Processing │
│   (Leader)      │    │  Lock Service    │    │  (Only Pod A)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
┌─────────────────┐             │             ┌─────────────────┐
│   Pod B         │─────────────┼────────────▶│  Pod B          │
│   (Follower)    │             │             │  (Standby)      │
└─────────────────┘             │             └─────────────────┘
                                │
┌─────────────────┐             │             ┌─────────────────┐
│   Pod C         │─────────────┼────────────▶│  Pod C          │
│   (Follower)    │             │             │  (Standby)      │
└─────────────────┘             │             └─────────────────┘
                                │
                                ▼
                    ┌──────────────────┐
                    │  Database        │
                    │  Lock Table      │
                    └──────────────────┘
```

---

## 📊 Database Schema

### Core Tables

#### 1. **`payment_transactions_live`** - Real-time Tracking
```sql
CREATE TABLE payment_transactions_live (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    original_message_id VARCHAR(100) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    creditor_bank VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processing_time_ms BIGINT,
    request_created_at TIMESTAMP NOT NULL,
    response_received_at TIMESTAMP
);
```

#### 2. **`payment_transactions_history`** - Historical Archive
```sql
CREATE TABLE payment_transactions_history (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    original_message_id VARCHAR(100) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    final_status VARCHAR(20) NOT NULL,
    processing_time_ms BIGINT,
    request_created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. **`bank_performance_daily`** - Aggregated Metrics
```sql
CREATE TABLE bank_performance_daily (
    id BIGSERIAL PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    total_requests INTEGER NOT NULL DEFAULT 0,
    successful_requests INTEGER NOT NULL DEFAULT 0,
    failed_requests INTEGER NOT NULL DEFAULT 0,
    success_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    avg_processing_time_ms BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_bank_date UNIQUE (bank_code, date)
);
```

#### 4. **`distributed_locks`** - Kubernetes Coordination
```sql
CREATE TABLE distributed_locks (
    id BIGSERIAL PRIMARY KEY,
    lock_name VARCHAR(100) NOT NULL UNIQUE,
    pod_id VARCHAR(200) NOT NULL,
    application_name VARCHAR(100) NOT NULL,
    acquired_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    lock_data TEXT
);
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **PostgreSQL 12+**
- **Maven 3.6+**
- **Kubernetes Cluster** (for production deployment)

### Local Development Setup

1. **Clone and Build**
```bash
git clone <repository-url>
cd nps
mvn clean install
```

2. **Database Setup**
```bash
# Create PostgreSQL database
createdb nps_db

# Run Flyway migrations
mvn flyway:migrate
```

3. **Configuration**
```bash
# Copy and edit configuration
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Update database connection details
# Update NPS API credentials
```

4. **Start Application**
```bash
mvn spring-boot:run
```

5. **Access Services**
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Analytics**: http://localhost:8080/api/v1/admin/analytics/dashboard/realtime

### Kubernetes Deployment

1. **Create Namespace**
```bash
kubectl create namespace nps
```

2. **Deploy Configuration**
```bash
kubectl apply -f kubernetes-deployment.yaml
```

3. **Verify Deployment**
```bash
kubectl get pods -n nps
kubectl get services -n nps
```

4. **Access Services**
```bash
# Port forward for testing
kubectl port-forward -n nps svc/nps-service 8080:8080

# Access via ingress (if configured)
curl https://nps.yourdomain.com/actuator/health
```

---

## 📊 Analytics Dashboard

### Real-time Metrics
- **Transaction Volume**: Live count of current day transactions
- **Success Rates**: Real-time success/failure ratios
- **Processing Times**: Average response times by bank
- **Active Transactions**: Pending transaction monitoring

### Historical Analytics
- **Volume Trends**: Daily, weekly, monthly transaction patterns
- **Bank Performance**: Comparative analysis across banks
- **Client Activity**: Per-client transaction analysis
- **Error Patterns**: Categorized error analysis
- **Processing Metrics**: Performance trends over time

### Sample Dashboard Response
```json
{
  "transactionMetrics": {
    "totalTransactions": 15420,
    "successfulTransactions": 14856,
    "successRate": 96.34,
    "averageProcessingTimeMs": 2340.5
  },
  "financialMetrics": {
    "totalAmount": 125000000.50,
    "averageTransactionAmount": 8103.25
  }
}
```

---

## 🔄 EOD Processing Flow

### Automatic Processing (Every Hour)
```
1. Scheduled Check → Should EOD run for yesterday?
2. Lock Acquisition → Only one pod can acquire lock
3. Data Validation → Check for completed transactions
4. Archival Process → Move live → history tables
5. Aggregation → Calculate bank performance metrics
6. Cleanup → Remove old live transactions
7. Lock Release → Free up for other operations
```

### Manual Processing
```
1. Admin Trigger → Manual EOD for specific date
2. Lock Acquisition → Same distributed lock mechanism
3. Processing → Same archival and aggregation
4. Lock Release → Complete operation
```

---

## 🛠️ API Endpoints

### Real-time Analytics
- `GET /api/v1/admin/analytics/dashboard/realtime` - Live metrics
- `GET /api/v1/admin/analytics/dashboard/live-transactions` - Current transactions
- `GET /api/v1/admin/analytics/dashboard/bank-performance` - Today's bank metrics

### Historical Analytics
- `GET /api/v1/admin/analytics/historical/volume-trends` - Transaction volume trends
- `GET /api/v1/admin/analytics/historical/bank-performance` - Bank performance analysis
- `GET /api/v1/admin/analytics/historical/client-activity` - Client activity analysis
- `GET /api/v1/admin/analytics/historical/error-analysis` - Error pattern analysis

### EOD Management
- `GET /api/v1/admin/analytics/eod/status` - EOD processing status
- `POST /api/v1/admin/analytics/eod/trigger` - Manual EOD trigger

### Transaction Analysis
- `GET /api/v1/admin/analytics/transactions/high-value` - High-value transactions
- `GET /api/v1/admin/analytics/transactions/slow` - Slow processing transactions
- `GET /api/v1/admin/analytics/transactions/failed` - Failed transactions

---

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/nps_db
DATABASE_USERNAME=nps_user
DATABASE_PASSWORD=nps_password

# NPS API Configuration
NPS_CLIENT_ID=your-nps-client-id
NPS_CLIENT_SECRET=your-nps-client-secret
NPS_MERCHANT_ID=your-bank-code

# Admin Configuration
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
ADMIN_API_KEY=admin_api_key_99999
```

### Application Properties
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Scheduling Configuration
spring.task.scheduling.pool.size=2
spring.task.execution.pool.core-size=2

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

---

## 📈 Performance Optimizations

### Database Indexing
- **Composite indexes** for complex analytics queries
- **Date-based partitioning** strategies for large datasets
- **Optimized query patterns** for real-time and historical analytics

### Caching Strategy
- **In-memory caching** for frequently accessed data
- **Redis integration** for distributed caching (future enhancement)
- **Query result caching** for expensive analytics operations

### Batch Processing
- **Batch operations** for EOD data archival
- **Chunked processing** for large transaction volumes
- **Async processing** for non-blocking operations

---

## 🔍 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Connectivity**: Automatic validation
- **EOD Processing Status**: `/api/v1/admin/analytics/eod/status`
- **System Metrics**: `/actuator/metrics`

### Logging
- **Structured Logging** with JSON format
- **Audit Trail** for all operations
- **Error Tracking** with detailed context
- **Performance Metrics** logging

### Metrics
- **Prometheus Integration** for metrics collection
- **Custom Metrics** for business KPIs
- **Grafana Dashboards** for visualization (future enhancement)

---

## 🚨 Troubleshooting

### Common Issues

#### 1. **EOD Processing Not Running**
```bash
# Check EOD status
curl -u admin:admin123 http://localhost:8080/api/v1/admin/analytics/eod/status

# Check for distributed locks
SELECT * FROM distributed_locks WHERE lock_name = 'EOD_PROCESSING_LOCK';
```

#### 2. **Database Connection Issues**
```bash
# Check database connectivity
curl -u admin:admin123 http://localhost:8080/actuator/health

# Verify database configuration
kubectl logs -n nps deployment/nps-integration-service
```

#### 3. **Performance Issues**
```bash
# Check system metrics
curl -u admin:admin123 http://localhost:8080/actuator/metrics

# Review database performance
SELECT * FROM pg_stat_activity WHERE datname = 'nps_db';
```

### Debug Commands
```bash
# View application logs
kubectl logs -n nps deployment/nps-integration-service -f

# Check pod status
kubectl get pods -n nps -o wide

# Verify services
kubectl get svc -n nps

# Check ingress
kubectl get ingress -n nps
```

---

## 🎯 Production Checklist

### Pre-Deployment
- [ ] **Database Migrations** tested and verified
- [ ] **Environment Variables** configured securely
- [ ] **SSL Certificates** configured for HTTPS
- [ ] **Resource Limits** set appropriately
- [ ] **Health Checks** configured and tested

### Post-Deployment
- [ ] **EOD Processing** scheduled and running
- [ ] **Analytics Endpoints** responding correctly
- [ ] **Distributed Locking** working across pods
- [ ] **Database Performance** optimized
- [ ] **Monitoring** configured and alerting

### Security
- [ ] **API Authentication** enabled and tested
- [ ] **Database Credentials** secured with secrets
- [ ] **Network Policies** configured
- [ ] **RBAC** permissions set appropriately
- [ ] **Audit Logging** enabled and monitored

---

## 🔮 Future Enhancements

### Phase 3 Possibilities
- **Real-time Alerting** for performance issues
- **Advanced Visualizations** with charts and graphs
- **Predictive Analytics** using machine learning
- **Export Capabilities** (PDF, Excel reports)
- **API Rate Limiting** and advanced security
- **Multi-tenant Support** for different organizations
- **WebSocket Integration** for real-time updates

### Integration Opportunities
- **Grafana Dashboards** for visualization
- **Elasticsearch** for advanced search and analytics
- **Redis** for distributed caching
- **Kafka** for event streaming
- **Prometheus** for advanced monitoring

---

## 📞 Support & Contributing

### Getting Help
1. **Check Documentation**: Review this README and API documentation
2. **Review Logs**: Check application and audit logs for details
3. **Monitor Metrics**: Use health checks and metrics endpoints
4. **Community Support**: Engage with the development team

### Contributing
1. **Fork Repository**: Create your own fork
2. **Create Branch**: Use feature branches for development
3. **Submit PR**: Create pull request with detailed description
4. **Code Review**: Participate in code review process

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🎉 Conclusion

The NPS Integration Service Phase 2 implementation provides a comprehensive analytics platform with Kubernetes-safe operations, intelligent data management, and production-ready features. The system is designed to handle high-volume payment processing with real-time monitoring, historical analytics, and automated operations.

**Ready for production deployment with enterprise-grade features!** 🚀

---

*For more detailed information, see the [API Documentation](PHASE2_API_DOCUMENTATION.md) and [Kubernetes Deployment Guide](kubernetes-deployment.yaml).*
