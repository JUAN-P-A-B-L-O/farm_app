#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================"
echo "ANIMAL FLOW"
echo "========================================"

echo "1. CREATE ANIMAL"
CREATE_RESPONSE=$(curl -s -X POST $BASE_URL/animals \
  -H "Content-Type: application/json" \
  -d '{
    "tag": "BR-001",
    "breed": "Holstein",
    "birthDate": "2022-05-10",
    "farmId": "farm-123"
  }')

echo $CREATE_RESPONSE | jq

ANIMAL_ID=$(echo $CREATE_RESPONSE | jq -r '.id')
echo "Generated ANIMAL_ID: $ANIMAL_ID"

echo "2. GET ALL ANIMALS"
curl -s $BASE_URL/animals | jq

echo "========================================"
echo "PRODUCTION FLOW"
echo "========================================"

echo "3. CREATE PRODUCTION"
CREATE_PROD_RESPONSE=$(curl -s -X POST $BASE_URL/productions \
  -H "Content-Type: application/json" \
  -d "{
    \"animalId\": \"$ANIMAL_ID\",
    \"date\": \"2024-03-20\",
    \"quantity\": 15.5
  }")

echo $CREATE_PROD_RESPONSE | jq

PRODUCTION_ID=$(echo $CREATE_PROD_RESPONSE | jq -r '.id')
echo "Generated PRODUCTION_ID: $PRODUCTION_ID"

echo "4. CREATE INVALID PRODUCTION (SHOULD FAIL)"
curl -s -X POST $BASE_URL/productions \
  -H "Content-Type: application/json" \
  -d "{
    \"animalId\": \"$ANIMAL_ID\",
    \"date\": \"2024-03-20\",
    \"quantity\": -5
  }" | jq

echo "5. GET ALL PRODUCTIONS"
curl -s $BASE_URL/productions | jq

echo "6. GET PRODUCTION BY ID (IF IMPLEMENTED)"
curl -s $BASE_URL/productions/$PRODUCTION_ID | jq

echo "========================================"
echo "CLEANUP"
echo "========================================"

echo "7. DELETE ANIMAL"
DELETE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE $BASE_URL/animals/$ANIMAL_ID)
echo "Delete HTTP Status: $DELETE_STATUS"

echo "8. GET DELETED ANIMAL (SHOULD FAIL)"
curl -s $BASE_URL/animals/$ANIMAL_ID | jq