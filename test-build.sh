#!/bin/bash

echo "Testing NPS Backend Build..."

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed or not in PATH"
    echo "Trying Maven wrapper..."
    if [ -f "./mvnw" ]; then
        echo "Using Maven wrapper..."
        ./mvnw clean compile -U
    else
        echo "Neither Maven nor Maven wrapper found"
        exit 1
    fi
else
    echo "Using Maven..."
    mvn clean compile -U
fi

echo "Build test completed!"
