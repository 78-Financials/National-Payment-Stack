# NPS API Documentation

## 📋 **Overview**

This document provides comprehensive API documentation for the Nigerian Payment Stack (NPS) system. All APIs follow RESTful conventions and return JSON responses.

**Base URL**: `https://api.nps.payaza.africa/api/v1`

**Authentication**: Bearer Token (JWT) or API Key

---

## 🔐 **Authentication**

### **Headers**
```http
Authorization: Bearer {jwt_token}
# OR
X-API-Key: {api_key}
Content-Type: application/json
```

### **Authentication Endpoints**

#### **Login**
```http
POST /auth/login
```

**Request Body:**
```json
{
  "clientId": "string",
  "apiKey": "string"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "clientId": "CLIENT123",
  "clientName": "ABC Bank",
  "clientType": "BANK",
  "permissions": ["PAYMENT_INITIATE", "ACCOUNT_VERIFY"],
  "expiresAt": "2024-01-01T12:00:00Z",
  "refreshToken": "refresh_token_here"
}
```

#### **Refresh Token**
```http
POST /auth/refresh
```

**Response:**
```json
{
  "token": "new_jwt_token",
  "expiresAt": "2024-01-01T12:00:00Z"
}
```

---

## 💰 **Payment Management**

### **Initiate Payment (PACS.008)**

```http
POST /payments/initiate
```

**Request Body:**
```json
{
  "transactionId": "TXN123456789",
  "amount": "1000.00",
  "currency": "NGN",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "debtorBankCode": "044",
  "creditorBankCode": "058",
  "narration": "Payment for services",
  "reference": "REF123456"
}
```

**Response:**
```json
{
  "transactionId": "TXN123456789",
  "messageId": "MSG123456789",
  "status": "PENDING",
  "responseCode": "00",
  "responseMessage": "Payment initiated successfully",
  "processedAt": "2024-01-01T10:30:00Z"
}
```

### **Payment Status Report (PACS.002)**

```http
POST /payments/status-report
```

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "transactionId": "TXN123456789",
  "paymentStatus": "SUCCESS",
  "responseCode": "00",
  "responseMessage": "Payment completed successfully",
  "processedAt": "2024-01-01T10:35:00Z"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "responseCode": "00",
  "responseMessage": "Status report processed successfully",
  "status": "SUCCESS",
  "paymentStatus": "SUCCESS",
  "createdAt": "2024-01-01T10:35:00Z"
}
```

### **Payment Status Request (PACS.028)**

```http
POST /payments/status-request
```

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "originalTransactionId": "TXN123456789",
  "requestDate": "2024-01-01T10:30:00Z"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "responseCode": "00",
  "responseMessage": "Status request processed successfully",
  "status": "SUCCESS",
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### **Get Payment History**

```http
GET /payments/history?page=0&size=20&from=2024-01-01&to=2024-01-31&status=SUCCESS
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `from` (optional): Start date (YYYY-MM-DD)
- `to` (optional): End date (YYYY-MM-DD)
- `status` (optional): Filter by status (PENDING, SUCCESS, FAILED)
- `transactionId` (optional): Filter by transaction ID

**Response:**
```json
{
  "content": [
    {
      "transactionId": "TXN123456789",
      "amount": "1000.00",
      "currency": "NGN",
      "status": "SUCCESS",
      "debtorAccount": "1234567890",
      "creditorAccount": "0987654321",
      "narration": "Payment for services",
      "createdAt": "2024-01-01T10:30:00Z",
      "processedAt": "2024-01-01T10:35:00Z",
      "responseCode": "00",
      "responseMessage": "Payment completed successfully"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "currentPage": 0,
  "size": 20
}
```

---

## 🔍 **Account Verification**

### **Verify Account (ACMT.023)**

```http
POST /identification/verify
```

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "accountNumber": "1234567890",
  "bankCode": "044",
  "accountName": "John Doe"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "responseCode": "00",
  "responseMessage": "Account verification successful",
  "status": "SUCCESS",
  "accountVerified": true,
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### **Identification Verification Report (ACMT.024)**

```http
POST /identification/report
```

**Request Body:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "accountNumber": "1234567890",
  "bankCode": "044",
  "accountName": "John Doe",
  "verificationStatus": "SUCCESS",
  "accountVerified": true,
  "responseCode": "00",
  "responseMessage": "Account verification successful",
  "processedAt": "2024-01-01T10:35:00Z"
}
```

**Response:**
```json
{
  "messageId": "MSG123456789",
  "originalMessageId": "MSG987654321",
  "responseCode": "00",
  "responseMessage": "Verification report processed successfully",
  "status": "SUCCESS",
  "verificationStatus": "SUCCESS",
  "accountVerified": true,
  "createdAt": "2024-01-01T10:35:00Z"
}
```

---

## 🚨 **Alert Management (Admin Only)**

### **Get Alert Configuration**

```http
GET /admin/alerts/config
```

**Response:**
```json
{
  "enabledMessageTypes": ["PACS008", "PACS002", "ACMT023", "ACMT024", "PACS028"],
  "suppressedAlertTypes": [],
  "isSuppressed": false,
  "suppressionEndTime": null,
  "suppressionReason": null,
  "suppressedBy": null,
  "maxAlertsPerHour": 100,
  "analyticsRetentionDays": 30
}
```

### **Suppress Alerts**

```http
POST /admin/alerts/suppress?hours=2&reason=Maintenance&suppressedBy=admin
```

**Query Parameters:**
- `hours` (required): Duration in hours
- `reason` (required): Reason for suppression
- `suppressedBy` (optional): Admin user ID

**Response:**
```json
{
  "message": "Alerts suppressed successfully",
  "suppressedUntil": "2024-01-01T12:30:00Z",
  "reason": "Maintenance",
  "suppressedBy": "admin"
}
```

### **Clear Alert Suppression**

```http
POST /admin/alerts/unsuppress
```

**Response:**
```json
{
  "message": "Alert suppression cleared successfully",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Enable/Disable Message Type**

```http
POST /admin/alerts/message-types/{messageType}/enable
POST /admin/alerts/message-types/{messageType}/disable
```

**Response:**
```json
{
  "message": "Alerts enabled for message type: PACS008",
  "messageType": "PACS008",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Set Enabled Message Types**

```http
PUT /admin/alerts/message-types
```

**Request Body:**
```json
["PACS008", "PACS002", "PACS028"]
```

**Response:**
```json
{
  "message": "Enabled message types updated successfully",
  "enabledTypes": ["PACS008", "PACS002", "PACS028"],
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Suppress Specific Alert Type**

```http
POST /admin/alerts/types/{messageType}/{severity}/suppress
POST /admin/alerts/types/{messageType}/{severity}/unsuppress
```

**Response:**
```json
{
  "message": "Alert type suppressed successfully",
  "messageType": "PACS008",
  "severity": "CRITICAL",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Get Alert Analytics**

```http
GET /admin/alerts/analytics
```

**Response:**
```json
{
  "totalAlerts": 150,
  "enabledMessageTypes": ["PACS008", "PACS002"],
  "suppressedAlertTypes": [],
  "isSuppressed": false,
  "alertCountsByType": {
    "PACS008_CRITICAL": 25,
    "PACS008_WARNING": 10,
    "PACS002_INFO": 5
  },
  "lastAlertTimes": {
    "PACS008_CRITICAL": "2024-01-01T10:30:00Z",
    "PACS008_WARNING": "2024-01-01T09:15:00Z"
  }
}
```

### **Get Recent Alerts**

```http
GET /admin/alerts/recent?limit=50
```

**Query Parameters:**
- `limit` (optional): Number of alerts to return (default: 50)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "PACS008 Processing Failure",
      "message": "Error processing PACS.008 message for transaction TXN123: Connection timeout",
      "severity": "CRITICAL",
      "status": "ACTIVE",
      "metricType": "ERROR",
      "metricName": "pacs008_processing_failure",
      "context": "{\"messageType\":\"PACS008\",\"transactionId\":\"TXN123\",\"errorType\":\"PACS008_PROCESSING_ERROR\",\"timestamp\":\"2024-01-01T10:30:00Z\",\"severity\":\"CRITICAL\"}",
      "notificationChannels": ["email", "slack", "webhook"],
      "createdAt": "2024-01-01T10:30:00Z",
      "updatedAt": "2024-01-01T10:30:00Z"
    }
  ]
}
```

### **Get Alerts by Message Type**

```http
GET /admin/alerts/message-types/{messageType}?limit=25
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "PACS008 Processing Failure",
      "message": "Error processing PACS.008 message",
      "severity": "CRITICAL",
      "status": "ACTIVE",
      "createdAt": "2024-01-01T10:30:00Z"
    }
  ]
}
```

### **Test Alert System**

```http
POST /admin/alerts/test?messageType=PACS008&severity=CRITICAL&transactionId=TEST123
```

**Query Parameters:**
- `messageType` (required): Message type to test
- `severity` (required): Alert severity (CRITICAL, WARNING, INFO)
- `transactionId` (optional): Test transaction ID

**Response:**
```json
{
  "message": "Test alert triggered successfully",
  "messageType": "PACS008",
  "severity": "CRITICAL",
  "transactionId": "TEST123",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Reset Alert Analytics**

```http
POST /admin/alerts/analytics/reset
```

**Response:**
```json
{
  "message": "Alert analytics reset successfully",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Cleanup Analytics**

```http
POST /admin/alerts/analytics/cleanup
```

**Response:**
```json
{
  "message": "Analytics cleanup completed successfully",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

---

## 👥 **Client Management (Admin Only)**

### **Get All Clients**

```http
GET /admin/clients?page=0&size=20&search=bank&clientType=BANK&isActive=true
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `search` (optional): Search by client name or ID
- `clientType` (optional): Filter by type (BANK, FIN, PAY)
- `isActive` (optional): Filter by active status

**Response:**
```json
{
  "content": [
    {
      "clientId": "CLIENT123",
      "clientName": "ABC Bank",
      "clientType": "BANK",
      "description": "Commercial bank",
      "contactEmail": "admin@abcbank.com",
      "contactPhone": "+2341234567890",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z",
      "lastActivity": "2024-01-01T10:00:00Z",
      "totalTransactions": 1500,
      "successRate": 98.5
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

### **Create New Client**

```http
POST /admin/clients
```

**Request Body:**
```json
{
  "clientName": "XYZ Bank",
  "clientType": "BANK",
  "description": "Commercial bank",
  "contactEmail": "admin@xyzbank.com",
  "contactPhone": "+2341234567890"
}
```

**Response:**
```json
{
  "clientId": "CLIENT456",
  "apiKey": "ak_live_1234567890abcdef",
  "clientName": "XYZ Bank",
  "clientType": "BANK",
  "description": "Commercial bank",
  "contactEmail": "admin@xyzbank.com",
  "contactPhone": "+2341234567890",
  "isActive": true,
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### **Get Client Details**

```http
GET /admin/clients/{clientId}
```

**Response:**
```json
{
  "clientId": "CLIENT123",
  "clientName": "ABC Bank",
  "clientType": "BANK",
  "description": "Commercial bank",
  "contactEmail": "admin@abcbank.com",
  "contactPhone": "+2341234567890",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "lastActivity": "2024-01-01T10:00:00Z",
  "totalTransactions": 1500,
  "successRate": 98.5,
  "apiKey": "ak_live_1234567890abcdef"
}
```

### **Update Client**

```http
PUT /admin/clients/{clientId}
```

**Request Body:**
```json
{
  "clientName": "ABC Bank Updated",
  "description": "Updated description",
  "contactEmail": "newadmin@abcbank.com",
  "contactPhone": "+2341234567891"
}
```

**Response:**
```json
{
  "clientId": "CLIENT123",
  "clientName": "ABC Bank Updated",
  "clientType": "BANK",
  "description": "Updated description",
  "contactEmail": "newadmin@abcbank.com",
  "contactPhone": "+2341234567891",
  "isActive": true,
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### **Regenerate API Key**

```http
POST /admin/clients/{clientId}/regenerate-key
```

**Response:**
```json
{
  "clientId": "CLIENT123",
  "newApiKey": "ak_live_new1234567890abcdef",
  "regeneratedAt": "2024-01-01T10:30:00Z"
}
```

### **Activate/Deactivate Client**

```http
POST /admin/clients/{clientId}/activate
POST /admin/clients/{clientId}/deactivate
```

**Response:**
```json
{
  "clientId": "CLIENT123",
  "isActive": true,
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### **Delete Client**

```http
DELETE /admin/clients/{clientId}
```

**Response:**
```json
{
  "message": "Client deleted successfully",
  "clientId": "CLIENT123",
  "deletedAt": "2024-01-01T10:30:00Z"
}
```

---

## 📊 **Analytics & Reporting (Admin Only)**

### **Get Real-time Dashboard**

```http
GET /admin/analytics/dashboard/realtime
```

**Response:**
```json
{
  "systemHealth": {
    "status": "HEALTHY",
    "uptime": "99.9%",
    "responseTime": 2.5,
    "lastUpdated": "2024-01-01T10:30:00Z"
  },
  "transactionMetrics": {
    "totalToday": 1500,
    "successfulToday": 1450,
    "failedToday": 50,
    "totalVolume": "1500000.00",
    "successRate": 96.7
  },
  "clientMetrics": {
    "activeClients": 15,
    "totalClients": 20,
    "newClientsThisMonth": 3
  },
  "alertMetrics": {
    "activeAlerts": 5,
    "criticalAlerts": 2,
    "resolvedToday": 10
  }
}
```

### **Get Transaction Analytics**

```http
GET /admin/analytics/transactions?from=2024-01-01&to=2024-01-31&groupBy=day&clientId=CLIENT123
```

**Query Parameters:**
- `from` (required): Start date (YYYY-MM-DD)
- `to` (required): End date (YYYY-MM-DD)
- `groupBy` (optional): Grouping (day, week, month)
- `clientId` (optional): Filter by client

**Response:**
```json
{
  "summary": {
    "totalTransactions": 1500,
    "totalVolume": "1500000.00",
    "successRate": 96.7,
    "averageResponseTime": 2.5
  },
  "dailyData": [
    {
      "date": "2024-01-01",
      "transactions": 50,
      "volume": "50000.00",
      "successRate": 98.0,
      "averageResponseTime": 2.1
    }
  ],
  "clientBreakdown": [
    {
      "clientId": "CLIENT123",
      "clientName": "ABC Bank",
      "transactions": 800,
      "volume": "800000.00",
      "successRate": 97.5
    }
  ]
}
```

### **Get Client Analytics**

```http
GET /admin/analytics/clients/{clientId}?from=2024-01-01&to=2024-01-31
```

**Response:**
```json
{
  "clientId": "CLIENT123",
  "clientName": "ABC Bank",
  "period": {
    "from": "2024-01-01",
    "to": "2024-01-31"
  },
  "metrics": {
    "totalTransactions": 800,
    "totalVolume": "800000.00",
    "successRate": 97.5,
    "averageResponseTime": 2.1,
    "peakHour": "14:00",
    "busiestDay": "Monday"
  },
  "trends": {
    "transactionGrowth": 15.5,
    "volumeGrowth": 12.3,
    "successRateChange": 0.5
  }
}
```

---

## ⚙️ **System Configuration (Admin Only)**

### **Get Notification Configuration**

```http
GET /admin/notifications/config
```

**Response:**
```json
{
  "emailApiUrl": "http://localhost:8090/live/send-email-without-template",
  "emailApiKey": "yRWTlNCmDoacSTIJS3BcLM6kAQ9jL5E9TOqXdjld",
  "emailSender": "nps@payaza.africa",
  "emailRecipients": "admin@payaza.africa",
  "slackWebhookUrl": "https://hooks.slack.com/services/...",
  "webhookUrl": "https://webhook.site/...",
  "webhookApiKey": "webhook_api_key",
  "maxRetryAttempts": 3,
  "retryIntervalSeconds": 300
}
```

### **Update Email Configuration**

```http
PUT /admin/notifications/config/email
```

**Request Body:**
```json
{
  "recipients": "admin@payaza.africa,ops@payaza.africa"
}
```

**Response:**
```json
{
  "message": "Email recipients updated successfully"
}
```

### **Update Slack Configuration**

```http
PUT /admin/notifications/config/slack
```

**Request Body:**
```json
{
  "webhookUrl": "https://hooks.slack.com/services/new_webhook"
}
```

**Response:**
```json
{
  "message": "Slack webhook URL updated successfully"
}
```

### **Update Webhook Configuration**

```http
PUT /admin/notifications/config/webhook
```

**Request Body:**
```json
{
  "url": "https://new-webhook.site/endpoint",
  "apiKey": "new_webhook_api_key"
}
```

**Response:**
```json
{
  "message": "Webhook URL updated successfully"
}
```

### **Update Retry Configuration**

```http
PUT /admin/notifications/config/retry
```

**Request Body:**
```json
{
  "maxAttempts": 5,
  "intervalSeconds": 600
}
```

**Response:**
```json
{
  "message": "Retry settings updated successfully"
}
```

### **Reset Configuration**

```http
POST /admin/notifications/config/reset
```

**Response:**
```json
{
  "message": "Notification configuration reset to defaults"
}
```

---

## 📋 **Audit & Compliance (Admin Only)**

### **Get Audit Logs**

```http
GET /admin/audit/logs?page=0&size=50&from=2024-01-01&to=2024-01-31&action=PACS008_INITIATE&clientId=CLIENT123
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50)
- `from` (optional): Start date (YYYY-MM-DD)
- `to` (optional): End date (YYYY-MM-DD)
- `action` (optional): Filter by action
- `clientId` (optional): Filter by client
- `success` (optional): Filter by success status

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "action": "PACS008_INITIATE",
      "resource": "Payment",
      "resourceId": "TXN123456789",
      "clientId": "CLIENT123",
      "clientName": "ABC Bank",
      "timestamp": "2024-01-01T10:30:00Z",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
      "success": true,
      "responseTime": 250,
      "details": "Payment initiated successfully"
    }
  ],
  "totalElements": 1000,
  "totalPages": 20,
  "currentPage": 0,
  "size": 50
}
```

### **Export Audit Logs**

```http
GET /admin/audit/logs/export?from=2024-01-01&to=2024-01-31&format=csv
```

**Query Parameters:**
- `from` (required): Start date (YYYY-MM-DD)
- `to` (required): End date (YYYY-MM-DD)
- `format` (optional): Export format (csv, xlsx, json)

**Response:** File download

---

## 🔄 **Webhook Endpoints**

### **NIBSS Callbacks**

#### **PACS.008 Callback**
```http
POST /nps/callback/pacs008
Content-Type: application/xml
```

**Request Body:** Encrypted XML message from NIBSS

**Response:**
```xml
<Response>
  <Status>SUCCESS</Status>
  <Message>Callback processed successfully</Message>
</Response>
```

#### **PACS.002 Callback**
```http
POST /nps/callback/pacs002
Content-Type: application/xml
```

#### **ACMT.024 Callback**
```http
POST /nps/callback/acmt024
Content-Type: application/xml
```

#### **PACS.028 Callback**
```http
POST /nps/callback/pacs028
Content-Type: application/xml
```

---

## ❌ **Error Responses**

### **Standard Error Format**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid request parameters",
  "timestamp": "2024-01-01T10:30:00Z",
  "path": "/api/v1/payments/initiate",
  "clientId": "CLIENT123",
  "details": {
    "field": "amount",
    "reason": "Amount must be greater than 0"
  }
}
```

### **HTTP Status Codes**
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `422` - Validation Error
- `429` - Rate Limit Exceeded
- `500` - Internal Server Error
- `503` - Service Unavailable

### **Common Error Codes**
- `INVALID_API_KEY` - Invalid or expired API key
- `INSUFFICIENT_PERMISSIONS` - User lacks required permissions
- `VALIDATION_ERROR` - Request validation failed
- `TRANSACTION_NOT_FOUND` - Transaction does not exist
- `CLIENT_NOT_FOUND` - Client does not exist
- `RATE_LIMIT_EXCEEDED` - API rate limit exceeded
- `SYSTEM_ERROR` - Internal system error

---

## 🔒 **Rate Limiting**

### **Rate Limits**
- **Payment APIs**: 100 requests per minute per client
- **Verification APIs**: 50 requests per minute per client
- **Admin APIs**: 200 requests per minute per admin
- **Analytics APIs**: 30 requests per minute per admin

### **Rate Limit Headers**
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

---

## 📚 **SDK Examples**

### **JavaScript/Node.js**
```javascript
const axios = require('axios');

const npsClient = axios.create({
  baseURL: 'https://api.nps.payaza.africa/api/v1',
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': 'your_api_key'
  }
});

// Initiate payment
const initiatePayment = async (paymentData) => {
  try {
    const response = await npsClient.post('/payments/initiate', paymentData);
    return response.data;
  } catch (error) {
    console.error('Payment initiation failed:', error.response.data);
    throw error;
  }
};

// Check payment status
const checkPaymentStatus = async (transactionId, messageId) => {
  try {
    const response = await npsClient.post('/payments/status', {
      originalTransactionId: transactionId,
      originalMessageId: messageId
    });
    return response.data;
  } catch (error) {
    console.error('Status check failed:', error.response.data);
    throw error;
  }
};
```

### **Python**
```python
import requests
import json

class NPSClient:
    def __init__(self, api_key, base_url='https://api.nps.payaza.africa/api/v1'):
        self.api_key = api_key
        self.base_url = base_url
        self.headers = {
            'Content-Type': 'application/json',
            'X-API-Key': api_key
        }
    
    def initiate_payment(self, payment_data):
        response = requests.post(
            f'{self.base_url}/payments/initiate',
            headers=self.headers,
            json=payment_data
        )
        response.raise_for_status()
        return response.json()
    
    def check_payment_status(self, transaction_id, message_id):
        response = requests.post(
            f'{self.base_url}/payments/status',
            headers=self.headers,
            json={
                'originalTransactionId': transaction_id,
                'originalMessageId': message_id
            }
        )
        response.raise_for_status()
        return response.json()
```

### **PHP**
```php
<?php

class NPSClient {
    private $apiKey;
    private $baseUrl;
    
    public function __construct($apiKey, $baseUrl = 'https://api.nps.payaza.africa/api/v1') {
        $this->apiKey = $apiKey;
        $this->baseUrl = $baseUrl;
    }
    
    public function initiatePayment($paymentData) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $this->baseUrl . '/payments/initiate');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($paymentData));
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'X-API-Key: ' . $this->apiKey
        ]);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        
        if ($httpCode !== 200) {
            throw new Exception('Payment initiation failed: ' . $response);
        }
        
        return json_decode($response, true);
    }
}
```

---

## 🔐 **User Management APIs**

### **Get User Profile**
```http
GET /auth/profile
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "CLIENT123",
  "clientName": "ABC Bank",
  "email": "admin@abcbank.com",
  "phone": "+2348012345678",
  "clientType": "BANK",
  "isActive": true,
  "lastLogin": "2024-01-01T10:30:00Z",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### **Update User Profile**
```http
PUT /auth/profile
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "clientName": "ABC Bank Limited",
  "email": "admin@abcbank.com",
  "phone": "+2348012345678"
}
```

### **Change Password**
```http
POST /auth/change-password
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "currentPassword": "current_password",
  "newPassword": "new_secure_password",
  "confirmPassword": "new_secure_password"
}
```

### **Forgot Password**
```http
POST /auth/forgot-password
Content-Type: application/x-www-form-urlencoded
```

**Request Body:**
```
email=admin@abcbank.com
```

### **Reset Password**
```http
POST /auth/reset-password
Content-Type: application/x-www-form-urlencoded
```

**Request Body:**
```
token=reset_token_here&newPassword=new_password
```

### **Logout**
```http
POST /auth/logout
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Logged out successfully"
}
```

---

## 📊 **Advanced Reporting APIs (Admin Only)**

### **Generate Custom Report**
```http
POST /admin/reports/generate
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reportType": "transaction_summary",
  "reportName": "Daily Transaction Summary",
  "description": "Summary of all transactions for today",
  "fromDate": "2024-01-01",
  "toDate": "2024-01-31",
  "clientIds": ["CLIENT001", "CLIENT002"],
  "filters": {
    "status": "SUCCESS",
    "amountRange": {"min": 1000, "max": 10000}
  },
  "groupBy": "client",
  "format": "json"
}
```

**Response:**
```json
{
  "reportId": "RPT001",
  "reportName": "Daily Transaction Summary",
  "reportType": "transaction_summary",
  "status": "COMPLETED",
  "data": [
    {
      "transactionId": "TXN001",
      "amount": 1000.0,
      "status": "SUCCESS",
      "clientId": "CLIENT001",
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ],
  "summary": {
    "totalRecords": 1,
    "generatedAt": "2024-01-01T10:30:00Z",
    "reportType": "transaction_summary"
  },
  "metadata": {
    "requestedBy": "admin",
    "filters": {...},
    "groupBy": "client",
    "format": "json"
  },
  "downloadUrl": "/admin/reports/export/RPT001"
}
```

### **Export Report**
```http
GET /admin/reports/export/{reportId}?format=csv
Authorization: Bearer {admin_token}
```

**Response:** File download (CSV, Excel, PDF, JSON)

### **Get Report Templates**
```http
GET /admin/reports/templates
Authorization: Bearer {admin_token}
```

**Response:**
```json
[
  {
    "id": "transaction_summary",
    "name": "Transaction Summary Report",
    "description": "Summary of all transactions for a given period",
    "category": "transactions",
    "parameters": ["fromDate", "toDate", "clientId"]
  },
  {
    "id": "client_performance",
    "name": "Client Performance Report",
    "description": "Performance metrics for specific clients",
    "category": "clients",
    "parameters": ["fromDate", "toDate", "clientIds"]
  }
]
```

### **Schedule Report**
```http
POST /admin/reports/schedule
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reportType": "transaction_summary",
  "reportName": "Daily Transaction Summary",
  "schedule": {
    "enabled": true,
    "frequency": "DAILY",
    "time": "09:00",
    "recipients": ["admin@example.com"]
  }
}
```

### **Get Scheduled Reports**
```http
GET /admin/reports/scheduled
Authorization: Bearer {admin_token}
```

### **Cancel Scheduled Report**
```http
DELETE /admin/reports/scheduled/{scheduleId}
Authorization: Bearer {admin_token}
```

### **Get Report History**
```http
GET /admin/reports/history?page=0&size=20&from=2024-01-01&to=2024-01-31
Authorization: Bearer {admin_token}
```

### **Get Real-time Metrics**
```http
GET /admin/reports/metrics/realtime
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "systemUptime": "99.9%",
  "activeConnections": 150,
  "memoryUsage": "75%",
  "cpuUsage": "45%",
  "transactionsToday": 1250,
  "successRate": 98.5,
  "averageResponseTime": 2.3,
  "activeClients": 25,
  "lastUpdated": "2024-01-01T10:30:00Z"
}
```

### **Get Performance Metrics**
```http
GET /admin/reports/metrics/performance?from=2024-01-01&to=2024-01-31
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "averageResponseTime": 2.1,
  "p95ResponseTime": 5.8,
  "p99ResponseTime": 12.3,
  "throughput": 150.5,
  "errorRate": 0.5,
  "timeSeries": [
    {
      "timestamp": "2024-01-01T00:00:00Z",
      "responseTime": 2.0,
      "throughput": 140.0,
      "errorRate": 0.5
    }
  ]
}
```

---

## 🔗 **Integration & Webhook Management APIs (Admin Only)**

### **Get Webhook Configurations**
```http
GET /admin/integrations/webhooks
Authorization: Bearer {admin_token}
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Payment Success Webhook",
    "url": "https://example.com/webhook",
    "apiKey": "webhook_api_key",
    "eventTypes": ["PAYMENT_SUCCESS", "PAYMENT_FAILED"],
    "isActive": true,
    "retryAttempts": 3,
    "timeoutSeconds": 30,
    "description": "Webhook for payment notifications",
    "createdAt": "2024-01-01T10:30:00Z",
    "updatedAt": "2024-01-01T10:30:00Z"
  }
]
```

### **Create Webhook Configuration**
```http
POST /admin/integrations/webhooks
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Payment Success Webhook",
  "url": "https://example.com/webhook",
  "apiKey": "webhook_api_key",
  "eventTypes": ["PAYMENT_SUCCESS", "PAYMENT_FAILED"],
  "isActive": true,
  "retryAttempts": 3,
  "timeoutSeconds": 30,
  "description": "Webhook for payment notifications"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Payment Success Webhook",
  "url": "https://example.com/webhook",
  "apiKey": "webhook_api_key",
  "eventTypes": ["PAYMENT_SUCCESS", "PAYMENT_FAILED"],
  "isActive": true,
  "retryAttempts": 3,
  "timeoutSeconds": 30,
  "description": "Webhook for payment notifications",
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### **Update Webhook Configuration**
```http
PUT /admin/integrations/webhooks/{webhookId}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Updated Payment Success Webhook",
  "url": "https://example.com/webhook/updated",
  "apiKey": "new_webhook_api_key",
  "eventTypes": ["PAYMENT_SUCCESS", "PAYMENT_FAILED", "PAYMENT_PENDING"],
  "isActive": true,
  "retryAttempts": 5,
  "timeoutSeconds": 45,
  "description": "Updated webhook for payment notifications"
}
```

### **Delete Webhook Configuration**
```http
DELETE /admin/integrations/webhooks/{webhookId}
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "message": "Webhook configuration deleted successfully",
  "webhookId": 1,
  "deletedAt": "2024-01-01T10:30:00Z"
}
```

### **Test Webhook Configuration**
```http
POST /admin/integrations/webhooks/{webhookId}/test
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "success": true,
  "response": "OK",
  "responseTime": 150,
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Get Rate Limit Configurations**
```http
GET /admin/integrations/rate-limits
Authorization: Bearer {admin_token}
```

**Response:**
```json
[
  {
    "clientId": "BANK001",
    "hourlyLimit": 1000,
    "dailyLimit": 10000,
    "burstLimit": 100,
    "updatedAt": "2024-01-01T10:30:00Z"
  }
]
```

### **Update Rate Limit Configuration**
```http
PUT /admin/integrations/rate-limits/{clientId}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "hourlyLimit": 1000,
  "dailyLimit": 10000,
  "burstLimit": 100
}
```

**Response:**
```json
{
  "clientId": "BANK001",
  "hourlyLimit": 1000,
  "dailyLimit": 10000,
  "burstLimit": 100,
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### **Get Client API Usage**
```http
GET /admin/integrations/rate-limits/{clientId}/usage
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "clientId": "BANK001",
  "requestsToday": 500,
  "requestsThisHour": 50,
  "requestsThisMinute": 5,
  "rateLimitRemaining": 950,
  "lastRequest": "2024-01-01T10:30:00Z"
}
```

---

## 📡 **Inbound Subscription Management APIs (Admin Only)**

### **Get Current Inbound PACS.008 Subscriber**
```http
GET /admin/inbound-subscriptions/pacs008/current
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "clientId": "BANK001",
  "messageType": "PACS008",
  "callbackUrl": "https://bank001.com/callback/pacs008",
  "isActive": true,
  "description": "Inbound PACS.008 subscription for BANK001",
  "createdAt": "2024-01-01T10:30:00Z",
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### **Set Inbound PACS.008 Subscriber**
```http
POST /admin/inbound-subscriptions/pacs008
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "clientId": "BANK001",
  "messageType": "PACS008",
  "callbackUrl": "https://bank001.com/callback/pacs008",
  "isActive": true,
  "description": "Inbound PACS.008 subscription for BANK001"
}
```

**Response:**
```json
{
  "message": "Inbound PACS.008 subscriber set successfully",
  "clientId": "BANK001",
  "messageType": "PACS008",
  "callbackUrl": "https://bank001.com/callback/pacs008",
  "isActive": true,
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### **Remove Inbound PACS.008 Subscriber**
```http
DELETE /admin/inbound-subscriptions/pacs008
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "message": "Inbound PACS.008 subscriber removed successfully",
  "removedAt": "2024-01-01T10:30:00Z"
}
```

### **Get Queue Status**
```http
GET /admin/inbound-subscriptions/queue/status
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "queueSize": 25,
  "processingRate": 10,
  "averageProcessingTime": 150,
  "lastProcessed": "2024-01-01T10:30:00Z",
  "status": "HEALTHY"
}
```

### **Get Subscription History**
```http
GET /admin/inbound-subscriptions/history?page=0&size=20
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "clientId": "BANK001",
      "messageType": "PACS008",
      "callbackUrl": "https://bank001.com/callback/pacs008",
      "isActive": false,
      "createdAt": "2024-01-01T10:30:00Z",
      "updatedAt": "2024-01-01T11:30:00Z",
      "deactivatedAt": "2024-01-01T11:30:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

---

## 🔗 **Integration & Webhook Management APIs (Admin Only)**

### **Get Webhook Configurations**
```http
GET /admin/integrations/webhooks
Authorization: Bearer {admin_token}
```

### **Create Webhook Configuration**
```http
POST /admin/integrations/webhooks
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Payment Success Webhook",
  "url": "https://example.com/webhook",
  "apiKey": "webhook_api_key",
  "eventTypes": ["PAYMENT_SUCCESS", "PAYMENT_FAILED"],
  "isActive": true,
  "retryAttempts": 3,
  "timeoutSeconds": 30,
  "description": "Webhook for payment notifications"
}
```

### **Update Webhook Configuration**
```http
PUT /admin/integrations/webhooks/{webhookId}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

### **Delete Webhook Configuration**
```http
DELETE /admin/integrations/webhooks/{webhookId}
Authorization: Bearer {admin_token}
```

### **Test Webhook Configuration**
```http
POST /admin/integrations/webhooks/{webhookId}/test
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "success": true,
  "response": "OK",
  "responseTime": 150,
  "timestamp": "2024-01-01T10:30:00Z"
}
```

### **Get Webhook Deliveries**
```http
GET /admin/integrations/webhooks/{webhookId}/deliveries?page=0&size=20
Authorization: Bearer {admin_token}
```

### **Retry Webhook Delivery**
```http
POST /admin/integrations/webhooks/{webhookId}/deliveries/{deliveryId}/retry
Authorization: Bearer {admin_token}
```

### **Get Rate Limit Configurations**
```http
GET /admin/integrations/rate-limits
Authorization: Bearer {admin_token}
```

### **Update Rate Limit Configuration**
```http
PUT /admin/integrations/rate-limits/{clientId}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "hourlyLimit": 1000,
  "dailyLimit": 10000,
  "burstLimit": 100
}
```

### **Get Client API Usage**
```http
GET /admin/integrations/rate-limits/{clientId}/usage
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "requestsToday": 500,
  "requestsThisHour": 50,
  "requestsThisMinute": 5,
  "rateLimitRemaining": 950,
  "lastRequest": "2024-01-01T10:30:00Z"
}
```

### **Get Third-party Integrations**
```http
GET /admin/integrations/third-party
Authorization: Bearer {admin_token}
```

### **Create Third-party Integration**
```http
POST /admin/integrations/third-party
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Slack Integration",
  "type": "SLACK",
  "endpoint": "https://hooks.slack.com/services/xxx",
  "apiKey": "slack_api_key",
  "isActive": true,
  "description": "Slack notifications for alerts"
}
```

### **Test Third-party Integration**
```http
POST /admin/integrations/third-party/{integrationId}/test
Authorization: Bearer {admin_token}
```

---

## 🖥️ **System Monitoring & Health APIs (Admin Only)**

### **Get System Health**
```http
GET /admin/system/health
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "status": "HEALTHY",
  "uptime": "99.9%",
  "version": "1.0.0",
  "timestamp": "2024-01-01T10:30:00Z",
  "issues": [],
  "components": {
    "database": "HEALTHY",
    "externalServices": "HEALTHY",
    "memory": "HEALTHY"
  }
}
```

### **Get System Metrics**
```http
GET /admin/system/metrics
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "resources": {
    "memoryUsage": 75.5,
    "cpuUsage": 45.2,
    "diskUsage": 60.8,
    "networkLatency": 12.5
  },
  "application": {
    "activeSessions": 25,
    "totalRequests": 15000,
    "errorCount": 15,
    "averageResponseTime": 2.1
  },
  "database": {
    "connectionPoolSize": 20,
    "activeConnections": 8,
    "queryCount": 5000,
    "slowQueries": 5
  },
  "external": {
    "nibssRequests": 1000,
    "emailSent": 250,
    "webhookDeliveries": 500,
    "failedRequests": 10
  }
}
```

### **Get Performance Metrics**
```http
GET /admin/system/performance?from=2024-01-01&to=2024-01-31
Authorization: Bearer {admin_token}
```

### **Get Database Health**
```http
GET /admin/system/database/health
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "status": "HEALTHY",
  "connectionCount": 15,
  "activeConnections": 8,
  "responseTime": 12,
  "lastCheck": "2024-01-01T10:30:00Z"
}
```

### **Get External Services Health**
```http
GET /admin/system/external-services/health
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "status": "HEALTHY",
  "nibss": {
    "status": "HEALTHY",
    "responseTime": 150,
    "lastCheck": "2024-01-01T10:30:00Z"
  },
  "email": {
    "status": "HEALTHY",
    "responseTime": 200,
    "lastCheck": "2024-01-01T10:30:00Z"
  },
  "notifications": {
    "slack": {"status": "HEALTHY", "responseTime": 100},
    "webhook": {"status": "HEALTHY", "responseTime": 80}
  }
}
```

### **Get System Logs**
```http
GET /admin/system/logs?page=0&size=50&level=ERROR&source=PaymentService
Authorization: Bearer {admin_token}
```

### **Search System Logs**
```http
GET /admin/system/logs/search?query=payment&page=0&size=50
Authorization: Bearer {admin_token}
```

### **Get Log Statistics**
```http
GET /admin/system/logs/statistics?from=2024-01-01&to=2024-01-31
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "totalLogs": 1000,
  "logsByLevel": {
    "INFO": 800,
    "WARN": 150,
    "ERROR": 50
  },
  "logsBySource": {
    "PaymentService": 300,
    "AuthService": 200,
    "SystemController": 500
  },
  "errorRate": 5.0,
  "timeRange": {
    "from": "2024-01-01T00:00:00Z",
    "to": "2024-01-31T23:59:59Z"
  }
}
```

### **Export System Logs**
```http
GET /admin/system/logs/export?format=csv&level=ERROR
Authorization: Bearer {admin_token}
```

### **Get System Alerts**
```http
GET /admin/system/alerts?page=0&size=20&severity=WARNING
Authorization: Bearer {admin_token}
```

### **Acknowledge System Alert**
```http
POST /admin/system/alerts/{alertId}/acknowledge
Authorization: Bearer {admin_token}
```

### **Get System Configuration**
```http
GET /admin/system/configuration
Authorization: Bearer {admin_token}
```

### **Update System Configuration**
```http
PUT /admin/system/configuration
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "maxConnections": 200,
  "timeoutSeconds": 60,
  "retryAttempts": 3,
  "logLevel": "INFO",
  "alertThresholds": {
    "memoryUsage": 85.0,
    "cpuUsage": 80.0,
    "responseTime": 5.0
  }
}
```

---

## 🔄 **WebSocket Events**

### **Connection**
```javascript
const ws = new WebSocket('wss://api.nps.payaza.africa/ws?token=jwt_token');

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  switch (data.type) {
    case 'ALERT_CREATED':
      // Handle new alert
      break;
    case 'TRANSACTION_UPDATED':
      // Handle transaction status update
      break;
    case 'SYSTEM_STATUS':
      // Handle system health update
      break;
  }
};
```

### **Event Types**
- `ALERT_CREATED` - New alert generated
- `ALERT_RESOLVED` - Alert resolved
- `TRANSACTION_UPDATED` - Transaction status changed
- `SYSTEM_STATUS` - System health status update
- `CLIENT_ACTIVITY` - Client activity update

---

This comprehensive API documentation provides all the information needed for frontend development and third-party integrations with the NPS system.