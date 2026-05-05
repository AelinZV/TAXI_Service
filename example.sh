#!/bin/bash
# Полный тестовый сценарий

echo " Регистрация..."
PASSENGER=$(curl -s -X POST http://localhost:8081/auth/register/passenger \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@demo.com","phone":"+111","password":"pass"}' | jq)
echo "$PASSENGER"
PASSENGER_ID=$(echo "$PASSENGER" | jq -r '.id')

DRIVER=$(curl -s -X POST http://localhost:8081/auth/register/driver \
  -H "Content-Type: application/json" \
  -d '{"name":"Driver","email":"driver@demo.com","phone":"+222","password":"pass","licenseNumber":"DEMO-001"}' | jq)
echo "$DRIVER"
DRIVER_ID=$(echo "$DRIVER" | jq -r '.id')

echo -e "\n🚕 Создание поездки..."
TRIP=$(curl -s -X POST http://localhost:8082/trips \
  -H "Content-Type: application/json" \
  -d "{\"passengerId\":$PASSENGER_ID,\"origin\":\"Home\",\"destination\":\"Work\",\"distanceKm\":5.0}" | jq)
echo "$TRIP"
TRIP_ID=$(echo "$TRIP" | jq -r '.id')

echo -e "\n Статистика:"
curl -s http://localhost:8082/trips/stats | jq

echo -e "\n Завершение поездки..."
curl -s -X PATCH "http://localhost:8082/trips/$TRIP_ID/status?status=COMPLETED" | jq

echo -e "\n⭐ Оценка..."
curl -s -X PATCH "http://localhost:8082/trips/$TRIP_ID/rating?rating=5" | jq

echo -e "\n🔔 Уведомления:"
curl -s "http://localhost:8083/notifications?trip_id=$TRIP_ID" | jq

echo -e "\n Тест завершён!"