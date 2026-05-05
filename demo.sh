#!/bin/bash
# 🚖 Taxi Microservices Demo Script
# Запуск: chmod +x demo.sh && ./demo.sh

set -euo pipefail  # Строгий режим: ошибка = остановка, unset variable = ошибка

CLEAN_DB=false

# Базовые URL сервисов
API_USER="http://localhost:8081"
API_TRIP="http://localhost:8082"
API_NOTIF="http://localhost:8083"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; RED='\033[0;31m'; NC='\033[0m'
log_info()  { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_success(){ echo -e "${GREEN}✅ $1${NC}"; }
log_warn()  { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}" >&2; }

fmt_json() { jq --color-output . 2>/dev/null || python3 -m json.tool 2>/dev/null || cat; }

# Проверка ответа API на ошибки
check_response() {
  local resp="$1"
  local step="$2"
  if echo "$resp" | grep -q '"error"'; then
    log_error "Шаг '$step' завершился с ошибкой:"
    echo "$resp" | fmt_json
    exit 1
  fi
}

# Извлечение ID из JSON-ответа
get_id() { echo "$1" | jq -r '.id // empty'; }

if [ "$CLEAN_DB" = true ]; then
  log_warn " Очистка базы данных..."
  docker exec taxi-db psql -U taxi -d taxi_db -c "
    TRUNCATE TABLE notification_tasks, trips, drivers, passengers RESTART IDENTITY CASCADE;
  " > /dev/null 2>&1
  log_success "База данных очищена."
fi

TS=$(date +%s)
P_EMAIL="pass_${TS}@demo.com"
P_NAME="Alice_${TS}"
P_PHONE="+1${TS: -9}"

D_EMAIL="driver_${TS}@demo.com"
D_NAME="Bob_${TS}"
D_PHONE="+9${TS: -9}"
D_LIC="LIC-${TS}"

log_info " Будут созданы уникальные пользователи (timestamp: $TS)"


echo -e "\n${BLUE}🚖 Taxi Microservices Demo${NC}"
echo "================================"

echo -e "\n${YELLOW} 1  Регистрация пассажира...${NC}"
P_RESP=$(curl -s -X POST "${API_USER}/auth/register/passenger" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"${P_NAME}\",\"email\":\"${P_EMAIL}\",\"phone\":\"${P_PHONE}\",\"password\":\"demo123\"}")
check_response "$P_RESP" "Регистрация пассажира"
P_ID=$(get_id "$P_RESP")
echo "$P_RESP" | fmt_json
log_success "Пассажир создан (ID: $P_ID)"

echo -e "\n${YELLOW} 2  Регистрация водителя...${NC}"
D_RESP=$(curl -s -X POST "${API_USER}/auth/register/driver" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"${D_NAME}\",\"email\":\"${D_EMAIL}\",\"phone\":\"${D_PHONE}\",\"password\":\"demo123\",\"licenseNumber\":\"${D_LIC}\"}")
check_response "$D_RESP" "Регистрация водителя"
D_ID=$(get_id "$D_RESP")
echo "$D_RESP" | fmt_json
log_success "Водитель создан (ID: $D_ID)"

echo -e "\n${YELLOW} 3  Создание поездки...${NC}"
T_RESP=$(curl -s -X POST "${API_TRIP}/trips" \
  -H "Content-Type: application/json" \
  -d "{\"passengerId\":${P_ID},\"origin\":\"Airport\",\"destination\":\"Hotel Central\",\"distanceKm\":12.5}")
check_response "$T_RESP" "Создание поездки"
T_ID=$(get_id "$T_RESP")
echo "$T_RESP" | fmt_json
log_success "Поездка создана (ID: $T_ID)"

echo -e "\n${YELLOW} 4  Статистика за сегодня...${NC}"
curl -s "${API_TRIP}/trips/stats" | fmt_json

echo -e "\n${YELLOW} 5  Завершение поездки #${T_ID}...${NC}"
curl -s -X PATCH "${API_TRIP}/trips/${T_ID}/status?status=COMPLETED" | fmt_json
log_success "Поездка завершена"

echo -e "\n${YELLOW} 6 Оценка поездки (5 звёзд)...${NC}"
curl -s -X PATCH "${API_TRIP}/trips/${T_ID}/rating?rating=5" | fmt_json
log_success "Поездка оценена"

echo -e "\n${YELLOW} 7️  Проверка задач уведомлений...${NC}"
curl -s "${API_NOTIF}/notifications?trip_id=${T_ID}" | fmt_json

# Финал
echo -e "\n${GREEN} Демонстрация успешно завершена!${NC}"
echo -e "${BLUE} Создано: Пассажир #${P_ID} | Водитель #${D_ID} | Поездка #${T_ID}${NC}"