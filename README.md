# Nigerian Payment Stack (NPS) Integration Service

A comprehensive Spring Boot application for integrating with the Nigerian Payment Stack (NPS) system - the backbone of Nigeria's inter-bank payment infrastructure. This service implements ISO 20022-compliant messaging for Electronic Funds Transactions (EFT) and account-based switching.

## Features

- **Identification Verification**: Complete acmt.023/acmt.024 message flow for account verification
- **ISO 20022 Compliance**: Full support for ISO 20022 payment message standards
- **Asynchronous Processing**: Proper implementation of NPS asynchronous message processing model
- **Dual Encryption Support**: AES-256-CBC (NPS switch) and AES-256-GCM (participant) modes
- **Message Format Validation**: Technical validation including message format and access rights
- **Callback Endpoints**: Proper "inward" endpoints for receiving NPS notifications
- **Unknown Tag Support**: Flexible implementation to handle new functionality over NPS lifecycle
- **Security**: Cryptographic security with proper authentication and encryption
- **Database**: JPA/Hibernate integration with H2 database (development)
- **RESTful API**: Comprehensive REST endpoints for NPS operations

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database** (development)
- **WebFlux** (for reactive HTTP client)
- **BouncyCastle** (encryption)
- **Maven**

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Configuration

1. Set the following environment variables:

```bash
export NPS_CLIENT_ID=your-nps-client-id
export NPS_CLIENT_SECRET=your-nps-client-secret
export NPS_MERCHANT_ID=your-bank-code
export NPS_ENCRYPTION_KEY=your-32-char-aes-256-key-here
export NPS_CALLBACK_URL=http://localhost:8080/api/v1/nps/identification/callback/report
export ADMIN_PASSWORD=your-admin-password
```

2. Update `src/main/resources/application.properties` with your NPS API configuration.

### Running the Application

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Payment Operations

#### Process Payment
```http
POST /api/v1/payments
Content-Type: application/json
X-API-Key: your-api-key

{
  "paymentId": "PAY123456789",
  "transactionId": "TXN123456789",
  "senderAccount": "1234567890",
  "receiverAccount": "0987654321",
  "amount": 1000.00,
  "currency": "NGN",
  "paymentPurpose": "Payment for services",
  "paymentType": "TRANSFER",
  "referenceNumber": "REF123456"
}
```

#### Get Payment Status
```http
GET /api/v1/payments/{paymentId}
X-API-Key: your-api-key
```

#### Cancel Payment
```http
POST /api/v1/payments/{paymentId}/cancel
X-API-Key: your-api-key
```

### Monitoring Endpoints

#### Health Check
```http
GET /api/v1/payments/health
GET /api/v1/monitoring/health
```

#### Payment Statistics
```http
GET /api/v1/monitoring/statistics
X-API-Key: your-api-key
```

### Callback Endpoints

#### Payment Status Callback
```http
POST /api/v1/callbacks/payment-status
Content-Type: application/json

{
  "paymentId": "PAY123456789",
  "status": "SUCCESS",
  "responseCode": "00",
  "responseMessage": "Payment successful",
  "npsReference": "NPS123456789"
}
```

## Database Schema

The application uses the following main entities:

- **PaymentRequest**: Stores payment request details and status
- Payment statuses: PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, TIMEOUT, REJECTED
- Payment types: TRANSFER, PAYMENT, COLLECTION, REMITTANCE, SALARY, BULK_TRANSFER, BILL_PAYMENT

## Security Features

- API key authentication for protected endpoints
- Request/response encryption support
- CORS configuration
- Input validation and sanitization
- Comprehensive error handling

## Monitoring and Logging

- Real-time payment monitoring with scheduled tasks
- Comprehensive logging with configurable levels
- Health checks and system statistics
- Payment timeout handling
- Performance metrics

## Development

### Database Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:npsdb`
- Username: `sa`
- Password: (empty)

### Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Deployment

### Environment Variables

Ensure the following environment variables are set in production:

- `NPS_CLIENT_ID`
- `NPS_CLIENT_SECRET`
- `NPS_MERCHANT_ID`
- `NPS_ENCRYPTION_KEY`
- `NPS_CALLBACK_URL`
- `ADMIN_PASSWORD`

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/nps-integration-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Error Handling

The application provides comprehensive error handling with:

- Validation errors with detailed field-level messages
- Business logic error handling
- System error handling with proper HTTP status codes
- Logging for debugging and monitoring

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team.
