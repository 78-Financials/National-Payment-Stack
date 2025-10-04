# NPS Validation Test Guide

## Overview

This guide provides comprehensive instructions for running the NPS (National Payment Stack) validation test suite. The test suite validates all three phases required by NIBSS for production credentials approval.

## Prerequisites

### Environment Setup
- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.2.0
- Access to NPS test environment
- Valid test credentials from NIBSS

### Dependencies
All required dependencies are included in the `pom.xml` file:
- Spring Boot Test
- WireMock for mock testing
- TestContainers for integration testing
- BouncyCastle for encryption

## Validation Phases

### Phase 1: Outbound Message Validation (Request)

**Purpose**: Validate the ability to send properly formatted and encrypted Pacs.008 messages to NPS.

**Test Steps**:
1. **Get Valid Pacs.008 Message**: Validate message structure using NIBSS sample
2. **Sign & Encrypt Message**: Test XML Digital Signature (XMLDSig) and encryption
3. **Send to NPS**: Validate message transmission format

**Key Requirements**:
- Message must be properly formatted according to ISO 20022 standard
- W3C Canonicalization (C14n) before signing
- XML Digital Signature (XMLDSig) using RSA-SHA256
- Encryption using AES-256-CBC (NPS supported) or AES-256-GCM (participant preferred)
- Proper Base64 encoding for transmission

### Phase 2: Inbound Receipt Validation (Receipt)

**Purpose**: Validate the ability to receive and process inbound messages from NPS.

**Test Steps**:
1. **Receive Inbound Message**: Validate message receipt capability
2. **Decrypt Message**: Test decryption of encrypted messages
3. **Process Message**: Validate business logic processing
4. **Log Observation**: Ensure proper logging for NIBSS review

**Key Requirements**:
- Callback endpoints must be accessible
- Message decryption must work correctly
- Proper logging for 24-48 hour observation period
- Error handling for malformed messages

### Phase 3: Response to Inbound Message (Response)

**Purpose**: Validate the ability to send properly formatted Pacs.002 response messages.

**Test Steps**:
1. **Get Valid Pacs.002 Message**: Validate response message structure
2. **Sign & Encrypt Response**: Test XML Digital Signature and encryption
3. **Send Response**: Validate response transmission

**Key Requirements**:
- Proper message correlation with original Pacs.008
- W3C Canonicalization (C14n) before signing
- XML Digital Signature (XMLDSig) using RSA-SHA256
- Correct status reporting (ACSC, RJCT, NAUT)
- Same encryption and signature standards as Phase 1
- Proper reference to original transaction

## Running the Tests

### Option 1: Run All Validation Tests

```bash
# Navigate to project directory
cd /path/to/nps-integration-service

# Run all validation tests
mvn test -Dtest=NpsValidationTestRunner

# Or run specific test classes
mvn test -Dtest=Phase1OutboundValidationTest
mvn test -Dtest=Phase2InboundReceiptValidationTest
mvn test -Dtest=Phase3ResponseValidationTest
mvn test -Dtest=NibssXmlSignatureTest
mvn test -Dtest=NibssXmlEncryptionTest
mvn test -Dtest=NibssDocumentLevelEncryptionTest
mvn test -Dtest=NibssXmlSignatureAndEncryptionIntegrationTest
mvn test -Dtest=NpsIntegrationValidationTest
```

### Option 2: Run Individual Test Methods

```bash
# Run specific test methods
mvn test -Dtest=Phase1OutboundValidationTest#testStep1_GetValidPacs008Message
mvn test -Dtest=Phase1OutboundValidationTest#testStep2_SignAndEncryptMessage
mvn test -Dtest=Phase1OutboundValidationTest#testStep3_SendSignedEncryptedMessageToNIBSS
```

### Option 3: Run from IDE

1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Navigate to the test classes in `src/test/java/com/payaza/nps/validation/`
3. Right-click on the test class or method and select "Run"
4. Review test results in the IDE console

## Test Configuration

### Application Properties

Create `src/test/resources/application-test.properties`:

```properties
# Test Configuration
spring.profiles.active=test

# NPS Test Configuration
nps.base-url=https://test-nps.nibss-plc.com.ng
nps.client-id=test-client-id
nps.client-secret=test-client-secret
nps.merchant-id=test-merchant-id
nps.timeout-seconds=30
nps.enable-encryption=true
nps.encryption-key=test-encryption-key-32-chars-long
nps.callback-url=http://localhost:8080/api/v1/nps

# Test Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Data

Test data is provided in `NpsValidationTestData.java`:
- Valid Pacs.008 message (Phase 1)
- Valid Pacs.002 message (Phase 3)
- Sample JSON requests
- Test endpoints and credentials

## Expected Test Results

### Successful Test Execution

```
=== COMPLETE NPS VALIDATION FLOW TEST ===
🚀 Starting Phase 1: Outbound Message Validation
✅ Phase 1 completed: Outbound message validated and encrypted
📥 Starting Phase 2: Inbound Receipt Validation
✅ Phase 2 completed: Inbound message received and logged for observation
📤 Starting Phase 3: Response to Inbound Message
✅ Phase 3 completed: Response message validated and encrypted
✅ ALL NPS VALIDATION TESTS COMPLETED SUCCESSFULLY
📋 Test results ready for NIBSS submission
📋 Production credentials can be requested
```

### Test Coverage

The test suite covers:
- ✅ Message format validation
- ✅ XML Digital Signature (XMLDSig) implementation
- ✅ XML Encryption (XMLEnc) implementation
- ✅ Document-level encryption (entire Document element replaced with xenc:EncryptedData)
- ✅ Element-level encryption (specific XML elements encrypted)
- ✅ W3C Canonicalization (C14n) processing
- ✅ Encryption/decryption (AES-256-CBC, AES-256-GCM, and XML Encryption)
- ✅ Signature generation/verification (RSA-SHA256)
- ✅ Key encryption (RSA-OAEP for session keys)
- ✅ Payload encryption (AES-256-GCM for XML elements)
- ✅ Message correlation and traceability
- ✅ Error handling and recovery
- ✅ Performance and scalability
- ✅ Different payment scenarios (ACSC, RJCT, NAUT)
- ✅ Callback endpoint configuration
- ✅ Log observation capability

## Troubleshooting

### Common Issues

1. **Encryption Errors**
   ```
   Error: Could not decrypt message
   Solution: Verify encryption key and algorithm compatibility
   ```

2. **Signature Verification Failures**
   ```
   Error: Message signature verification failed
   Solution: Check RSA key pair generation and signature algorithm
   ```

3. **Message Format Issues**
   ```
   Error: Invalid XML structure
   Solution: Validate XML against ISO 20022 schema
   ```

4. **Network Connectivity**
   ```
   Error: Connection refused
   Solution: Verify NPS endpoint URLs and network access
   ```

### Debug Mode

Enable debug logging by adding to `application-test.properties`:

```properties
# Debug Configuration
logging.level.com.payaza.nps=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.bouncycastle=DEBUG
```

## NIBSS Submission

### Required Information

When submitting test results to NIBSS, include:

1. **Test Execution Report**
   - Test execution timestamp
   - All test results (PASSED/FAILED)
   - Performance metrics
   - Error handling validation

2. **Technical Specifications**
   - XML Digital Signature (XMLDSig) implementation
   - XML Encryption (XMLEnc) implementation
   - Document-level encryption (entire Document element replaced with xenc:EncryptedData)
   - Element-level encryption (specific XML elements encrypted)
   - W3C Canonicalization (C14n) support
   - Encryption algorithms used (AES-256-CBC, AES-256-GCM, XML Encryption)
   - Key encryption algorithms used (RSA-OAEP)
   - Signature algorithms used (RSA-SHA256)
   - Message formats supported (ISO 20022)
   - Callback endpoint details

3. **Environment Details**
   - Test IP address
   - Test port numbers
   - Callback URLs
   - Log retention period

4. **Contact Information**
   - Technical contact details
   - Business contact details
   - Support contact information

### Submission Format

Use the following template for NIBSS submission:

```
Subject: NPS Integration Validation Results - [Organization Name]

Dear NIBSS Team,

We have completed the NPS integration validation as per your requirements.
Please find the test results attached.

Organization: [Your Organization Name]
Contact Person: [Your Contact Name]
Email: [Your Email]
Phone: [Your Phone]

Validation Results:
✅ Phase 1: Outbound Message Validation - PASSED
✅ Phase 2: Inbound Receipt Validation - PASSED  
✅ Phase 3: Response to Inbound Message - PASSED

Technical Specifications:
- Encryption: AES-256-CBC and AES-256-GCM support
- Signature: RSA-SHA256
- Message Format: ISO 20022 (pacs.008, pacs.002)
- Transport: HTTPS with certificate-based authentication

Callback Endpoints:
- Payment Status: https://[your-domain]/api/v1/nps/payments/callback/status-report
- ID Verification: https://[your-domain]/api/v1/nps/identification/callback/report

We request the issuance of production credentials for the NPS integration.
All validation requirements have been met and documented.

Please review the test results and approve production access.

Best regards,
[Your Name]
[Your Organization]
```

## Security Considerations

### Key Management
- Store encryption keys securely
- Use environment variables for sensitive data
- Implement key rotation policies
- Never commit keys to version control

### Network Security
- Use HTTPS for all communications
- Implement certificate pinning
- Configure firewall rules appropriately
- Monitor network traffic

### Logging Security
- Sanitize sensitive data in logs
- Implement log encryption
- Set appropriate log retention periods
- Monitor log access

## Support and Maintenance

### Regular Testing
- Run validation tests before deployments
- Test after NPS system updates
- Validate new message types
- Performance testing under load

### Monitoring
- Monitor callback endpoint availability
- Track message processing times
- Monitor encryption/decryption performance
- Alert on signature verification failures

### Updates
- Keep dependencies updated
- Monitor NIBSS announcements
- Update test cases for new requirements
- Maintain backward compatibility

## Conclusion

This validation test suite ensures compliance with NIBSS NPS requirements and provides comprehensive coverage of all integration aspects. Regular execution of these tests will help maintain system reliability and compliance with NPS standards.

For questions or issues, please refer to the NIBSS documentation or contact the technical support team.
