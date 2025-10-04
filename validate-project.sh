#!/bin/bash

echo "Validating NPS Project Structure..."

# Check if all required files exist
echo "Checking project files..."

required_files=(
    "pom.xml"
    "src/main/java/com/payaza/nps/NigerianPaymentStackApplication.java"
    "src/main/java/com/payaza/nps/config/NpsConfiguration.java"
    "src/main/java/com/payaza/nps/model/PaymentRequest.java"
    "src/main/java/com/payaza/nps/service/PaymentService.java"
    "src/main/java/com/payaza/nps/controller/PaymentController.java"
    "src/main/resources/application.properties"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file exists"
    else
        echo "✗ $file missing"
    fi
done

echo ""
echo "Project structure validation complete!"
echo ""
echo "To run the application:"
echo "1. Set environment variables:"
echo "   export NPS_CLIENT_ID=your-client-id"
echo "   export NPS_CLIENT_SECRET=your-client-secret"
echo "   export NPS_MERCHANT_ID=your-merchant-id"
echo "   export NPS_ENCRYPTION_KEY=your-encryption-key"
echo ""
echo "2. Run with Maven:"
echo "   mvn spring-boot:run"
echo ""
echo "3. Or build and run JAR:"
echo "   mvn clean package"
echo "   java -jar target/nps-integration-service-1.0.0.jar"
echo ""
echo "Application will be available at: http://localhost:8080"
echo "H2 Database Console: http://localhost:8080/h2-console"
