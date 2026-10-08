# AgroClimate Monitoring (Spring Boot, Java 17)

Бэкенд веб-приложения для мониторинга микроклимата в агрокомплексе.

## Стек

- Java 17, Spring Boot 3.3.x
- Spring Web, Spring Data JPA (Hibernate)
- Spring Security + JWT (jjwt)
- Bean Validation (Jakarta)
- DTO + ручной маппер (`EntityMapper`)
- PostgreSQL
- springdoc-openapi (Swagger UI)
- Lombok

## Подготовка БД (PostgreSQL без Docker)

Предполагается, что у вас локально работает PostgreSQL и есть пользователь
`postgres` с паролем `qwerty` (как в `application.yml`).

1. Создайте базу:

    ```sql
    CREATE DATABASE agro_db;
    ```

2. Подключитесь к `agro_db` и выполните скрипт
   `scripts/001_schema.sql` (через pgAdmin / DBeaver / psql):

    ```bash
    psql -U postgres -d agro_db -f scripts/001_schema.sql
    ```

    Скрипт пересоздаёт все таблицы с `GENERATED ALWAYS AS IDENTITY`,
    что нужно для корректной работы JPA-стратегии `IDENTITY`.

3. Запустите приложение:

    ```bash
    mvn spring-boot:run
    ```

При первом запуске `DataInitializer` сам наполнит БД ролями, тестовым
администратором и демо-данными (теплица, зона, 3 датчика, 3 правила).

## Тестовый администратор

- email: `admin@agro.local`
- password: `admin123`
- роли: `ADMIN`

## Swagger UI

После запуска документация доступна по адресам:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Чтобы тестировать защищённые эндпоинты прямо в Swagger:

1. Сделайте `POST /api/auth/login` (admin@agro.local / admin123).
2. Скопируйте `token` из ответа.
3. Нажмите **Authorize** в правом верхнем углу Swagger UI и вставьте
   токен (без префикса `Bearer`).
4. Теперь все защищённые ручки доступны.

## Симуляция данных

Сервис `SensorSimulationService` каждые `app.simulation.period-ms`
(по умолчанию 5 секунд) проходит по всем активным датчикам и записывает
случайное измерение (плавно блуждающее вокруг предыдущего значения, с
редкими выбросами для срабатывания алертов). После каждого измерения
`AlertService.evaluate()` сверяет значение с активными порогами
и открывает / закрывает алерты.

Отключить симуляцию: `app.simulation.enabled=false`.

## Единицы измерения в API

Все DTO измерений / правил / алертов содержат поле `unit` рядом со значением:

- `TEMPERATURE` -> `°C`
- `HUMIDITY` -> `%`
- `LIGHT` -> `lx`

## Безопасность и роли

JWT передаётся в заголовке `Authorization: Bearer <token>`.

| URL                                                       | Роли                              |
| --------------------------------------------------------- | --------------------------------- |
| `POST /api/auth/register`, `/login`                       | Public                            |
| `/swagger-ui/**`, `/v3/api-docs/**`                       | Public                            |
| `GET /api/**`                                             | Любой аутентифицированный         |
| `POST/PUT /api/greenhouses,zones,sensors,threshold-rules` | `ADMIN`, `AGRONOMIST`             |
| `DELETE /api/...`                                         | `ADMIN`                           |
| `POST /api/alerts/{id}/acknowledge,close`                 | `ADMIN`, `AGRONOMIST`, `OPERATOR` |
| `/api/users/**`                                           | `ADMIN`                           |

## Основные эндпоинты

```
POST  /api/auth/register
POST  /api/auth/login

GET   /api/greenhouses           POST  /api/greenhouses
GET   /api/zones?greenhouseId=1  POST  /api/zones
GET   /api/sensors?zoneId=1      POST  /api/sensors
GET   /api/sensors/{id}/measurements?from=...&to=...
GET   /api/threshold-rules?zoneId=1   POST /api/threshold-rules

GET   /api/alerts?openOnly=true
POST  /api/alerts/{id}/acknowledge
POST  /api/alerts/{id}/close

GET   /api/users                 (ADMIN)
PATCH /api/users/{id}/status?status=BLOCKED
```

## Пример: логин и запрос данных (curl)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@agro.local","password":"admin123"}' | jq -r .token)

curl -s http://localhost:8080/api/sensors -H "Authorization: Bearer $TOKEN" | jq
curl -s "http://localhost:8080/api/sensors/1/measurements" -H "Authorization: Bearer $TOKEN" | jq
```

Пример ответа `/measurements`:

```json
[
	{
		"id": 42,
		"sensorId": 1,
		"metric": "TEMPERATURE",
		"value": 23.41,
		"unit": "°C",
		"ts": "2026-04-29T12:34:56",
		"source": "SIM"
	}
]
```

## Структура проекта

```
src/main/java/com/agroclimate
├── AgroClimateApplication.java
├── config/
│   ├── DataInitializer.java
│   └── OpenApiConfig.java
├── controller/        # REST контроллеры
├── domain/
│   ├── entity/        # JPA сущности
│   └── enums/         # Metric, RoleCode, Severity, ...
├── dto/               # DTO (records)
├── exception/         # глобальный обработчик ошибок
├── repository/        # Spring Data JPA репозитории
├── security/          # JWT, фильтр, конфиг безопасности
└── service/           # бизнес-логика + симуляция

scripts/
└── 001_schema.sql     # DDL для PostgreSQL (запустить один раз)
```
