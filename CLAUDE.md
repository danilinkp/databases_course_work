# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Курсовая работа по БД: REST API системы онлайн-заказов еды на Spring Boot 4 / Java 21. Репозиторий содержит бэкенд (`food-delivery-api/`), LaTeX-документацию (`docs/`) и скрипты для исследовательского раздела (`research/`).

## API Commands

```bash
# Из директории food-delivery-api/
./gradlew bootRun
./gradlew build
./gradlew clean build
./gradlew test
./gradlew test --tests "com.example.deliveryservice.<ClassName>"

# Рабочая БД + Redis (postgres:5433, redis:6379)
docker compose up -d
docker compose down

# Swagger UI после запуска
# http://localhost:8080/swagger-ui.html
```

## Research Commands

```bash
# Из директории research/
pip install -r requirements.txt

# Тестовые контейнеры (postgres:5434, redis:6380) — отдельные от рабочих
docker compose -f docker-compose.test.yml up -d

python benchmark_indexes.py   # ~15–30 мин
python benchmark_cache.py     # ~5 мин

# Результаты сохраняются в ../data/
```

## Architecture

### Controller pattern
Все контроллеры реализованы через два слоя: интерфейс (аннотации `@RequestMapping`, `@Tag`) и реализация в `controllers/impl/`. Интерфейс описывает контракт, реализация инжектирует сервис.

### Security
`UserDetailsServiceImpl` ищет пользователя последовательно в трёх таблицах: `customers` → `couriers` → `admins`. JWT-токен содержит email, аутентификация stateless. Публичные эндпоинты перечислены в `SecurityConfig.PUBLIC_ENDPOINTS`.

Роли: `ROLE_CUSTOMER`, `ROLE_COURIER`, `ROLE_SYSTEM_ADMIN`, `ROLE_RESTAURANT_ADMIN`. Доступ к эндпоинтам контролируется через `@PreAuthorize` на методах контроллера.

### Caching
`@Cacheable` используется только в `RestaurantService` и `DishService`. Имена кэшей и TTL заданы в `RedisCacheConfig`:
- `restaurants` — 60 мин
- `dishes` — 30 мин
- `deliveryZones` — 120 мин
- `couriers` — 15 мин

`@CacheEvict(allEntries = true)` вызывается на всех мутирующих методах соответствующего сервиса.

### Database
Миграции Flyway в `src/main/resources/db/migrations/` (V1–V7). Порядок: типы → таблицы → ограничения → индексы → функция → триггеры → роли.

Ключевые нюансы схемы:
- `orders` денормализован: хранит `customer_name`, `restaurant_name` snapshot-ами
- Триггеры `trg_update_restaurant_rating` и `trg_update_courier_rating` на `reviews` автоматически пересчитывают рейтинги
- Функция `calculate_order_total()` вычисляет стоимость с учётом зон доставки, пиковых надбавок и скидки первого заказа

### Research scripts
`research/seed_data.py` — общий модуль с утилитами генерации данных, импортируется обоими бенчмарками. Тестовая БД полностью изолирована от рабочей. Схема разворачивается из тех же migration-файлов через `setup_schema()`.
