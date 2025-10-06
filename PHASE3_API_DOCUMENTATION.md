# NPS Phase 3 Implementation - Real-time Alerting System API Documentation

## Overview

Phase 3 introduces a comprehensive **Real-time Alerting System** that monitors various metrics and triggers alerts based on configurable thresholds. The system provides proactive issue detection, intelligent alerting, and multi-channel notifications.

## Key Features

- **📊 Real-time Metrics Collection**: Transaction, system, and bank performance metrics
- **🎯 Intelligent Alert Rules**: Configurable conditions with flexible expressions
- **📱 Multi-channel Notifications**: Email, SMS, Slack, and Webhook support
- **🔔 Smart Alert Management**: Suppression, escalation, and acknowledgment
- **📈 Dashboard & Analytics**: Real-time alert monitoring and trends
- **🔄 Automated Recovery**: Automatic resolution and recovery notifications

---

## 🚨 Alert Management APIs

### Alert Rules Management

#### Get All Alert Rules
```http
GET /api/v1/alerts/rules
Authorization: Bearer {admin_token}
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "High Transaction Failure Rate",
    "description": "Alert when transaction success rate drops below 90%",
    "conditionExpression": "transaction_metrics.success_rate < 90",
    "severity": "CRITICAL",
    "notificationChannels": ["email", "slack", "sms"],
    "escalationPolicy": "immediate",
    "enabled": true,
    "evaluationIntervalSeconds": 60,
    "suppressionWindowSeconds": 300,
    "maxAlertsPerHour": 10,
    "metricType": "transaction",
    "metricName": "success_rate",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "createdBy": "ADMIN"
  }
]
```

#### Get Enabled Alert Rules
```http
GET /api/v1/alerts/rules/enabled
Authorization: Bearer {admin_token}
```

#### Create Alert Rule
```http
POST /api/v1/alerts/rules
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "High Processing Time",
  "description": "Alert when average processing time exceeds 15 seconds",
  "conditionExpression": "transaction_metrics.avg_processing_time_seconds > 15",
  "severity": "WARNING",
  "notificationChannels": ["email", "slack"],
  "escalationPolicy": "30m",
  "enabled": true,
  "evaluationIntervalSeconds": 120,
  "suppressionWindowSeconds": 600,
  "maxAlertsPerHour": 5,
  "metricType": "transaction",
  "metricName": "avg_processing_time_seconds"
}
```

#### Update Alert Rule
```http
PUT /api/v1/alerts/rules/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

#### Delete Alert Rule
```http
DELETE /api/v1/alerts/rules/{id}
Authorization: Bearer {admin_token}
```

#### Toggle Alert Rule (Enable/Disable)
```http
PATCH /api/v1/alerts/rules/{id}/toggle
Authorization: Bearer {admin_token}
```

---

### Alert Management

#### Get Alerts with Pagination
```http
GET /api/v1/alerts?page=0&size=20&sortBy=createdAt&sortDir=desc&status=ACTIVE&severity=CRITICAL
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "alertRuleId": 1,
      "name": "High Transaction Failure Rate",
      "message": "Transaction success rate dropped to 85.2%",
      "severity": "CRITICAL",
      "status": "ACTIVE",
      "notificationChannels": ["email", "slack", "sms"],
      "metricValue": 85.2,
      "thresholdValue": 90.0,
      "metricName": "success_rate",
      "metricType": "transaction",
      "createdAt": "2024-01-15T14:30:00",
      "acknowledgedAt": null,
      "acknowledgedBy": null,
      "resolvedAt": null,
      "resolvedBy": null,
      "lastNotificationSent": "2024-01-15T14:30:05",
      "notificationCount": 2,
      "escalatedAt": null,
      "context": "{\"total_transactions\": 1250, \"success_rate\": 85.2}"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### Get Active Alerts
```http
GET /api/v1/alerts/active
Authorization: Bearer {admin_token}
```

#### Get Alerts by Severity
```http
GET /api/v1/alerts/severity/{severity}
Authorization: Bearer {admin_token}
```

**Severity Values:** `CRITICAL`, `WARNING`, `INFO`

#### Get Alert by ID
```http
GET /api/v1/alerts/{id}
Authorization: Bearer {admin_token}
```

#### Acknowledge Alert
```http
PATCH /api/v1/alerts/{id}/acknowledge?acknowledgedBy=admin_user
Authorization: Bearer {admin_token}
```

#### Resolve Alert
```http
PATCH /api/v1/alerts/{id}/resolve?resolvedBy=admin_user
Authorization: Bearer {admin_token}
```

#### Suppress Alert
```http
PATCH /api/v1/alerts/{id}/suppress?suppressedBy=admin_user
Authorization: Bearer {admin_token}
```

#### Get Alert History
```http
GET /api/v1/alerts/{id}/history
Authorization: Bearer {admin_token}
```

**Response:**
```json
[
  {
    "id": 1,
    "alertId": 1,
    "action": "CREATED",
    "oldStatus": null,
    "newStatus": "ACTIVE",
    "userId": "SYSTEM",
    "details": "Alert triggered by rule evaluation",
    "createdAt": "2024-01-15T14:30:00"
  },
  {
    "id": 2,
    "alertId": 1,
    "action": "NOTIFICATION_SENT",
    "oldStatus": null,
    "newStatus": null,
    "userId": "SYSTEM",
    "details": "Notification sent via: email, slack, sms",
    "createdAt": "2024-01-15T14:30:05"
  },
  {
    "id": 3,
    "alertId": 1,
    "action": "ACKNOWLEDGED",
    "oldStatus": "ACTIVE",
    "newStatus": "ACKNOWLEDGED",
    "userId": "admin_user",
    "details": "Alert acknowledged",
    "createdAt": "2024-01-15T14:35:00"
  }
]
```

---

## 📊 Alert Dashboard & Analytics

#### Get Alert Dashboard Summary
```http
GET /api/v1/alerts/dashboard/summary
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "activeAlerts": {
    "CRITICAL": 2,
    "WARNING": 5,
    "INFO": 1
  },
  "todayStats": {
    "total": 15,
    "critical": 3,
    "warning": 8,
    "info": 4
  },
  "recentTrends": [
    ["2024-01-15", "CRITICAL", 3],
    ["2024-01-15", "WARNING", 8],
    ["2024-01-14", "CRITICAL", 1],
    ["2024-01-14", "WARNING", 5]
  ],
  "timestamp": "2024-01-15T15:30:00"
}
```

#### Get Recent Alert History
```http
GET /api/v1/alerts/history/recent?hours=24
Authorization: Bearer {admin_token}
```

---

## 🔧 Alert System Management

#### Manually Trigger Alert Evaluation
```http
POST /api/v1/alerts/evaluate
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "status": "success",
  "message": "Alert evaluation triggered successfully",
  "timestamp": "2024-01-15T15:30:00"
}
```

#### Test Notification Channels
```http
POST /api/v1/alerts/test-notification?channel=email
Authorization: Bearer {admin_token}
```

**Supported Channels:** `email`, `sms`, `slack`, `webhook`

**Response:**
```json
{
  "status": "success",
  "message": "Test notification sent via email",
  "timestamp": "2024-01-15T15:30:00"
}
```

---

## 📋 Alert Condition Expressions

### Supported Metric Paths

#### Transaction Metrics
- `transaction_metrics.success_rate` - Transaction success rate (%)
- `transaction_metrics.failure_rate` - Transaction failure rate (%)
- `transaction_metrics.timeout_rate` - Transaction timeout rate (%)
- `transaction_metrics.total_transactions` - Total transaction count
- `transaction_metrics.successful_transactions` - Successful transaction count
- `transaction_metrics.failed_transactions` - Failed transaction count
- `transaction_metrics.timeout_transactions` - Timeout transaction count
- `transaction_metrics.avg_processing_time_ms` - Average processing time (ms)
- `transaction_metrics.avg_processing_time_seconds` - Average processing time (seconds)
- `transaction_metrics.max_processing_time_ms` - Maximum processing time (ms)
- `transaction_metrics.min_processing_time_ms` - Minimum processing time (ms)
- `transaction_metrics.high_value_transactions` - High-value transaction count
- `transaction_metrics.total_volume` - Total transaction volume

#### System Metrics
- `system_metrics.cpu_usage` - CPU usage percentage
- `system_metrics.memory_usage` - Memory usage percentage
- `system_metrics.active_threads` - Active thread count
- `system_metrics.database_connections` - Database connection count
- `system_metrics.heap_memory_used` - Heap memory used (bytes)
- `system_metrics.heap_memory_max` - Maximum heap memory (bytes)
- `system_metrics.response_time_ms` - Average response time (ms)
- `system_metrics.error_rate` - Error rate percentage

#### Bank Metrics
- `bank_metrics.overall_success_rate` - Overall bank success rate (%)
- `bank_metrics.total_banks` - Total number of banks

### Supported Operators
- `<` - Less than
- `>` - Greater than
- `<=` - Less than or equal to
- `>=` - Greater than or equal to
- `==` - Equal to
- `!=` - Not equal to

### Example Conditions
```javascript
// Transaction success rate below 90%
"transaction_metrics.success_rate < 90"

// Processing time exceeds 10 seconds
"transaction_metrics.avg_processing_time_seconds > 10"

// CPU usage above 80%
"system_metrics.cpu_usage > 80"

// Memory usage above 85%
"system_metrics.memory_usage > 85"

// Bank success rate below 95%
"bank_metrics.overall_success_rate < 95"

// Low transaction volume (less than 50 transactions)
"transaction_metrics.total_transactions < 50"
```

---

## 🔔 Notification Channels

### Email Notifications
- **Subject Format:** `[SEVERITY] NPS Alert - {Alert Name}`
- **Content:** Detailed alert information with metric values and thresholds
- **Recipients:** Configurable per alert rule

### SMS Notifications
- **Format:** `[SEVERITY] NPS: {Alert Message}`
- **Length:** Truncated to fit SMS limits
- **Recipients:** Configurable phone numbers

### Slack Notifications
- **Format:** Rich message with color-coded severity
- **Content:** Alert details with metric information
- **Channels:** Configurable Slack channels per alert rule

### Webhook Notifications
- **Format:** JSON payload with complete alert information
- **Content:** Full alert context and metrics
- **Endpoints:** Configurable webhook URLs

---

## 📈 Alert Severity Levels

### CRITICAL (Priority 1)
- **Color:** Red (#FF0000)
- **Description:** Immediate action required
- **Examples:** Service down, high failure rates, system errors
- **Response Time:** Immediate notification

### WARNING (Priority 2)
- **Color:** Orange (#FFA500)
- **Description:** Monitor closely, potential issues
- **Examples:** Performance degradation, unusual patterns
- **Response Time:** Within 30 minutes

### INFO (Priority 3)
- **Color:** Blue (#0080FF)
- **Description:** Informational, business insights
- **Examples:** High-value transactions, volume changes
- **Response Time:** Within 2 hours

---

## 🎯 Alert Status Lifecycle

```
CREATED → ACTIVE → ACKNOWLEDGED → RESOLVED
    ↓         ↓           ↓
  EXPIRED  SUPPRESSED  REACTIVATED
```

### Status Descriptions
- **ACTIVE:** Alert is active and requires attention
- **ACKNOWLEDGED:** Alert has been acknowledged by team member
- **RESOLVED:** Alert has been resolved
- **SUPPRESSED:** Alert has been temporarily suppressed
- **EXPIRED:** Alert has expired due to timeout

---

## 🔧 Configuration Examples

### Sample Alert Rules

#### Critical Transaction Failure Alert
```json
{
  "name": "Critical Transaction Failure",
  "description": "Alert when transaction success rate drops below 85%",
  "conditionExpression": "transaction_metrics.success_rate < 85",
  "severity": "CRITICAL",
  "notificationChannels": ["email", "sms", "slack"],
  "escalationPolicy": "immediate",
  "enabled": true,
  "evaluationIntervalSeconds": 30,
  "suppressionWindowSeconds": 300,
  "maxAlertsPerHour": 5,
  "metricType": "transaction",
  "metricName": "success_rate"
}
```

#### System Performance Warning
```json
{
  "name": "High CPU Usage Warning",
  "description": "Alert when CPU usage exceeds 80%",
  "conditionExpression": "system_metrics.cpu_usage > 80",
  "severity": "WARNING",
  "notificationChannels": ["email", "slack"],
  "escalationPolicy": "15m",
  "enabled": true,
  "evaluationIntervalSeconds": 60,
  "suppressionWindowSeconds": 600,
  "maxAlertsPerHour": 10,
  "metricType": "system",
  "metricName": "cpu_usage"
}
```

#### Business Intelligence Alert
```json
{
  "name": "High-Value Transaction Detected",
  "description": "Alert when high-value transaction is processed",
  "conditionExpression": "transaction_metrics.high_value_transactions > 0",
  "severity": "INFO",
  "notificationChannels": ["email", "slack"],
  "escalationPolicy": "none",
  "enabled": true,
  "evaluationIntervalSeconds": 300,
  "suppressionWindowSeconds": 3600,
  "maxAlertsPerHour": 20,
  "metricType": "transaction",
  "metricName": "high_value_transactions"
}
```

---

## 🚀 Getting Started

### 1. Create Your First Alert Rule
```bash
curl -X POST "http://localhost:8081/api/v1/alerts/rules" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Custom Alert",
    "description": "Alert when success rate drops",
    "conditionExpression": "transaction_metrics.success_rate < 95",
    "severity": "WARNING",
    "notificationChannels": ["email", "slack"],
    "enabled": true,
    "metricType": "transaction",
    "metricName": "success_rate"
  }'
```

### 2. Check Active Alerts
```bash
curl -X GET "http://localhost:8081/api/v1/alerts/active" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 3. Test Notifications
```bash
curl -X POST "http://localhost:8081/api/v1/alerts/test-notification?channel=email" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### 4. View Dashboard
```bash
curl -X GET "http://localhost:8081/api/v1/alerts/dashboard/summary" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 📝 Best Practices

### Alert Rule Design
1. **Set Appropriate Thresholds:** Use realistic thresholds based on historical data
2. **Choose Right Severity:** Match severity to business impact
3. **Configure Suppression:** Prevent alert spam with appropriate suppression windows
4. **Select Channels:** Choose notification channels based on urgency

### Alert Management
1. **Acknowledge Promptly:** Acknowledge alerts to show they're being addressed
2. **Resolve Quickly:** Resolve alerts when issues are fixed
3. **Monitor Trends:** Use dashboard to identify patterns and trends
4. **Review Regularly:** Regularly review and tune alert rules

### Performance Considerations
1. **Evaluation Frequency:** Balance between responsiveness and system load
2. **Metric Collection:** Optimize metric collection intervals
3. **Database Indexing:** Ensure proper indexing for alert queries
4. **Notification Limits:** Set appropriate limits to prevent spam

---

This comprehensive alerting system provides enterprise-grade monitoring and notification capabilities for the NPS platform, enabling proactive issue detection and rapid response to critical events.
