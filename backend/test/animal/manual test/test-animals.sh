#!/bin/bash

BASE_URL="http://localhost:8080"

ANIMAL_TESTS=0
PRODUCTION_TESTS=0
FEED_TESTS=0
USER_TESTS=0

FAILED_TESTS=()

function run_test() {
  NAME=$1
  EXPECTED_STATUS=$2
  CMD=$3

  echo "$NAME"

  RESPONSE=$(eval "$CMD -w '\n%{http_code}'")
  BODY=$(echo "$RESPONSE" | sed '$d')
  STATUS=$(echo "$RESPONSE" | tail -n1)

  echo "$BODY" | jq

  if [ "$STATUS" != "$EXPECTED_STATUS" ]; then
    echo "❌ FAILED: expected $EXPECTED_STATUS but got $STATUS"
    FAILED_TESTS+=("$NAME (expected $EXPECTED_STATUS got $STATUS)")
  else
    echo "✅ PASSED"
  fi

  echo "----------------------------------------"
}

echo "========================================"
echo "USER FLOW"
echo "========================================"

run_test "1. CREATE USER" 201 \
"curl -s -X POST $BASE_URL/users \
-H 'Content-Type: application/json' \
-d '{
  \"name\": \"Joao\",
  \"email\": \"joao@test.com\",
  \"role\": \"ADMIN\"
}'"
((USER_TESTS++))

USER_ID=$(curl -s $BASE_URL/users | jq -r '.[0].id')
echo "Generated USER_ID: $USER_ID"

run_test "2. GET ALL USERS" 200 \
"curl -s $BASE_URL/users"
((USER_TESTS++))

echo "========================================"
echo "ANIMAL FLOW"
echo "========================================"

run_test "3. CREATE ANIMAL" 201 \
"curl -s -X POST $BASE_URL/animals \
-H 'Content-Type: application/json' \
-d '{
  \"tag\": \"BR-002\",
  \"breed\": \"Holstein\",
  \"birthDate\": \"2022-05-10\",
  \"farmId\": \"farm-123\"
}'"
((ANIMAL_TESTS++))

ANIMAL_ID=$(curl -s $BASE_URL/animals | jq -r '.[0].id')
echo "Generated ANIMAL_ID: $ANIMAL_ID"

run_test "4. GET ALL ANIMALS" 200 \
"curl -s $BASE_URL/animals"
((ANIMAL_TESTS++))

echo "========================================"
echo "PRODUCTION FLOW"
echo "========================================"

run_test "5. CREATE PRODUCTION" 201 \
"curl -s -X POST $BASE_URL/productions \
-H 'Content-Type: application/json' \
-d '{
  \"animalId\": \"$ANIMAL_ID\",
  \"date\": \"2024-03-20\",
  \"quantity\": 15.5,
  \"userId\": \"$USER_ID\"
}'"
((PRODUCTION_TESTS++))

PRODUCTION_ID=$(curl -s $BASE_URL/productions | jq -r '.[0].id')
echo "Generated PRODUCTION_ID: $PRODUCTION_ID"

run_test "6. CREATE INVALID PRODUCTION" 400 \
"curl -s -X POST $BASE_URL/productions \
-H 'Content-Type: application/json' \
-d '{
  \"animalId\": \"$ANIMAL_ID\",
  \"date\": \"2024-03-20\",
  \"quantity\": -5,
  \"userId\": \"$USER_ID\"
}'"
((PRODUCTION_TESTS++))

run_test "7. GET ALL PRODUCTIONS" 200 \
"curl -s $BASE_URL/productions"
((PRODUCTION_TESTS++))

run_test "8. GET PRODUCTION BY ID" 200 \
"curl -s $BASE_URL/productions/$PRODUCTION_ID"
((PRODUCTION_TESTS++))

run_test "9. GET INVALID PRODUCTION" 404 \
"curl -s $BASE_URL/productions/invalid-id"
((PRODUCTION_TESTS++))

echo "========================================"
echo "UPDATE FLOW"
echo "========================================"

run_test "10. UPDATE PRODUCTION" 200 \
"curl -s -X PUT $BASE_URL/productions/$PRODUCTION_ID \
-H 'Content-Type: application/json' \
-d '{
  \"quantity\": 20,
  \"userId\": \"$USER_ID\"
}'"
((PRODUCTION_TESTS++))

run_test "11. UPDATE INVALID PRODUCTION" 400 \
"curl -s -X PUT $BASE_URL/productions/$PRODUCTION_ID \
-H 'Content-Type: application/json' \
-d '{
  \"quantity\": -10,
  \"userId\": \"$USER_ID\"
}'"
((PRODUCTION_TESTS++))

run_test "12. UPDATE NON EXISTENT" 404 \
"curl -s -X PUT $BASE_URL/productions/non-existent-id \
-H 'Content-Type: application/json' \
-d '{
  \"quantity\": 10,
  \"userId\": \"$USER_ID\"
}'"
((PRODUCTION_TESTS++))

echo "========================================"
echo "FILTER FLOW"
echo "========================================"

run_test "13. FILTER BY ANIMAL" 200 \
"curl -s \"$BASE_URL/productions?animalId=$ANIMAL_ID\""
((PRODUCTION_TESTS++))

run_test "14. FILTER BY DATE" 200 \
"curl -s \"$BASE_URL/productions?date=2024-03-20\""
((PRODUCTION_TESTS++))

run_test "15. FILTER COMBINED" 200 \
"curl -s \"$BASE_URL/productions?animalId=$ANIMAL_ID&date=2024-03-20\""
((PRODUCTION_TESTS++))

run_test "16. FILTER EMPTY" 200 \
"curl -s \"$BASE_URL/productions?animalId=invalid&date=2020-01-01\""
((PRODUCTION_TESTS++))

echo "========================================"
echo "AGGREGATION FLOW"
echo "========================================"

run_test "17. TOTAL PRODUCTION" 200 \
"curl -s \"$BASE_URL/productions/summary/by-animal?animalId=$ANIMAL_ID\""
((PRODUCTION_TESTS++))

echo "========================================"
echo "FEED FLOW"
echo "========================================"

run_test "18. CREATE FEED TYPE" 201 \
"curl -s -X POST $BASE_URL/feed-types \
-H 'Content-Type: application/json' \
-d '{\"name\": \"Silagem\", \"costPerKg\": 1.5}'"
((FEED_TESTS++))

FEED_TYPE_ID=$(curl -s $BASE_URL/feed-types | jq -r '.[0].id')
echo "Generated FEED_TYPE_ID: $FEED_TYPE_ID"

run_test "19. CREATE FEEDING" 201 \
"curl -s -X POST $BASE_URL/feedings \
-H 'Content-Type: application/json' \
-d '{
  \"animalId\": \"$ANIMAL_ID\",
  \"feedTypeId\": \"$FEED_TYPE_ID\",
  \"date\": \"2024-03-20\",
  \"quantity\": 10,
  \"userId\": \"$USER_ID\"
}'"
((FEED_TESTS++))

run_test "20. CREATE INVALID FEEDING" 400 \
"curl -s -X POST $BASE_URL/feedings \
-H 'Content-Type: application/json' \
-d '{
  \"animalId\": \"$ANIMAL_ID\",
  \"feedTypeId\": \"$FEED_TYPE_ID\",
  \"date\": \"2024-03-20\",
  \"quantity\": -5,
  \"userId\": \"$USER_ID\"
}'"
((FEED_TESTS++))

echo "========================================"
echo "PROFIT FLOW"
echo "========================================"

run_test "21. GET PROFIT" 200 \
"curl -s \"$BASE_URL/productions/summary/profit/by-animal?animalId=$ANIMAL_ID\""
((PRODUCTION_TESTS++))

run_test "22. PROFIT INVALID ANIMAL" 404 \
"curl -s \"$BASE_URL/productions/summary/profit/by-animal?animalId=invalid-id\""
((PRODUCTION_TESTS++))

echo "========================================"
echo "SUMMARY"
echo "========================================"

TOTAL_TESTS=$((ANIMAL_TESTS + PRODUCTION_TESTS + FEED_TESTS + USER_TESTS))

echo "User tests: $USER_TESTS"
echo "Animal tests: $ANIMAL_TESTS"
echo "Production tests: $PRODUCTION_TESTS"
echo "Feed tests: $FEED_TESTS"
echo "Total tests: $TOTAL_TESTS"
echo "Failures: ${#FAILED_TESTS[@]}"

if [ ${#FAILED_TESTS[@]} -ne 0 ]; then
  echo "❌ FAILED TESTS:"
  for test in "${FAILED_TESTS[@]}"; do
    echo "- $test"
  done
  exit 1
else
  echo "✅ ALL TESTS PASSED"
fi