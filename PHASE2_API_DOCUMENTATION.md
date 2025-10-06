# NPS Phase 2 Implementation - API Documentation

## 🚀 EOD Processing & Historical Analytics with Kubernetes Support

### Overview

Phase 2 extends the NPS integration service with:
- **Distributed Lock Service** for Kubernetes pod coordination
- **End-of-Day Processing** with automatic data archival
- **Historical Analytics** for comprehensive reporting
- **Enhanced Dashboard** with real-time and historical insights

---

## 📊 Real-time Analytics Endpoints

### 1. Real-time Dashboard Metrics
```http
GET /api/v1/admin/analytics/dashboard/realtime
```

**Response:**
```json
{
  "totalTransactions": 1247,
  "successfulTransactions": 1198,
  "failedTransactions": 23,
  "pendingTransactions": 26,
  "timeoutTransactions": 0,
  "successRate": 96.1,
  "averageProcessingTimeMs": 2340.5
}
```

### 2. Live Transactions Feed
```http
GET /api/v1/admin/analytics/dashboard/live-transactions
```

**Response:**
```json
[
  {
    "transactionId": "BAN-987654321",
    "clientId": "BAN",
    "amount": 50000.00,
    "currency": "NGN",
    "creditorBank": "GTBank",
    "status": "SUCCESS",
    "processingTimeMs": 1200,
    "requestCreatedAt": "2025-10-06T02:15:30"
  }
]
```

### 3. Today's Bank Performance
```http
GET /api/v1/admin/analytics/dashboard/bank-performance
```

**Response:**
```json
[
  ["GTBank", 342, 338, 98.8, 890],
  ["FirstBank", 289, 284, 98.3, 1120],
  ["Access Bank", 198, 192, 97.0, 1450]
]
```

---

## 📚 Historical Analytics Endpoints

### 1. Transaction Volume Trends
```http
GET /api/v1/admin/analytics/historical/volume-trends?startDate=2024-01-01&endDate=2024-01-31&period=DAILY
```

**Parameters:**
- `startDate`: Start date (ISO format: YYYY-MM-DD)
- `endDate`: End date (ISO format: YYYY-MM-DD)
- `period`: DAILY, WEEKLY, or MONTHLY

### 2. Bank Performance Analysis
```http
GET /api/v1/admin/analytics/historical/bank-performance?startDate=2024-01-01&endDate=2024-01-31
```

**Response:**
```json
[
  ["GTBank", 3420, 3380, 98.83, 890, 25000000.00],
  ["FirstBank", 2890, 2845, 98.44, 1120, 22000000.00]
]
```

### 3. Bank Success Rates
```http
GET /api/v1/admin/analytics/historical/bank-success-rates?startDate=2024-01-01&endDate=2024-01-31&minTransactions=100
```

### 4. Top Performing Banks
```http
GET /api/v1/admin/analytics/historical/top-performing-banks?startDate=2024-01-01&endDate=2024-01-31&minVolume=50
```

### 5. Client Activity Analysis
```http
GET /api/v1/admin/analytics/historical/client-activity?startDate=2024-01-01&endDate=2024-01-31
```

### 6. Error Analysis
```http
GET /api/v1/admin/analytics/historical/error-analysis?startDate=2024-01-01&endDate=2024-01-31
```

### 7. Hourly Transaction Patterns
```http
GET /api/v1/admin/analytics/historical/hourly-patterns?startDate=2024-01-01&endDate=2024-01-31
```

**Response:**
```json
[
  [9, 1250, 2340.5],
  [10, 1890, 2450.2],
  [11, 2100, 2230.8]
]
```

### 8. Currency Distribution
```http
GET /api/v1/admin/analytics/historical/currency-distribution?startDate=2024-01-01&endDate=2024-01-31
```

### 9. Amount Distribution
```http
GET /api/v1/admin/analytics/historical/amount-distribution?startDate=2024-01-01&endDate=2024-01-31
```

### 10. Comprehensive Dashboard Metrics
```http
GET /api/v1/admin/analytics/historical/dashboard-metrics?startDate=2024-01-01&endDate=2024-01-31
```

**Response:**
```json
{
  "dateRange": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  },
  "transactionMetrics": {
    "totalTransactions": 15420,
    "successfulTransactions": 14856,
    "failedTransactions": 324,
    "timeoutTransactions": 240,
    "successRate": 96.34,
    "failureRate": 2.10,
    "timeoutRate": 1.56
  },
  "financialMetrics": {
    "totalAmount": 125000000.50,
    "averageTransactionAmount": 8103.25
  },
  "performanceMetrics": {
    "averageProcessingTimeMs": 2340.5,
    "averageProcessingTimeSeconds": 2.34
  }
}
```

---

## 🕐 EOD Processing Endpoints

### 1. EOD Processing Status
```http
GET /api/v1/admin/analytics/eod/status
```

**Response:**
```json
{
  "lastProcessedDate": "2024-01-31",
  "hasPerformanceData": true,
  "isLocked": false,
  "podId": "nps-pod-1-abc123",
  "timestamp": "2025-10-06T10:30:00"
}
```

### 2. Manual EOD Trigger
```http
POST /api/v1/admin/analytics/eod/trigger?targetDate=2024-01-31
```

**Response:**
```json
{
  "message": "EOD processing completed for date: 2024-01-31",
  "targetDate": "2024-01-31",
  "timestamp": 1640995200000
}
```

---

## 🔒 Distributed Locking (Kubernetes-Safe)

### How It Works

1. **Lock Acquisition**: Only one pod can acquire the EOD processing lock
2. **Lock Expiration**: Locks expire after 2 hours to prevent deadlocks
3. **Automatic Cleanup**: Expired locks are automatically cleaned up
4. **Pod Identification**: Each pod has a unique identifier for tracking

### Lock Flow
```
Pod A → acquireLock("EOD_PROCESSING_LOCK", 120 minutes, 30 seconds) ✅
Pod B → acquireLock("EOD_PROCESSING_LOCK", 120 minutes, 30 seconds) ❌ [Lock exists]
Pod C → acquireLock("EOD_PROCESSING_LOCK", 120 minutes, 30 seconds) ❌ [Lock exists]

Pod A → processEndOfDay(targetDate)
Pod A → releaseLock("EOD_PROCESSING_LOCK") ✅
```

---

## 📊 Transaction Analysis Endpoints

### 1. High-Value Transactions
```http
GET /api/v1/admin/analytics/transactions/high-value?threshold=100000
```

### 2. Slow Transactions
```http
GET /api/v1/admin/analytics/transactions/slow?thresholdMs=5000
```

### 3. Failed Transactions
```http
GET /api/v1/admin/analytics/transactions/failed?page=0&size=20
```

### 4. Client Transactions
```http
GET /api/v1/admin/analytics/client/{clientId}/transactions
```

### 5. Transaction Details
```http
GET /api/v1/admin/analytics/transaction/{transactionId}
```

---

## 🗄️ Database Schema

### New Tables

#### 1. `distributed_locks`
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

#### 2. `payment_transactions_live`
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

#### 3. `payment_transactions_history`
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

#### 4. `bank_performance_daily`
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

---

## ⚙️ Configuration

### Application Properties
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Scheduling Configuration
spring.task.scheduling.pool.size=2
spring.task.execution.pool.core-size=2
spring.task.execution.pool.max-size=4

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nps-integration-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nps-integration-service
  template:
    metadata:
      labels:
        app: nps-integration-service
    spec:
      containers:
      - name: nps-service
        image: nps-integration-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          value: "jdbc:postgresql://postgres:5432/nps_db"
        - name: JPA_DDL_AUTO
          value: "validate"
```

---

## 🔄 EOD Processing Flow

### Automatic Processing
1. **Scheduled Check**: Every hour, check if EOD processing is needed
2. **Lock Acquisition**: Acquire distributed lock for EOD processing
3. **Data Archival**: Move completed transactions to history table
4. **Performance Aggregation**: Generate daily bank performance metrics
5. **Cleanup**: Remove old live transactions
6. **Lock Release**: Release distributed lock

### Manual Processing
1. **Manual Trigger**: Admin triggers EOD for specific date
2. **Lock Acquisition**: Same distributed lock mechanism
3. **Processing**: Same archival and aggregation process
4. **Lock Release**: Release lock when complete

---

## 📈 Analytics Capabilities

### Real-time Analytics
- Live transaction monitoring
- Current day performance metrics
- Real-time success/failure rates
- Active transaction tracking

### Historical Analytics
- Volume trends over time
- Bank performance comparison
- Client activity analysis
- Error pattern identification
- Processing time analysis
- Currency and amount distributions

### Performance Metrics
- Success rates by bank
- Average processing times
- Transaction volume patterns
- Error categorization
- Client activity tracking

---

## 🚀 Deployment Notes

### Prerequisites
1. **PostgreSQL Database** with proper permissions
2. **Flyway Migration Support** enabled
3. **Kubernetes Cluster** (for distributed locking)
4. **Admin Authentication** configured

### Environment Variables
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/nps_db
DATABASE_USERNAME=nps_user
DATABASE_PASSWORD=nps_password
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Connectivity**: Automatic validation
- **EOD Processing Status**: `/api/v1/admin/analytics/eod/status`
- **System Metrics**: `/actuator/metrics`

---

## 🎯 Key Features

✅ **Kubernetes-Safe EOD Processing** - Distributed locking prevents conflicts  
✅ **Comprehensive Analytics** - Real-time and historical insights  
✅ **Automatic Data Archival** - Efficient storage management  
✅ **Bank Performance Tracking** - Detailed metrics and trends  
✅ **Error Analysis** - Pattern identification and categorization  
✅ **Audit Logging** - Complete operation tracking  
✅ **Production-Ready** - Optimized for high-volume transactions  

---

## 📞 Support

For issues or questions about Phase 2 implementation:
1. Check the audit logs for detailed operation tracking
2. Monitor the EOD processing status endpoint
3. Verify distributed lock status in database
4. Review application logs for error details

The system is now ready for production deployment with comprehensive analytics and Kubernetes-safe operations! 🎉
