#!/bin/bash

BASE_URL="http://localhost:8080"

ANIMAL_TESTS=0
PRODUCTION_TESTS=0
FEED_TESTS=0

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
((ANIMAL_TESTS++))

echo "2. GET ALL ANIMALS"
curl -s $BASE_URL/animals | jq
((ANIMAL_TESTS++))

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
((PRODUCTION_TESTS++))

echo "4. CREATE INVALID PRODUCTION (SHOULD FAIL)"
curl -s -X POST $BASE_URL/productions \
  -H "Content-Type: application/json" \
  -d "{
    \"animalId\": \"$ANIMAL_ID\",
    \"date\": \"2024-03-20\",
    \"quantity\": -5
  }" | jq
((PRODUCTION_TESTS++))

echo "5. GET ALL PRODUCTIONS"
curl -s $BASE_URL/productions | jq
((PRODUCTION_TESTS++))

echo "6. GET PRODUCTION BY ID"
curl -s $BASE_URL/productions/$PRODUCTION_ID | jq
((PRODUCTION_TESTS++))

echo "7. GET PRODUCTION WITH INVALID ID (SHOULD FAIL)"
curl -s $BASE_URL/productions/invalid-id | jq
((PRODUCTION_TESTS++))

echo "========================================"
echo "UPDATE FLOW"
echo "========================================"

echo "8. UPDATE PRODUCTION (VALID)"
UPDATE_RESPONSE=$(curl -s -X PUT $BASE_URL/productions/$PRODUCTION_ID \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 20.0
  }')

echo $UPDATE_RESPONSE | jq
((PRODUCTION_TESTS++))

echo "9. UPDATE PRODUCTION INVALID (SHOULD FAIL)"
curl -s -X PUT $BASE_URL/productions/$PRODUCTION_ID \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": -10
  }' | jq
((PRODUCTION_TESTS++))

echo "10. UPDATE NON-EXISTENT PRODUCTION (SHOULD FAIL)"
curl -s -X PUT $BASE_URL/productions/non-existent-id \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 10
  }' | jq
((PRODUCTION_TESTS++))

echo "========================================"
echo "FILTER FLOW"
echo "========================================"

echo "11. FILTER BY ANIMAL ID"
curl -s "$BASE_URL/productions?animalId=$ANIMAL_ID" | jq
((PRODUCTION_TESTS++))

echo "12. FILTER BY DATE"
curl -s "$BASE_URL/productions?date=2024-03-20" | jq
((PRODUCTION_TESTS++))

echo "13. FILTER BY ANIMAL AND DATE"
curl -s "$BASE_URL/productions?animalId=$ANIMAL_ID&date=2024-03-20" | jq
((PRODUCTION_TESTS++))

echo "14. FILTER WITH NO RESULTS (SHOULD RETURN EMPTY)"
curl -s "$BASE_URL/productions?animalId=invalid&date=2020-01-01" | jq
((PRODUCTION_TESTS++))

echo "========================================"
echo "AGGREGATION FLOW"
echo "========================================"

echo "15. GET TOTAL PRODUCTION BY ANIMAL"
curl -s "$BASE_URL/productions/summary/by-animal?animalId=$ANIMAL_ID" | jq
((PRODUCTION_TESTS++))

echo "16. GET TOTAL PRODUCTION FOR ANIMAL WITH NO DATA (SHOULD BE 0)"
curl -s "$BASE_URL/productions/summary/by-animal?animalId=non-existent-id" | jq
((PRODUCTION_TESTS++))

echo "========================================"
echo "FEED FLOW"
echo "========================================"

echo "17. CREATE FEED TYPE"
CREATE_FEED_RESPONSE=$(curl -s -X POST $BASE_URL/feed-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Silagem",
    "costPerKg": 1.5
  }')

echo $CREATE_FEED_RESPONSE | jq
FEED_TYPE_ID=$(echo $CREATE_FEED_RESPONSE | jq -r '.id')
echo "Generated FEED_TYPE_ID: $FEED_TYPE_ID"
((FEED_TESTS++))

echo "18. GET ALL FEED TYPES"
curl -s $BASE_URL/feed-types | jq
((FEED_TESTS++))

echo "19. GET FEED TYPE BY ID"
curl -s $BASE_URL/feed-types/$FEED_TYPE_ID | jq
((FEED_TESTS++))

echo "20. CREATE FEEDING"
CREATE_FEEDING_RESPONSE=$(curl -s -X POST $BASE_URL/feedings \
  -H "Content-Type: application/json" \
  -d "{
    \"animalId\": \"$ANIMAL_ID\",
    \"feedTypeId\": \"$FEED_TYPE_ID\",
    \"date\": \"2024-03-20\",
    \"quantity\": 10
  }")

echo $CREATE_FEEDING_RESPONSE | jq
FEEDING_ID=$(echo $CREATE_FEEDING_RESPONSE | jq -r '.id')
echo "Generated FEEDING_ID: $FEEDING_ID"
((FEED_TESTS++))

echo "21. CREATE INVALID FEEDING (SHOULD FAIL)"
curl -s -X POST $BASE_URL/feedings \
  -H "Content-Type: application/json" \
  -d "{
    \"animalId\": \"$ANIMAL_ID\",
    \"feedTypeId\": \"$FEED_TYPE_ID\",
    \"date\": \"2024-03-20\",
    \"quantity\": -5
  }" | jq
((FEED_TESTS++))

echo "22. GET ALL FEEDINGS"
curl -s $BASE_URL/feedings | jq
((FEED_TESTS++))

echo "23. GET FEEDING BY ID"
curl -s $BASE_URL/feedings/$FEEDING_ID | jq
((FEED_TESTS++))

echo "24. GET FEEDING INVALID (SHOULD FAIL)"
curl -s $BASE_URL/feedings/invalid-id | jq
((FEED_TESTS++))

echo "========================================"
echo "CLEANUP"
echo "========================================"

echo "25. DELETE ANIMAL"
DELETE_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE $BASE_URL/animals/$ANIMAL_ID)
echo "Delete HTTP Status: $DELETE_STATUS"
((ANIMAL_TESTS++))

echo "26. GET DELETED ANIMAL (SHOULD FAIL)"
curl -s $BASE_URL/animals/$ANIMAL_ID | jq
((ANIMAL_TESTS++))

echo "========================================"
echo "TEST SUMMARY"
echo "========================================"

TOTAL_TESTS=$((ANIMAL_TESTS + PRODUCTION_TESTS + FEED_TESTS))

echo "Animal tests: $ANIMAL_TESTS"
echo "Production tests: $PRODUCTION_TESTS"
echo "Feed tests: $FEED_TESTS"
echo "Total tests: $TOTAL_TESTS"