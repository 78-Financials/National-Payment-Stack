# 👥 NPS User Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Client Management](#client-management)
3. [Payment Processing](#payment-processing)
4. [Identification Verification](#identification-verification)
5. [Analytics & Reporting](#analytics--reporting)
6. [Alert Management](#alert-management)
7. [System Administration](#system-administration)
8. [Troubleshooting](#troubleshooting)

## Getting Started

### Overview
The Nigerian Payment Stack (NPS) Integration Service provides a comprehensive platform for processing payments, verifying account identities, and managing financial transactions through NIBSS. This guide will help you understand and use all the features of the system.

### System Requirements
- **Internet Connection**: Stable internet connection for API access
- **API Key**: Valid API key provided by system administrator
- **JSON Format**: Ability to send/receive JSON formatted requests
- **HTTPS**: All communications must use HTTPS

### Base URLs
- **Production**: `https://nps-api.payaza.com`
- **Staging**: `https://nps-staging-api.payaza.com`
- **Development**: `http://localhost:8080`

## Client Management

### Understanding Client Accounts
Each client in the system has the following attributes:
- **Client ID**: Unique identifier for your organization
- **API Key**: Secret key for authentication
- **Transaction Prefix**: Required prefix for all your transaction IDs
- **Rate Limit**: Maximum number of requests per hour
- **Status**: Active or Inactive

### API Key Management
Your API key is your primary method of authentication. Keep it secure and never share it publicly.

**Header Format:**
```http
X-API-Key: your_api_key_here
```

**Best Practices:**
- Store API keys securely
- Rotate keys regularly
- Use different keys for different environments
- Monitor key usage for security

### Transaction ID Requirements
All payment transactions must include a transaction ID with your assigned prefix:

**Format:** `{PREFIX}-{UNIQUE_ID}`

**Examples:**
- Bank ABC: `ABC-123456789`
- Credit Union XYZ: `XYZ-987654321`

## Payment Processing

### Making a Payment

#### **Endpoint:** `POST /api/v1/payments/transfer`

#### **Request Example:**
```json
{
  "messageId": "MSG123456789",
  "transactionId": "ABC-123456789",
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

#### **Field Descriptions:**
- **messageId**: Unique message identifier (alphanumeric, 10-50 characters)
- **transactionId**: Your transaction ID with proper prefix
- **senderInstitutionCode**: Sender bank code (3-digit code)
- **receiverInstitutionCode**: Receiver bank code (3-digit code)
- **senderAccountNumber**: Sender account number (10 digits)
- **receiverAccountNumber**: Receiver account number (10 digits)
- **senderAccountName**: Full name of sender
- **receiverAccountName**: Full name of receiver
- **amount**: Transaction amount (decimal, minimum 1.00)
- **currency**: Currency code (NGN for Nigerian Naira)
- **narration**: Payment description (optional)

#### **Response Example:**
```json
{
  "messageId": "MSG123456789",
  "transactionId": "ABC-123456789",
  "status": "SUCCESS",
  "responseCode": "00",
  "responseMessage": "Payment processed successfully",
  "processedAt": "2024-01-15T10:30:00Z"
}
```

#### **Response Codes:**
- **00**: Success
- **96**: System malfunction
- **68**: Timeout
- **91**: Invalid institution
- **92**: Invalid account
- **93**: Insufficient funds
- **94**: Duplicate transaction

### Payment Status Tracking
All payments are tracked in the system. You can monitor the status of your transactions through the analytics dashboard.

**Status Types:**
- **PENDING**: Payment submitted, awaiting processing
- **SUCCESS**: Payment completed successfully
- **FAILED**: Payment failed due to various reasons
- **TIMEOUT**: Payment timed out during processing

## Identification Verification

### Verifying Account Details

#### **Endpoint:** `POST /api/v1/identification/verify`

#### **Request Example:**
```json
{
  "messageId": "MSG123456789",
  "accountNumber": "1234567890",
  "bankCode": "001"
}
```

#### **Field Descriptions:**
- **messageId**: Unique message identifier
- **accountNumber**: Account number to verify (10 digits)
- **bankCode**: Bank code (3-digit code)

#### **Response Example:**
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

### Account Verification Use Cases
- **Pre-payment validation**: Verify account details before processing payments
- **Customer onboarding**: Validate customer account information
- **Fraud prevention**: Confirm account ownership
- **Compliance**: Meet regulatory requirements for account verification

## Analytics & Reporting

### Dashboard Overview
The analytics dashboard provides real-time insights into your payment processing activities.

#### **Key Metrics:**
- **Total Transactions**: Number of transactions processed
- **Success Rate**: Percentage of successful transactions
- **Average Response Time**: Mean processing time
- **Volume**: Total monetary value processed
- **Active Clients**: Number of active client accounts

### Transaction Analytics

#### **Endpoint:** `GET /api/v1/analytics/transactions`

#### **Query Parameters:**
- `startDate`: Start date (YYYY-MM-DD)
- `endDate`: End date (YYYY-MM-DD)
- `clientId`: Filter by client ID
- `status`: Filter by transaction status
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

#### **Example Request:**
```http
GET /api/v1/analytics/transactions?startDate=2024-01-01&endDate=2024-01-31&status=SUCCESS&page=0&size=10
```

#### **Response Example:**
```json
{
  "content": [
    {
      "transactionId": "ABC-123456789",
      "amount": 1000.00,
      "currency": "NGN",
      "status": "SUCCESS",
      "senderBank": "001",
      "receiverBank": "002",
      "createdAt": "2024-01-15T10:30:00Z",
      "processedAt": "2024-01-15T10:30:05Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 15,
  "size": 10,
  "number": 0
}
```

### Bank Performance Analytics
Monitor the performance of different banks and financial institutions.

#### **Key Metrics:**
- **Success Rate**: Percentage of successful transactions per bank
- **Average Response Time**: Mean processing time per bank
- **Transaction Volume**: Number of transactions per bank
- **Error Rate**: Percentage of failed transactions per bank

### Historical Analytics
Access historical data for trend analysis and reporting.

#### **Available Reports:**
- **Daily Transaction Summary**: Daily transaction volumes and success rates
- **Monthly Performance Report**: Monthly performance metrics
- **Bank Comparison Report**: Comparative analysis of bank performance
- **Error Analysis Report**: Detailed error analysis and patterns

## Alert Management

### Understanding Alerts
The alert system monitors system performance and notifies you of important events or issues.

#### **Alert Types:**
- **Performance Alerts**: High response times, low throughput
- **Error Alerts**: High error rates, system failures
- **Security Alerts**: Suspicious activities, authentication failures
- **Business Alerts**: Unusual transaction patterns, threshold breaches

### Alert Severity Levels
- **CRITICAL**: Immediate attention required
- **WARNING**: Attention required within hours
- **INFO**: Informational notification

### Managing Alert Rules

#### **Creating Alert Rules**
You can create custom alert rules to monitor specific metrics.

#### **Endpoint:** `POST /api/v1/alerts/rules`

#### **Request Example:**
```json
{
  "name": "High Error Rate Alert",
  "description": "Alert when error rate exceeds 5%",
  "metricName": "error_rate",
  "condition": "GREATER_THAN",
  "threshold": 5.0,
  "severity": "WARNING",
  "enabled": true,
  "evaluationInterval": 300
}
```

#### **Condition Types:**
- **GREATER_THAN**: Metric value exceeds threshold
- **LESS_THAN**: Metric value below threshold
- **EQUALS**: Metric value equals threshold
- **NOT_EQUALS**: Metric value not equal to threshold

#### **Available Metrics:**
- **error_rate**: Percentage of failed requests
- **response_time**: Average response time
- **throughput**: Transactions per second
- **success_rate**: Percentage of successful requests
- **active_transactions**: Number of active transactions

### Viewing Active Alerts

#### **Endpoint:** `GET /api/v1/alerts/active`

#### **Response Example:**
```json
[
  {
    "id": 1,
    "title": "High Error Rate Detected",
    "message": "Error rate is 8.5%, exceeding threshold of 5%",
    "severity": "WARNING",
    "status": "ACTIVE",
    "metricValue": 8.5,
    "thresholdValue": 5.0,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

### Acknowledging Alerts
When you receive an alert, you should acknowledge it to indicate you're aware of the issue.

#### **Endpoint:** `POST /api/v1/alerts/{alertId}/acknowledge`

#### **Request Example:**
```json
{
  "acknowledgedBy": "admin@company.com",
  "notes": "Investigating the issue"
}
```

## System Administration

### Admin Dashboard
The admin dashboard provides comprehensive system management capabilities.

#### **Features:**
- **Client Management**: Create, update, and manage client accounts
- **System Monitoring**: Real-time system health and performance metrics
- **User Management**: Manage admin users and permissions
- **Configuration**: System configuration and settings
- **Audit Logs**: Complete audit trail of all system activities

### Client Management

#### **Creating New Clients**
1. Navigate to Client Management section
2. Click "Create New Client"
3. Fill in client details:
   - Client ID (unique identifier)
   - Client Name
   - Transaction Prefix
   - Rate Limit
4. Click "Create Client"
5. Copy and securely store the generated API key

#### **Updating Client Settings**
1. Find the client in the client list
2. Click "Edit" button
3. Modify the required settings
4. Click "Save Changes"

#### **Regenerating API Keys**
1. Select the client
2. Click "Regenerate API Key"
3. Confirm the action
4. Securely distribute the new API key to the client

### System Configuration

#### **Rate Limiting**
Configure rate limits to prevent system abuse:
- **Default Rate Limit**: 1,000 requests per hour
- **Per-Client Limits**: Customizable per client
- **Burst Limits**: Allow temporary spikes in traffic

#### **Monitoring Settings**
Configure monitoring parameters:
- **Metrics Collection Interval**: How often to collect metrics
- **Alert Evaluation Interval**: How often to evaluate alert rules
- **Data Retention Period**: How long to keep historical data

### Audit Logging
All system activities are logged for security and compliance purposes.

#### **Audit Log Categories:**
- **Authentication**: Login attempts, API key usage
- **Administrative Actions**: Client management, configuration changes
- **API Calls**: All API requests and responses
- **System Events**: System startup, shutdown, errors

#### **Accessing Audit Logs**
1. Navigate to Audit Logs section
2. Use filters to narrow down results:
   - Date range
   - Action type
   - User/Client ID
   - Status
3. Export logs for external analysis

## Troubleshooting

### Common Issues and Solutions

#### **1. Authentication Errors (401 Unauthorized)**

**Problem**: API requests returning 401 Unauthorized
**Possible Causes:**
- Invalid or missing API key
- Client account is inactive
- API key format is incorrect

**Solutions:**
- Verify API key is correct and properly formatted
- Check that client account is active
- Ensure API key is included in the correct header: `X-API-Key`
- Contact administrator if issues persist

#### **2. Transaction ID Validation Errors (400 Bad Request)**

**Problem**: Transaction ID validation failing
**Possible Causes:**
- Missing or incorrect transaction prefix
- Invalid transaction ID format
- Duplicate transaction ID

**Solutions:**
- Ensure transaction ID includes your assigned prefix
- Use unique transaction IDs for each request
- Check transaction ID format: `{PREFIX}-{UNIQUE_ID}`

#### **3. Payment Processing Failures**

**Problem**: Payments failing with error codes
**Common Error Codes:**
- **91**: Invalid institution code
- **92**: Invalid account number
- **93**: Insufficient funds
- **94**: Duplicate transaction

**Solutions:**
- Verify bank codes are correct (3-digit codes)
- Confirm account numbers are valid (10 digits)
- Check for sufficient funds
- Ensure transaction IDs are unique

#### **4. Slow Response Times**

**Problem**: API responses taking longer than expected
**Possible Causes:**
- High system load
- Network connectivity issues
- Database performance problems

**Solutions:**
- Check system status dashboard
- Verify network connectivity
- Contact support if issues persist
- Consider implementing retry logic with exponential backoff

#### **5. Rate Limiting Issues (429 Too Many Requests)**

**Problem**: Receiving rate limit exceeded errors
**Solutions:**
- Reduce request frequency
- Implement proper request throttling
- Contact administrator to increase rate limits
- Use batch processing for multiple transactions

### Error Response Format
All API errors follow a consistent format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/payments/transfer",
  "details": {
    "field": "transactionId",
    "message": "Transaction ID must include valid prefix"
  }
}
```

### Getting Help

#### **Support Channels:**
- **Email**: support@payaza.com
- **Phone**: +234-XXX-XXXX
- **Documentation**: https://docs.payaza.com/nps
- **Status Page**: https://status.payaza.com

#### **When Reporting Issues:**
1. Include your client ID
2. Provide the exact error message
3. Include request details (without sensitive data)
4. Specify the time when the issue occurred
5. Describe steps to reproduce the issue

#### **Response Times:**
- **Critical Issues**: Within 2 hours
- **High Priority**: Within 4 hours
- **Medium Priority**: Within 24 hours
- **Low Priority**: Within 72 hours

### Best Practices

#### **Security Best Practices:**
- Keep API keys secure and never share them
- Use HTTPS for all communications
- Implement proper error handling
- Log API interactions for audit purposes
- Regularly rotate API keys

#### **Performance Best Practices:**
- Implement proper request throttling
- Use connection pooling for high-volume applications
- Cache frequently accessed data
- Monitor response times and error rates
- Implement retry logic with exponential backoff

#### **Integration Best Practices:**
- Test thoroughly in staging environment
- Implement comprehensive error handling
- Use proper HTTP status codes
- Follow RESTful API conventions
- Document your integration

### System Status and Maintenance

#### **Maintenance Windows:**
- **Planned Maintenance**: Typically on Sundays 2:00 AM - 4:00 AM WAT
- **Emergency Maintenance**: As needed with advance notice
- **Updates**: Usually deployed during low-traffic periods

#### **Status Notifications:**
- Subscribe to status updates at https://status.payaza.com
- Follow @PayazaStatus on Twitter for real-time updates
- Check email notifications for planned maintenance

#### **SLA Guarantees:**
- **Uptime**: 99.9% availability
- **Response Time**: 95% of requests under 2 seconds
- **Error Rate**: Less than 0.1% error rate
- **Support Response**: As per response time commitments above

## Conclusion

This user guide provides comprehensive information for using the NPS Integration Service effectively. For additional support, technical details, or advanced configuration options, please refer to the technical documentation or contact our support team.

Remember to:
- Keep your API keys secure
- Monitor your transaction volumes and success rates
- Stay updated with system status and maintenance notifications
- Follow best practices for optimal performance and security

For the latest updates and announcements, visit our documentation portal at https://docs.payaza.com/nps.
