# NPS Phase 3 Implementation - Real-time Alerting System

## 🚨 Overview

Phase 3 introduces a comprehensive **Real-time Alerting System** that provides proactive monitoring, intelligent alerting, and multi-channel notifications for the Nigerian Payment Stack platform. This system enables operations teams to detect and respond to issues before they impact customers.

## 🎯 Key Features

### 📊 **Real-time Metrics Collection**
- **Transaction Metrics**: Success rates, processing times, volumes, error patterns
- **System Health Metrics**: CPU usage, memory consumption, database connections
- **Bank Performance Metrics**: Individual bank success rates and response times
- **Business Intelligence Metrics**: High-value transactions, unusual patterns

### 🎯 **Intelligent Alert Rules Engine**
- **Flexible Condition Expressions**: Support for complex metric-based conditions
- **Multiple Severity Levels**: CRITICAL, WARNING, INFO with different response times
- **Smart Suppression**: Prevent alert spam with configurable suppression windows
- **Escalation Policies**: Automatic escalation based on time and severity

### 📱 **Multi-channel Notifications**
- **Email Notifications**: Detailed alert information with metric context
- **SMS Alerts**: Critical alerts with immediate delivery
- **Slack Integration**: Rich messages with color-coded severity
- **Webhook Support**: JSON payloads for custom integrations

### 🔔 **Advanced Alert Management**
- **Alert Lifecycle**: ACTIVE → ACKNOWLEDGED → RESOLVED workflow
- **Audit Trail**: Complete history of all alert actions and changes
- **Suppression Control**: Temporary suppression for maintenance windows
- **Recovery Notifications**: Automatic notifications when issues are resolved

### 📈 **Dashboard & Analytics**
- **Real-time Dashboard**: Live view of active alerts and system health
- **Alert Trends**: Historical analysis of alert patterns and resolution times
- **Performance Metrics**: Alert response times and team performance
- **Custom Reports**: Configurable reporting for different stakeholders

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Metrics       │───▶│  Alert Engine    │───▶│  Notification   │
│   Collection    │    │  & Rules Engine  │    │  System         │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Transaction   │    │  Alert Rules     │    │  Email/SMS/     │
│   Monitoring    │    │  Configuration   │    │  Slack/Webhook  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   System        │    │  Threshold       │    │  Alert History  │
│   Health        │    │  Management      │    │  & Escalation   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 📋 Components

### **Data Models**
- **`AlertRule`**: Configuration for alert conditions and thresholds
- **`Alert`**: Individual alert instances with lifecycle management
- **`AlertHistory`**: Complete audit trail of all alert actions
- **`AlertSeverity`**: CRITICAL, WARNING, INFO severity levels
- **`AlertStatus`**: ACTIVE, ACKNOWLEDGED, RESOLVED, SUPPRESSED states

### **Core Services**
- **`MetricsCollectionService`**: Real-time metrics gathering from various sources
- **`AlertEngine`**: Rules evaluation and alert triggering logic
- **`NotificationService`**: Multi-channel notification delivery
- **`AlertHistoryService`**: Audit trail and history management

### **REST Controllers**
- **`AlertController`**: Complete alert management API
- **Alert Rules Management**: CRUD operations for alert rules
- **Alert Management**: Acknowledgment, resolution, suppression
- **Dashboard & Analytics**: Real-time monitoring and reporting

## 🔧 Configuration

### **Alert Condition Expressions**

The system supports flexible condition expressions using metric paths:

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
```

### **Supported Metric Paths**

#### Transaction Metrics
- `transaction_metrics.success_rate` - Transaction success rate (%)
- `transaction_metrics.failure_rate` - Transaction failure rate (%)
- `transaction_metrics.timeout_rate` - Transaction timeout rate (%)
- `transaction_metrics.total_transactions` - Total transaction count
- `transaction_metrics.avg_processing_time_ms` - Average processing time (ms)
- `transaction_metrics.avg_processing_time_seconds` - Average processing time (seconds)
- `transaction_metrics.high_value_transactions` - High-value transaction count

#### System Metrics
- `system_metrics.cpu_usage` - CPU usage percentage
- `system_metrics.memory_usage` - Memory usage percentage
- `system_metrics.active_threads` - Active thread count
- `system_metrics.database_connections` - Database connection count
- `system_metrics.response_time_ms` - Average response time (ms)
- `system_metrics.error_rate` - Error rate percentage

#### Bank Metrics
- `bank_metrics.overall_success_rate` - Overall bank success rate (%)
- `bank_metrics.total_banks` - Total number of banks

### **Default Alert Rules**

The system comes with pre-configured alert rules:

1. **High Transaction Failure Rate** (CRITICAL)
   - Condition: `transaction_metrics.success_rate < 90`
   - Channels: email, slack, sms
   - Escalation: immediate

2. **High Processing Time** (WARNING)
   - Condition: `transaction_metrics.avg_processing_time_seconds > 10`
   - Channels: email, slack
   - Escalation: 30 minutes

3. **High CPU Usage** (WARNING)
   - Condition: `system_metrics.cpu_usage > 80`
   - Channels: email, slack
   - Escalation: 15 minutes

4. **High Memory Usage** (WARNING)
   - Condition: `system_metrics.memory_usage > 85`
   - Channels: email, slack
   - Escalation: 15 minutes

5. **Bank Performance Degradation** (WARNING)
   - Condition: `bank_metrics.overall_success_rate < 95`
   - Channels: slack
   - Escalation: 15 minutes

## 🚀 Getting Started

### **1. Database Setup**

The system uses Flyway migrations to create the required tables:

```sql
-- V3: Create Alert System Tables
-- Creates: alert_rules, alerts, alert_history, alert_rule_channels, alert_channels
```

### **2. Configuration**

Add alerting configuration to `application.properties`:

```properties
# Alerting Configuration
alerting.enabled=true
alerting.evaluation-interval=30s
alerting.suppression-window=5m

# Notification Channels
alerting.email.enabled=true
alerting.email.smtp.host=smtp.company.com
alerting.email.smtp.port=587

alerting.sms.enabled=true
alerting.sms.provider=twilio
alerting.sms.api-key=your-twilio-api-key

alerting.slack.enabled=true
alerting.slack.webhook-url=https://hooks.slack.com/services/...

alerting.webhook.enabled=true
alerting.webhook.default-url=https://monitoring.company.com/webhook
```

### **3. Testing the System**

Use the provided test script:

```bash
./test_phase3_implementation.sh
```

### **4. Creating Custom Alert Rules**

```bash
curl -X POST "http://localhost:8081/api/v1/alerts/rules" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Custom High Volume Alert",
    "description": "Alert when transaction volume exceeds normal levels",
    "conditionExpression": "transaction_metrics.total_transactions > 1000",
    "severity": "WARNING",
    "notificationChannels": ["email", "slack"],
    "enabled": true,
    "metricType": "transaction",
    "metricName": "total_transactions"
  }'
```

## 📊 API Endpoints

### **Alert Rules Management**
- `GET /api/v1/alerts/rules` - Get all alert rules
- `POST /api/v1/alerts/rules` - Create new alert rule
- `PUT /api/v1/alerts/rules/{id}` - Update alert rule
- `DELETE /api/v1/alerts/rules/{id}` - Delete alert rule
- `PATCH /api/v1/alerts/rules/{id}/toggle` - Enable/disable rule

### **Alert Management**
- `GET /api/v1/alerts` - Get alerts with pagination
- `GET /api/v1/alerts/active` - Get active alerts
- `GET /api/v1/alerts/{id}` - Get alert by ID
- `PATCH /api/v1/alerts/{id}/acknowledge` - Acknowledge alert
- `PATCH /api/v1/alerts/{id}/resolve` - Resolve alert
- `PATCH /api/v1/alerts/{id}/suppress` - Suppress alert

### **Dashboard & Analytics**
- `GET /api/v1/alerts/dashboard/summary` - Get dashboard summary
- `GET /api/v1/alerts/history/recent` - Get recent alert history
- `GET /api/v1/alerts/{id}/history` - Get alert history

### **System Management**
- `POST /api/v1/alerts/evaluate` - Manually trigger evaluation
- `POST /api/v1/alerts/test-notification` - Test notification channels

## 🔔 Notification Examples

### **Email Notification**
```
Subject: [CRITICAL] NPS Alert - High Transaction Failure Rate

Alert Details:
==============

Name: High Transaction Failure Rate
Severity: CRITICAL
Status: ACTIVE
Timestamp: 2024-01-15 14:30:00

Message: Transaction success rate dropped to 85.2%

Metric Information:
  Metric: success_rate
  Current Value: 85.20
  Threshold: 90.00

Please investigate this issue promptly.

Best regards,
NPS Alerting System
```

### **Slack Notification**
```json
{
  "text": "🚨 *CRITICAL Alert*",
  "attachments": [
    {
      "color": "#FF0000",
      "title": "High Transaction Failure Rate",
      "text": "Transaction success rate dropped to 85.2%",
      "timestamp": 1705327800,
      "fields": [
        {
          "title": "Severity",
          "value": "CRITICAL",
          "short": true
        },
        {
          "title": "Status",
          "value": "ACTIVE",
          "short": true
        },
        {
          "title": "Metric Value",
          "value": "85.20",
          "short": true
        }
      ]
    }
  ]
}
```

### **SMS Notification**
```
[CRITICAL] NPS: Transaction success rate dropped to 85.2%
```

## 📈 Monitoring & Analytics

### **Alert Dashboard Metrics**
- Active alerts by severity
- Today's alert statistics
- Recent alert trends (7-day view)
- Alert resolution times
- Team performance metrics

### **Alert Trends Analysis**
- Alert frequency patterns
- Peak alert times
- Most common alert types
- Resolution time trends
- Escalation patterns

### **Performance Metrics**
- Alert evaluation time
- Notification delivery time
- System response time
- Database query performance
- Memory and CPU usage

## 🛠️ Maintenance & Operations

### **Regular Maintenance Tasks**
1. **Review Alert Rules**: Monthly review of alert rules and thresholds
2. **Clean Old Data**: Archive old alerts and history (configurable retention)
3. **Update Thresholds**: Adjust thresholds based on historical performance
4. **Test Notifications**: Regular testing of all notification channels
5. **Monitor Performance**: Track alert system performance metrics

### **Troubleshooting Common Issues**

#### **High Alert Volume**
- Review and adjust alert thresholds
- Implement alert suppression rules
- Consolidate similar alerts
- Review alert evaluation frequency

#### **Notification Failures**
- Check notification channel configuration
- Verify network connectivity
- Test notification channels individually
- Review notification service logs

#### **Performance Issues**
- Monitor database query performance
- Optimize alert evaluation intervals
- Review metrics collection frequency
- Check system resource usage

## 🔒 Security Considerations

### **Access Control**
- All alert management APIs require admin authentication
- Role-based access control for different alert operations
- Audit logging for all alert rule changes
- Secure storage of notification credentials

### **Data Privacy**
- Sensitive data masking in alert messages
- Configurable data retention policies
- Secure transmission of notifications
- Compliance with data protection regulations

## 🚀 Future Enhancements

### **Planned Features**
1. **Machine Learning Integration**: Anomaly detection using ML models
2. **Advanced Escalation**: Dynamic escalation based on business rules
3. **Alert Correlation**: Intelligent grouping of related alerts
4. **Mobile App**: Dedicated mobile app for alert management
5. **Integration APIs**: REST APIs for external monitoring systems

### **Scalability Improvements**
1. **Distributed Alerting**: Support for multiple alerting instances
2. **Event Streaming**: Real-time event processing with Kafka
3. **Microservices**: Split alerting into specialized microservices
4. **Cloud Native**: Kubernetes-native deployment and scaling

## 📚 Documentation

- **API Documentation**: `PHASE3_API_DOCUMENTATION.md`
- **Test Script**: `test_phase3_implementation.sh`
- **Database Migrations**: `src/main/resources/db/migration/V3__Create_alert_tables.sql`
- **Configuration Examples**: See `application.properties`

## 🤝 Support

For questions, issues, or feature requests related to the alerting system:

1. Check the API documentation
2. Review the test script for examples
3. Examine the database migrations
4. Contact the development team

---

**The Real-time Alerting System is now ready for production deployment and provides enterprise-grade monitoring capabilities for the NPS platform!** 🎉
