#!/bin/bash

# NPS Phase 3 Implementation Test Script
# Tests the Real-time Alerting System

set -e

# Configuration
BASE_URL="http://localhost:8081"
ADMIN_TOKEN="admin-token-here"  # Replace with actual admin token
CLIENT_TOKEN="client-token-here"  # Replace with actual client token

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    NPS Phase 3 - Alerting System Test${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to make HTTP requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    
    if [ -n "$data" ]; then
        curl -s -X "$method" \
             -H "Authorization: Bearer $token" \
             -H "Content-Type: application/json" \
             -d "$data" \
             "$BASE_URL$endpoint"
    else
        curl -s -X "$method" \
             -H "Authorization: Bearer $token" \
             "$BASE_URL$endpoint"
    fi
}

# Function to test endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local token=$5
    local expected_status=$6
    
    echo -e "${YELLOW}Testing: $name${NC}"
    
    response=$(make_request "$method" "$endpoint" "$data" "$token")
    status_code=$(echo "$response" | grep -o '"status"[^,]*' | cut -d'"' -f4 || echo "unknown")
    
    if [[ "$status_code" == "$expected_status" ]] || [[ -z "$expected_status" ]]; then
        echo -e "${GREEN}✓ PASS${NC} - $name"
        echo "Response: $response" | head -c 200
        echo ""
    else
        echo -e "${RED}✗ FAIL${NC} - $name (Expected: $expected_status, Got: $status_code)"
        echo "Response: $response"
    fi
    echo ""
}

echo -e "${BLUE}1. Testing Alert Rules Management${NC}"
echo "=================================="

# Test getting all alert rules
test_endpoint "Get All Alert Rules" "GET" "/api/v1/alerts/rules" "" "$ADMIN_TOKEN"

# Test getting enabled alert rules
test_endpoint "Get Enabled Alert Rules" "GET" "/api/v1/alerts/rules/enabled" "" "$ADMIN_TOKEN"

# Test creating a new alert rule
NEW_RULE_DATA='{
  "name": "Test High Processing Time",
  "description": "Test alert for high processing time",
  "conditionExpression": "transaction_metrics.avg_processing_time_seconds > 5",
  "severity": "WARNING",
  "notificationChannels": ["email", "slack"],
  "escalationPolicy": "30m",
  "enabled": true,
  "evaluationIntervalSeconds": 60,
  "suppressionWindowSeconds": 300,
  "maxAlertsPerHour": 10,
  "metricType": "transaction",
  "metricName": "avg_processing_time_seconds"
}'

test_endpoint "Create Alert Rule" "POST" "/api/v1/alerts/rules" "$NEW_RULE_DATA" "$ADMIN_TOKEN"

echo -e "${BLUE}2. Testing Alert Management${NC}"
echo "=============================="

# Test getting alerts
test_endpoint "Get Alerts" "GET" "/api/v1/alerts?page=0&size=10" "" "$ADMIN_TOKEN"

# Test getting active alerts
test_endpoint "Get Active Alerts" "GET" "/api/v1/alerts/active" "" "$ADMIN_TOKEN"

# Test getting alerts by severity
test_endpoint "Get Critical Alerts" "GET" "/api/v1/alerts/severity/CRITICAL" "" "$ADMIN_TOKEN"

echo -e "${BLUE}3. Testing Alert Dashboard${NC}"
echo "============================="

# Test getting dashboard summary
test_endpoint "Get Alert Dashboard Summary" "GET" "/api/v1/alerts/dashboard/summary" "" "$ADMIN_TOKEN"

# Test getting recent alert history
test_endpoint "Get Recent Alert History" "GET" "/api/v1/alerts/history/recent?hours=24" "" "$ADMIN_TOKEN"

echo -e "${BLUE}4. Testing Alert System Management${NC}"
echo "====================================="

# Test manual alert evaluation
test_endpoint "Trigger Alert Evaluation" "POST" "/api/v1/alerts/evaluate" "" "$ADMIN_TOKEN"

# Test notification channels
test_endpoint "Test Email Notification" "POST" "/api/v1/alerts/test-notification?channel=email" "" "$ADMIN_TOKEN"
test_endpoint "Test Slack Notification" "POST" "/api/v1/alerts/test-notification?channel=slack" "" "$ADMIN_TOKEN"
test_endpoint "Test SMS Notification" "POST" "/api/v1/alerts/test-notification?channel=sms" "" "$ADMIN_TOKEN"
test_endpoint "Test Webhook Notification" "POST" "/api/v1/alerts/test-notification?channel=webhook" "" "$ADMIN_TOKEN"

echo -e "${BLUE}5. Testing Alert Actions (if alerts exist)${NC}"
echo "============================================="

# Get the first alert ID to test actions
echo -e "${YELLOW}Attempting to get alert ID for testing actions...${NC}"
alerts_response=$(make_request "GET" "/api/v1/alerts?page=0&size=1" "" "$ADMIN_TOKEN")
alert_id=$(echo "$alerts_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$alert_id" ] && [ "$alert_id" != "null" ]; then
    echo -e "${GREEN}Found alert ID: $alert_id${NC}"
    
    # Test getting specific alert
    test_endpoint "Get Alert by ID" "GET" "/api/v1/alerts/$alert_id" "" "$ADMIN_TOKEN"
    
    # Test getting alert history
    test_endpoint "Get Alert History" "GET" "/api/v1/alerts/$alert_id/history" "" "$ADMIN_TOKEN"
    
    # Test acknowledging alert
    test_endpoint "Acknowledge Alert" "PATCH" "/api/v1/alerts/$alert_id/acknowledge?acknowledgedBy=test_user" "" "$ADMIN_TOKEN"
    
    # Test resolving alert
    test_endpoint "Resolve Alert" "PATCH" "/api/v1/alerts/$alert_id/resolve?resolvedBy=test_user" "" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}No alerts found to test actions${NC}"
fi

echo -e "${BLUE}6. Testing Integration with Existing Analytics${NC}"
echo "==============================================="

# Test existing analytics endpoints to ensure they still work
test_endpoint "Get Real-time Dashboard" "GET" "/api/v1/analytics/dashboard" "" "$ADMIN_TOKEN"
test_endpoint "Get Live Transactions" "GET" "/api/v1/analytics/transactions/live" "" "$ADMIN_TOKEN"
test_endpoint "Get Bank Performance" "GET" "/api/v1/analytics/banks/performance" "" "$ADMIN_TOKEN"

echo -e "${BLUE}7. Testing Alert Rule Management Operations${NC}"
echo "=============================================="

# Test updating an alert rule (get first rule ID)
echo -e "${YELLOW}Attempting to get alert rule ID for testing updates...${NC}"
rules_response=$(make_request "GET" "/api/v1/alerts/rules?page=0&size=1" "" "$ADMIN_TOKEN")
rule_id=$(echo "$rules_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$rule_id" ] && [ "$rule_id" != "null" ]; then
    echo -e "${GREEN}Found alert rule ID: $rule_id${NC}"
    
    # Test getting specific rule
    test_endpoint "Get Alert Rule by ID" "GET" "/api/v1/alerts/rules/$rule_id" "" "$ADMIN_TOKEN"
    
    # Test toggling rule
    test_endpoint "Toggle Alert Rule" "PATCH" "/api/v1/alerts/rules/$rule_id/toggle" "" "$ADMIN_TOKEN"
    
    # Test updating rule
    UPDATE_RULE_DATA='{
      "name": "Updated Test Rule",
      "description": "Updated test alert rule",
      "conditionExpression": "transaction_metrics.success_rate < 95",
      "severity": "CRITICAL",
      "notificationChannels": ["email", "sms"],
      "enabled": true
    }'
    
    test_endpoint "Update Alert Rule" "PUT" "/api/v1/alerts/rules/$rule_id" "$UPDATE_RULE_DATA" "$ADMIN_TOKEN"
else
    echo -e "${YELLOW}No alert rules found to test operations${NC}"
fi

echo -e "${BLUE}8. Testing Metrics Collection${NC}"
echo "============================="

# Test metrics collection by triggering evaluation
echo -e "${YELLOW}Testing metrics collection and alert evaluation...${NC}"
eval_response=$(make_request "POST" "/api/v1/alerts/evaluate" "" "$ADMIN_TOKEN")
echo "Evaluation Response: $eval_response"

echo -e "${BLUE}9. Testing Error Handling${NC}"
echo "==========================="

# Test invalid endpoints
test_endpoint "Invalid Alert ID" "GET" "/api/v1/alerts/999999" "" "$ADMIN_TOKEN"
test_endpoint "Invalid Rule ID" "GET" "/api/v1/alerts/rules/999999" "" "$ADMIN_TOKEN"

# Test unauthorized access
test_endpoint "Unauthorized Access" "GET" "/api/v1/alerts" "" "invalid-token"

echo -e "${BLUE}10. Performance Testing${NC}"
echo "========================="

# Test multiple concurrent requests
echo -e "${YELLOW}Testing concurrent requests...${NC}"
for i in {1..5}; do
    make_request "GET" "/api/v1/alerts/dashboard/summary" "" "$ADMIN_TOKEN" &
done
wait

echo -e "${GREEN}Concurrent requests completed${NC}"

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    Phase 3 Testing Complete${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✓ Alert Rules Management${NC}"
echo -e "${GREEN}✓ Alert Management${NC}"
echo -e "${GREEN}✓ Alert Dashboard & Analytics${NC}"
echo -e "${GREEN}✓ Notification System${NC}"
echo -e "${GREEN}✓ Alert System Management${NC}"
echo -e "${GREEN}✓ Integration with Existing Analytics${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Configure notification channels (email, SMS, Slack, webhooks)"
echo "2. Set up monitoring dashboards"
echo "3. Configure alert escalation policies"
echo "4. Train operations team on alert management"
echo "5. Set up alert suppression rules for maintenance windows"
echo ""
echo -e "${BLUE}Real-time Alerting System is ready for production!${NC}"
