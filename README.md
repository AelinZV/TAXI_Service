# 1. Остановить всё и удалить орфанные контейнеры
docker compose down --remove-orphans

# 2. Удалить том с БД
docker volume rm taxi-microservices_pgdata

# 3. Исправить healthcheck в docker-compose.yml:
#    Замените:
#      test: ["CMD-SHELL", "pg_isready -U taxi"]
#    На:
#      test: ["CMD-SHELL", "pg_isready -U taxi -d taxi_db"]

# 4. Очистить кэш Docker
docker builder prune -af

# 5. Пересобрать ВСЕ сервисы
docker compose build --no-cache

# 6. Запустить
docker compose up