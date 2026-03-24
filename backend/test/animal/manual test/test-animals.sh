#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================"
echo "1. CREATE ANIMAL"
echo "========================================"

CREATE_RESPONSE=$(curl -s -X POST $BASE_URL/animals \
  -H "Content-Type: application/json" \
  -d '{
    "tag": "BR-001",
    "breed": "Holstein",
    "birthDate": "2022-05-10",
    "farmId": "farm-123"
  }')

echo $CREATE_RESPONSE | jq

# Extract ID
ANIMAL_ID=$(echo $CREATE_RESPONSE | jq -r '.id')

echo "Generated ID: $ANIMAL_ID"

echo "========================================"
echo "2. GET ALL ANIMALS"
echo "========================================"

curl -s $BASE_URL/animals | jq

echo "========================================"
echo "3. GET ANIMAL BY ID"
echo "========================================"

curl -s $BASE_URL/animals/$ANIMAL_ID | jq

echo "========================================"
echo "4. UPDATE ANIMAL"
echo "========================================"

UPDATE_RESPONSE=$(curl -s -X PUT $BASE_URL/animals/$ANIMAL_ID \
  -H "Content-Type: application/json" \
  -d '{
    "breed": "Jersey"
  }')

echo $UPDATE_RESPONSE | jq

echo "========================================"
echo "5. DELETE ANIMAL"
echo "========================================"

DELETE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE $BASE_URL/animals/$ANIMAL_ID)

echo "Delete HTTP Status: $DELETE_STATUS"

echo "========================================"
echo "6. GET AFTER DELETE (SHOULD FAIL)"
echo "========================================"

curl -s $BASE_URL/animals/$ANIMAL_ID | jq