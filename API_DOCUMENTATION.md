# NPS Integration Service API Documentation

## Overview

The Nigerian Payment Stack (NPS) Integration Service provides a comprehensive REST API for integrating with the Nigerian Payment Stack system - the backbone of Nigeria's inter-bank payment infrastructure. This service implements ISO 20022-compliant messaging for Electronic Funds Transactions (EFT) and account-based switching, supporting asynchronous message processing and dual encryption modes.

## Base URL

```
http://localhost:8080
```

## Authentication

The API uses API key authentication for protected endpoints. Include the API key in the request header:

```
X-API-Key: your-api-key
```

## NPS Message Flows

### A. Identification Verification Flow (acmt.023/acmt.024)

1. **Step 1**: Originator initiates credit transfer (pain.001)
2. **Step 2**: Originator Bank generates ID verification request (acmt.023) → NPS
3. **Step 3**: NPS performs technical validation
4. **Step 4**: NPS delivers payment (acmt.023) to Beneficiary Bank
5. **Step 5**: Beneficiary Bank verifies and delivers ID verification report (acmt.024) → NPS
6. **Step 6**: NPS performs technical validation
7. **Step 7**: NPS delivers ID verification report (acmt.024) to Originator Bank

### B. Payment Request Flow (pacs.008/pacs.002)

#### Approved Payment Flow
1. **Step 1**: Originator initiates credit transfer (pain.001)
2. **Step 2**: Originator Bank generates payment (pacs.008) → NPS
3. **Step 3**: NPS performs technical validation
4. **Step 4**: NPS delivers payment (pacs.008) to Beneficiary Bank
5. **Step 5**: Beneficiary Bank verifies and delivers authorization (pacs.002/ACSC) → NPS
6. **Step 6**: NPS performs technical validation
7. **Step 7**: NPS delivers status (pacs.002/ACSC) to Originator

#### Declined Payment Flow
1. **Step 1**: Originator initiates credit transfer (pain.001)
2. **Step 2**: Originator Bank generates payment (pacs.008) → NPS
3. **Step 3**: NPS performs technical validation
4. **Step 4**: NPS delivers payment (pacs.008) to Beneficiary Bank
5. **Step 5**: Beneficiary Bank verifies and declines (pacs.002/RJCT) → NPS
6. **Step 6**: NPS performs technical validation
7. **Step 7**: NPS delivers status (pacs.002/RJCT) to Originator

#### Timeout Payment Flow
1. **Step 1**: Originator initiates credit transfer (pain.001)
2. **Step 2**: Originator Bank generates payment (pacs.008) → NPS
3. **Step 3**: NPS performs technical validation
4. **Step 4**: NPS delivers payment (pacs.008) to Beneficiary Bank (no response)
5. **Step 5**: NPS delivers timeout status (pacs.002/RJCT) to Beneficiary Bank
6. **Step 6**: NPS delivers timeout status (pacs.002/RJCT) to Originator

### C. Payment Status Request Flow (pacs.028)

1. **Step 1**: Bank sends payment status request (pacs.028) → NPS
2. **Step 2**: NPS validates request and original transaction
3. **Step 3**: If transaction not found, NPS returns HTTP 404
4. **Step 4**: NPS delivers requested payment status (pacs.002)

## API Endpoints

### 1. NPS Identification Verification

#### Submit Identification Verification Request (acmt.023)
**POST** `/api/v1/nps/identification/verify`

Submit an identification verification request to NPS for account verification. This endpoint accepts JSON requests and converts them to ISO 20022 XML internally.

**Request Body:** (JSON format)
```json
{
  "accountId": "1234567890",
  "accountOwnerName": "John Doe",
  "beneficiaryBankId": "002",
  "originalTransactionRef": "TXN123456789",
  "additionalInformation": "Account verification for payment processing"
}
```

**Response:**
```
Identification verification request submitted successfully. Verification results will be received via callback.
```

#### Receive Identification Verification Report (acmt.024)
**POST** `/api/v1/nps/identification/callback/report`

This is the callback endpoint that NPS calls with verification results (asynchronous processing).

**Request Body:** (ISO 20022 XML format)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:acmt.024.001.02">
  <IdVrfctnRpt>
    <MsgHdr>
      <MsgId>MSG123456789</MsgId>
      <CreDtTm>2024-01-15T10:35:00</CreDtTm>
    </MsgHdr>
    <RptOrErr>
      <Rpt>
        <VrfctnSts>
          <Sts>VERIFIED</Sts>
          <Rsn>Account details verified successfully</Rsn>
        </VrfctnSts>
      </Rpt>
    </RptOrErr>
  </IdVrfctnRpt>
</Document>
```

**Response:**
```
Identification verification report received and processed
```

### 2. NPS Payment Processing

#### Submit Payment Request (pacs.008)
**POST** `/api/v1/nps/payments/submit`

Submit a payment request to NPS for credit transfer processing. This endpoint accepts JSON requests and converts them to ISO 20022 XML internally.

**Request Body:** (JSON format)
```json
{
  "endToEndId": "E2E123456789",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "amount": 1000.00,
  "currency": "NGN",
  "debtorName": "John Doe",
  "creditorName": "Jane Smith",
  "remittanceInfo": "Payment for services",
  "debtorBankId": "001",
  "creditorBankId": "002",
  "instructionId": "INSTR123456789",
  "transactionId": "TXN123456789"
}
```

**Response:**
```
Payment request submitted successfully. Payment status will be received via callback.
```

#### Receive Payment Status Report (pacs.002)
**POST** `/api/v1/nps/payments/callback/status-report`

This is the callback endpoint that NPS calls with payment status results (asynchronous processing).

**Request Body:** (ISO 20022 XML format)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>MSG123456790</MsgId>
      <CreDtTm>2024-01-15T10:35:00</CreDtTm>
      <OrgnlMsgId>MSG123456789</OrgnlMsgId>
      <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>
    </GrpHdr>
    <TxInfAndSts>
      <StsId>STAT123456789</StsId>
      <OrgnlEndToEndId>E2E123456789</OrgnlEndToEndId>
      <TxSts>ACSC</TxSts>
      <AccptncDtTm>2024-01-15T10:35:00</AccptncDtTm>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

**Response:**
```
Payment status report received and processed
```

#### Request Payment Status (pacs.028)
**POST** `/api/v1/nps/payments/status-request`

Request the status of a previously sent payment instruction. This endpoint accepts JSON requests and converts them to ISO 20022 XML internally.

**Request Body:** (JSON format)
```json
{
  "originalMessageId": "MSG123456789",
  "originalEndToEndId": "E2E123456789",
  "originalInstructionId": "INSTR123456789",
  "originalTransactionId": "TXN123456789",
  "originalUETR": "UETR123456789",
  "originalClearSystemReference": "CLR123456789"
}
```

**Response:** (JSON format)
```json
{
  "messageId": "MSG123456790",
  "endToEndId": "E2E123456789",
  "instructionId": "INSTR123456789",
  "transactionId": "TXN123456789",
  "status": "ACSC",
  "statusCode": "00",
  "statusReason": "Payment accepted and settled",
  "amount": 1000.00,
  "currency": "NGN",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "debtorName": "John Doe",
  "creditorName": "Jane Smith",
  "remittanceInfo": "Payment for services",
  "createdAt": "2024-01-15T10:30:00",
  "processedAt": "2024-01-15T10:35:00",
  "npsReference": "NPS123456789",
  "clearingSystemReference": "CLR123456789"
}
```

#### Create Payment Request (Simplified)
**POST** `/api/v1/nps/payments/create-payment`

Create and submit a payment request using JSON format.

**Request Body:** (JSON format)
```json
{
  "endToEndId": "E2E123456789",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "amount": 1000.00,
  "currency": "NGN",
  "debtorName": "John Doe",
  "creditorName": "Jane Smith",
  "remittanceInfo": "Payment for services",
  "debtorBankId": "001",
  "creditorBankId": "002"
}
```

**Response:**
```
Payment request created and submitted successfully. Payment status will be received via callback.
```

#### Request Payment Status (Simplified)
**POST** `/api/v1/nps/payments/request-status`

Create and submit a payment status request using JSON format.

**Request Body:** (JSON format)
```json
{
  "originalMessageId": "MSG123456789",
  "originalEndToEndId": "E2E123456789"
}
```

**Response:** Payment Status Report (JSON format)
```json
{
  "messageId": "MSG123456790",
  "endToEndId": "E2E123456789",
  "status": "ACSC",
  "statusCode": "00",
  "statusReason": "Payment accepted and settled",
  "amount": 1000.00,
  "currency": "NGN",
  "processedAt": "2024-01-15T10:35:00"
}
```

#### Get Payment Status by Transaction ID
**GET** `/api/v1/payments/transaction/{transactionId}`

Retrieve the status of a payment by transaction ID.

#### Cancel Payment
**POST** `/api/v1/payments/{paymentId}/cancel`

Cancel a pending or processing payment.

**Response:**
```json
{
  "paymentId": "PAY123456789",
  "transactionId": "TXN123456789",
  "status": "CANCELLED",
  "responseCode": "00",
  "responseMessage": "Payment cancelled successfully",
  "createdAt": "2024-01-15T10:29:45"
}
```

#### Get Payments by Status
**GET** `/api/v1/payments/status/{status}`

Retrieve all payments with a specific status.

**Parameters:**
- `status`: PaymentStatus enum (PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, TIMEOUT, REJECTED)

### 2. Monitoring Endpoints

#### Health Check
**GET** `/api/v1/payments/health`

Check if the payment service is running.

**Response:**
```
NPS Payment Service is running
```

#### System Health
**GET** `/api/v1/monitoring/health`

Get detailed system health information.

**Response:**
```json
{
  "status": "UP",
  "message": null,
  "databaseStatus": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "totalPayments": 150
}
```

#### Payment Statistics
**GET** `/api/v1/monitoring/statistics`

Get payment processing statistics.

**Response:**
```json
{
  "totalPayments": 150,
  "successfulPayments": 120,
  "failedPayments": 20,
  "pendingPayments": 5,
  "processingPayments": 5
}
```

### 3. Callback Endpoints

#### Payment Status Callback
**POST** `/api/v1/callbacks/payment-status`

Handle payment status notifications from NPS.

**Request Body:**
```json
{
  "paymentId": "PAY123456789",
  "status": "SUCCESS",
  "responseCode": "00",
  "responseMessage": "Payment successful",
  "npsReference": "NPS123456789"
}
```

**Response:**
```
Callback processed successfully
```

#### Webhook Verification
**GET** `/api/v1/callbacks/webhook/verify`

Verify webhook endpoint.

**Parameters:**
- `challenge`: Challenge string for verification

## Data Models

### PaymentRequestDto

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| paymentId | String | Yes | Unique payment identifier |
| transactionId | String | Yes | Unique transaction identifier |
| senderAccount | String | Yes | Sender account number |
| receiverAccount | String | Yes | Receiver account number |
| amount | BigDecimal | Yes | Payment amount (minimum 0.01) |
| currency | String | Yes | Currency code (3 characters, NGN only) |
| paymentPurpose | String | No | Description of payment purpose (max 500 chars) |
| paymentType | PaymentType | Yes | Type of payment |
| referenceNumber | String | No | Reference number (max 35 chars) |

### PaymentResponseDto

| Field | Type | Description |
|-------|------|-------------|
| paymentId | String | Payment identifier |
| transactionId | String | Transaction identifier |
| npsReference | String | NPS system reference |
| status | PaymentStatus | Current payment status |
| responseCode | String | Response code from NPS |
| responseMessage | String | Response message from NPS |
| amount | BigDecimal | Payment amount |
| currency | String | Currency code |
| processedAt | LocalDateTime | When payment was processed |
| createdAt | LocalDateTime | When payment was created |

### PaymentStatus Enum

| Value | Description |
|-------|-------------|
| PENDING | Payment is pending processing |
| PROCESSING | Payment is being processed |
| SUCCESS | Payment completed successfully |
| FAILED | Payment failed |
| CANCELLED | Payment was cancelled |
| TIMEOUT | Payment timed out |
| REJECTED | Payment was rejected |

### PaymentType Enum

| Value | Description |
|-------|-------------|
| TRANSFER | Account transfer |
| PAYMENT | General payment |
| COLLECTION | Payment collection |
| REMITTANCE | Remittance payment |
| SALARY | Salary payment |
| BULK_TRANSFER | Bulk transfer |
| BILL_PAYMENT | Bill payment |

## Error Handling

### Error Response Format

```json
{
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "Validation failed",
  "details": {
    "amount": "Amount must be greater than zero",
    "currency": "Currency is required"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| VALIDATION_ERROR | Request validation failed |
| INVALID_ARGUMENT | Invalid argument provided |
| INVALID_STATE | Invalid state for operation |
| PAYMENT_NOT_FOUND | Payment not found |
| DUPLICATE_PAYMENT | Payment ID already exists |
| API_ERROR | NPS API error |
| SYSTEM_ERROR | Internal system error |

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |
| 500 | Internal Server Error |

## Rate Limiting

The API implements rate limiting to prevent abuse. Default limits:
- 100 requests per minute per API key
- 1000 requests per hour per API key

## Security

- All API endpoints require authentication via API key
- Sensitive data is encrypted in transit and at rest
- Request signatures are validated for critical operations
- CORS is configured for cross-origin requests

## Testing

### Sample cURL Commands

#### Process Payment
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "paymentId": "PAY123456789",
    "transactionId": "TXN123456789",
    "senderAccount": "1234567890",
    "receiverAccount": "0987654321",
    "amount": 1000.00,
    "currency": "NGN",
    "paymentPurpose": "Test payment",
    "paymentType": "TRANSFER",
    "referenceNumber": "REF123456"
  }'
```

#### Get Payment Status
```bash
curl -X GET http://localhost:8080/api/v1/payments/PAY123456789 \
  -H "X-API-Key: your-api-key"
```

#### Health Check
```bash
curl -X GET http://localhost:8080/api/v1/payments/health
```

## Support

For technical support or questions about the API, please contact the development team.
