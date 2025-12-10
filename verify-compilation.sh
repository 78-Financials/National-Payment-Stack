#!/bin/bash

echo "=== NPS Backend Compilation Verification ==="
echo ""

# Check Java version
echo "1. Checking Java version..."
if command -v java &> /dev/null; then
    java -version
else
    echo "❌ Java not found"
    exit 1
fi
echo ""

# Check Maven version
echo "2. Checking Maven version..."
if command -v mvn &> /dev/null; then
    mvn -version
    MAVEN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    echo "Using Maven wrapper..."
    ./mvnw -version
    MAVEN_CMD="./mvnw"
else
    echo "❌ Neither Maven nor Maven wrapper found"
    exit 1
fi
echo ""

# Clean and compile
echo "3. Cleaning and compiling..."
$MAVEN_CMD clean compile -q
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed!"
    exit 1
fi
echo ""

# Run tests
echo "4. Running tests..."
$MAVEN_CMD test -q
if [ $? -eq 0 ]; then
    echo "✅ Tests passed!"
else
    echo "❌ Tests failed!"
    exit 1
fi
echo ""

echo "🎉 All checks passed! Backend is ready."
