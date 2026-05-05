# 🚖 Taxi Microservices Platform

> 🧩 Масштабируемая платформа такси на микросервисах с динамическим ценообразованием, асинхронными уведомлениями и полной контейнеризацией.

[![Build & Test](https://github.com/yourname/taxi-microservices/actions/workflows/ci.yml/badge.svg)](https://github.com/AelinZV/TAXI_Service/actions/runs/25368121080)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring_Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-✓-blue.svg)](https://www.docker.com/)

---

##  Возможности

### Бизнес-логика
| Функция | Описание | Статус |
|---------|----------|--------|
|  Регистрация пользователей | Пассажиры и водители с валидацией уникальности | ✅ |
|  Аутентификация | JWT-токены, разделение ролей (PASSENGER/DRIVER) | ✅ |
|  Создание поездки | Атомарное назначение свободного водителя | ✅ |
|  Динамическое ценообразование | Базовый тариф + surge pricing при нехватке водителей | ✅ |
|  Рейтинг поездок | Оценка 1-5 звёзд после завершения | ✅ |
|  Асинхронные уведомления | Воркер-пул с retry-логикой и graceful shutdown | ✅ |
|  Статистика в реальном времени | Количество поездок, средняя цена, доступные водители | ✅ |

### 🔧 Технические преимущества
-  **Полная контейнеризация** — один `docker compose up` поднимает всю инфраструктуру
- ️ **Идемпотентность** — демо-скрипт генерирует уникальные данные, можно запускать многократно
-  **Интеграционное тестирование** — автоматический прогон сценария в CI/CD
-  **Health checks** — мониторинг готовности сервисов через Spring Actuator
-  **Безопасность** — CSRF отключён для API, CORS настроен для разработки

---


---

##  Технологический стек

| Категория | Технология | Версия | Назначение |
|-----------|------------|--------|------------|
| **Backend** | Spring Boot | 3.2.0 | Микросервисы, REST API |
| **Язык** | Java | 17 | Основная реализация |
| **Сборка** | Maven | 3.9 | Управление зависимостями |
| **СУБД** | PostgreSQL | 15 | Реляционное хранение данных |
| **Кэш** | Redis | 7 | TTL-кэш счётчиков |
| **Контейнеры** | Docker + Compose | 24+ | Оркестрация инфраструктуры |
| **Аутентификация** | JWT (jjwt) | 0.11.5 | Stateless-сессии |
| **Мониторинг** | Spring Actuator | 3.2.0 | Health endpoints |
| **Логирование** | SLF4J + Logback | 1.4.11 | Структурированные логи |
| **CI/CD** | GitHub Actions | — | Автоматическая сборка и тесты |

---

## Быстрый старт

### ▶️ Запуск за 3 команды

```bash
# 1. Клонировать репозиторий
git clone https://github.com/yourname/taxi-microservices.git
cd taxi-microservices

# 2. Запустить всю инфраструктуру
docker compose up --build

# 3. Дождаться готовности (лог: "Started ...Application in X seconds")