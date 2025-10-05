# NIBSS Integration Guide

## Overview

This service provides a complete bidirectional integration with the Nigerian Inter-Bank Settlement System (NIBSS) National Payment Stack (NPS). It supports asynchronous message processing with full encryption, signature verification, and callback handling.

## Architecture

```
┌─────────────────┐    JSON    ┌──────────────────┐    XML (Signed/Encrypted)    ┌─────────────┐
│   Your System   │ ──────────► │   NPS Wrapper    │ ───────────────────────────► │   NIBSS     │
│                 │            │     Service      │                              │             │
└─────────────────┘            └──────────────────┘                              └─────────────┘
        ▲                               ▲                                                  │
        │                               │                                                  │
        │                               │                                                  ▼
        │                               │                                         ┌─────────────┐
        │                               │                                         │   NIBSS     │
        │                               │                                         │  Callbacks  │
        │                               │                                         └─────────────┘
        │                               │                                                  │
        │                               │                                                  │
        │                               │ XML (Signed/Encrypted)                          │
        │                               │ ────────────────────────────────────────────────┘
        │                               │
        │                               ▼
        │                        ┌──────────────────┐
        │                        │  Callback        │
        │                        │  Controllers     │
        │                        └──────────────────┘
        │                               │
        │                               │ JSON
        │                               ▼
        └─────────────────────────┌──────────────────┐
                                  │  Your System     │
                                  │  Integration     │
                                  └──────────────────┘
```

## Supported Message Types

### Outbound Messages (Your System → NIBSS)

1. **ACMT.023** - Identification Verification Request
2. **ACMT.024** - Identification Verification Report  
3. **PACS.008** - Payment Request
4. **PACS.002** - Payment Status Report
5. **PACS.028** - Payment Status Request

### Inbound Messages (NIBSS → Your System)

1. **ACMT.024** - Identification Verification Report (Callback)
2. **PACS.002** - Payment Status Report (Callback)
3. **PACS.028** - Payment Status Request Response (Callback)
4. **PACS.008** - Payment Request Response (Callback)

## Security Features

### XML Digital Signature (XMLDSig)
- **Algorithm**: RSA-SHA256
- **Canonicalization**: W3C C14n (`http://www.w3.org/TR/2001/REC-xml-c14n-20010315`)
- **Digest**: SHA-256
- **Format**: Enveloped signature

### XML Encryption
- **Content Encryption**: AES-256-CBC (NIBSS standard)
- **Key Encryption**: RSA-OAEP-MGF1P
- **Mode**: Document-level encryption
- **Compatibility**: Supports both AES-256-CBC and AES-256-GCM

### Asynchronous Processing
- All NIBSS requests are processed asynchronously
- HTTP 200 response indicates receipt by NIBSS, not final settlement
- Final outcomes communicated via callback notifications

## API Endpoints

### Outbound APIs (Your System → NIBSS)

#### ACMT.023 - Identification Verification Request
```http
POST /api/v1/acmt023/verify
Content-Type: application/json

{
  "messageId": "ACMT023-123456789",
  "institutionCode": "044",
  "accountNumber": "1234567890",
  "bankCode": "044",
  "accountName": "John Doe",
  "amount": 1000.00,
  "currency": "NGN",
  "referenceNumber": "REF-123456789",
  "creatorBankName": "Test Bank",
  "assignorBankName": "Test Bank",
  "assignorBicfi": "999058",
  "assignorMemberId": "044",
  "assigneeBicfi": "999057",
  "assigneeMemberId": "058"
}
```

#### PACS.008 - Payment Request
```http
POST /api/v1/pacs008/transfer
Content-Type: application/json

{
  "messageId": "PACS008-123456789",
  "transactionId": "TXN-123456789",
  "amount": 5000.00,
  "currency": "NGN",
  "senderAccountNumber": "1234567890",
  "receiverAccountNumber": "0987654321",
  "receiverBankCode": "058",
  "paymentPurpose": "Payment for services",
  "senderBicfi": "999058",
  "senderMemberId": "999058",
  "receiverMemberId": "999057",
  "instructionId": "INST-123456789",
  "endToEndId": "E2E-123456789",
  "settlementDate": "2024-01-01Z",
  "senderAccountName": "John Doe",
  "receiverAccountName": "Jane Smith",
  "debtorBvn": "2211232344",
  "creditorBvn": "2211232346",
  "transactionLocation": "01080652440N020900337921E",
  "nameEnquiryMsgId": "NE-123456789",
  "riskRating": "R000000000000000000B9"
}
```

### Inbound APIs (NIBSS → Your System)

#### Callback Endpoints
```http
POST /api/v1/nps/callback/acmt024
POST /api/v1/nps/callback/pacs002
POST /api/v1/nps/callback/pacs028
POST /api/v1/nps/callback/pacs008
Content-Type: application/xml

<!-- Signed and encrypted XML from NIBSS -->
```

## Configuration

### application.properties
```properties
# NPS Configuration
nps.base-url=https://nps.nibss-plc.com.ng
nps.client-id=your-nps-client-id
nps.client-secret=your-nps-client-secret
nps.merchant-id=your-bank-code
nps.timeout-seconds=30
nps.enable-encryption=true
nps.encryption-key=your-32-char-aes-256-key-here
nps.callback-url=http://your-domain.com/api/v1/nps/callback

# Endpoint URLs
nps.acmt023-endpoint=/acmt023
nps.acmt024-endpoint=/acmt024
nps.pacs008-endpoint=/pacs008
nps.pacs002-endpoint=/pacs002
nps.pacs028-endpoint=/pacs028
nps.participants-endpoint=/participants
```

## Message Flow

### Outbound Flow (Your System → NIBSS)

1. **Receive JSON Request** from your internal system
2. **Convert to ISO 20022 XML** using message templates
3. **Sign XML** using your private key (RSA-SHA256)
4. **Encrypt XML** using NIBSS public key (AES-256-CBC)
5. **Send to NIBSS** via HTTPS POST
6. **Return JSON Response** to your internal system

### Inbound Flow (NIBSS → Your System)

1. **Receive Encrypted XML** from NIBSS callback
2. **Decrypt XML** using your private key
3. **Verify Signature** using NIBSS public key
4. **Parse XML** and extract relevant data
5. **Process Result** (update database, send notifications, etc.)
6. **Return Acknowledgment** to NIBSS

## Error Handling

### HTTP Status Codes
- **200 OK**: Request received and processed successfully
- **400 Bad Request**: Invalid request format or missing required fields
- **401 Unauthorized**: Invalid authentication credentials
- **500 Internal Server Error**: Processing error

### Response Format
```json
{
  "messageId": "ACMT023-123456789",
  "status": "SUCCESS|FAILED|PENDING",
  "responseCode": "00|99|01",
  "responseMessage": "Human readable message",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

## Testing

### Run Integration Tests
```bash
mvn test -Dtest="NpsBidirectionalIntegrationTest"
```

### Test Individual Message Types
```bash
# Test ACMT.023
curl -X POST http://localhost:8080/api/v1/acmt023/verify \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "ACMT023-123456789",
    "institutionCode": "044",
    "accountNumber": "1234567890",
    "bankCode": "044",
    "accountName": "John Doe",
    "amount": 1000.00,
    "currency": "NGN",
    "referenceNumber": "REF-123456789"
  }'

# Test PACS.008
curl -X POST http://localhost:8080/api/v1/pacs008/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "PACS008-123456789",
    "transactionId": "TXN-123456789",
    "amount": 5000.00,
    "currency": "NGN",
    "senderAccountNumber": "1234567890",
    "receiverAccountNumber": "0987654321",
    "receiverBankCode": "058",
    "paymentPurpose": "Payment for services"
  }'
```

## Security Considerations

### Key Management
- Store private keys securely (HSM, Azure Key Vault, AWS KMS)
- Rotate keys regularly according to NIBSS requirements
- Use separate keys for different environments (dev, test, prod)

### Network Security
- Use HTTPS for all communications
- Implement IP whitelisting for callback endpoints
- Use mutual TLS (mTLS) if required by NIBSS

### Monitoring
- Log all message exchanges
- Monitor for failed signature verifications
- Set up alerts for callback failures
- Track message processing times

## Deployment

### Docker Deployment
```bash
# Build image
docker build -t nps-integration-service .

# Run container
docker run -p 8080:8080 \
  -e NPS_CLIENT_ID=your-client-id \
  -e NPS_CLIENT_SECRET=your-client-secret \
  -e NPS_MERCHANT_ID=your-merchant-id \
  nps-integration-service
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
      - name: nps-integration-service
        image: nps-integration-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: NPS_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: nps-secrets
              key: client-id
        - name: NPS_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: nps-secrets
              key: client-secret
        - name: NPS_MERCHANT_ID
          valueFrom:
            secretKeyRef:
              name: nps-secrets
              key: merchant-id
```

## Troubleshooting

### Common Issues

1. **Signature Verification Failed**
   - Check if NIBSS public key is correct
   - Verify XML canonicalization is correct
   - Ensure signature algorithm matches NIBSS requirements

2. **Decryption Failed**
   - Verify your private key is correct
   - Check if encryption method matches (AES-256-CBC vs AES-256-GCM)
   - Ensure key encryption method is RSA-OAEP-MGF1P

3. **Callback Not Received**
   - Check if callback URL is accessible from NIBSS
   - Verify SSL certificate is valid
   - Check firewall rules and network connectivity

4. **Message Format Errors**
   - Validate XML against ISO 20022 schemas
   - Check if all required fields are present
   - Verify field formats and data types

### Logging

Enable debug logging for troubleshooting:
```properties
logging.level.com.payaza.nps=DEBUG
logging.level.org.apache.xml.security=DEBUG
logging.level.org.bouncycastle=DEBUG
```

## Support

For technical support and questions:
- Check NIBSS documentation and specifications
- Review application logs for detailed error information
- Contact NIBSS support for API-related issues

## Version History

- **v1.0.0** - Initial implementation with basic message support
- **v1.1.0** - Added bidirectional communication and callback handling
- **v1.2.0** - Enhanced security with proper signature and encryption
- **v1.3.0** - Added comprehensive error handling and monitoring
