# FitTrack — Tracking Service

Spring Boot microservice that records user **activities** (workouts) and **nutrition** entries for the FitTrack platform. Persists data in MongoDB, validates users against `user-service` via Eureka-discovered `WebClient`, and publishes change events to RabbitMQ for downstream consumers (e.g. an AI calorie/insights service).

## Tech stack

- **Java 21** · **Spring Boot 4.0.2** · **Maven**
- **Spring Data MongoDB** (sync driver)
- **Spring AMQP** (RabbitMQ producer)
- **Spring Cloud Netflix Eureka** client (service discovery)
- **Spring WebFlux `WebClient`** (load-balanced calls to `user-service`)
- **AWS SDK v2 — SSM Parameter Store** (runtime config / secrets)
- Lombok, AOP-based request logging

## Architecture

```
                ┌───────────────┐
                │ user-service  │  ← Eureka-registered, validated via WebClient
                └──────┬────────┘
                       │ /api/users/{id}/validate
                       │
   client ──► tracking-service ──► MongoDB  (activities, nutrition)
                       │
                       ▼
                   RabbitMQ        (fitness_exchange)
                       │
                       ├── activity_routing_key  → activity_queue
                       └── nutrition_routing_key → nutrition_queue
```

Configuration values that are sensitive or environment-specific (Mongo URI, database name, RabbitMQ addresses) are stored as **SSM Parameter Store** keys; `application.yaml` only holds the *parameter names*, and `ParameterStoreService` resolves them at startup using `DefaultCredentialsProvider`.

## API

Base path: `/api`

### Activities — `/api/activities`

| Method | Path | Description |
|---|---|---|
| `GET`    | `/`                              | List all supported activity types |
| `GET`    | `/user/{userId}`                 | List activities for a user |
| `GET`    | `/{activityId}`                  | Get one activity |
| `POST`   | `/user/{userId}/track`           | Track a new activity |
| `PUT`    | `/{activityId}/user/{userId}`    | Update an activity |
| `DELETE` | `/{activityId}/user/{userId}`    | Delete an activity |

**Activity types:** `RUNNING`, `CYCLING`, `SWIMMING`, `YOGA`, `WEIGHTLIFTING`, `HIKING`, `DANCING`, `PILATES`, `CROSSFIT`.

### Nutrition — `/api/nutrition`

| Method | Path | Description |
|---|---|---|
| `GET`    | `/`                               | List all supported nutrition / meal types |
| `GET`    | `/user/{userId}`                  | List nutrition entries for a user |
| `GET`    | `/{nutritionId}`                  | Get one nutrition entry |
| `POST`   | `/user/{userId}/log`              | Log a new nutrition entry |
| `PUT`    | `/{nutritionId}/user/{userId}`    | Update a nutrition entry |
| `DELETE` | `/{nutritionId}/user/{userId}`   | Delete a nutrition entry |

**Meal types:** `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`, `SUPPLEMENT`, `PRE_WORKOUT`, `POST_WORKOUT`.

All responses are wrapped in a `GlobalResponseDTO<T>` envelope (`{ data, message, ... }`); errors flow through `GlobalExceptionHandler` and return an `ErrorDTO`.

### Sample request

```bash
curl -X POST http://localhost:8072/api/activities/user/42/track \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt>" \
  -d '{
    "userId": 42,
    "type": "RUNNING",
    "duration": 30,
    "startTime": "2026-05-15T07:30:00",
    "metadata": "5 km, easy pace"
  }'
```

The `Authorization` header is propagated to `user-service` by `WebClientConfig`.

## Configuration

`src/main/resources/application.yaml` holds the SSM parameter *paths*, not values:

```yaml
mongodb:
  uri: /Fittrack/ai-srvc/mongodb/uri
  database: /Fittrack/ai-srvc/mongodb/database

rabbitmq:
  addresses: /Fittrack/rabbitmq/addresses

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

server:
  port: 8072
```

Required at runtime:

| What | How it's resolved |
|---|---|
| AWS region | `aws.ssm.region` property (env var `AWS_SSM_REGION` or JVM arg) |
| AWS credentials | `DefaultCredentialsProvider` chain (env, profile, instance role) |
| Mongo URI / database | SSM parameters listed above |
| RabbitMQ addresses | SSM parameter listed above |
| Eureka registry | `http://localhost:8761/eureka/` by default |

## Running locally

Prerequisites: **JDK 21**, a running **MongoDB**, **RabbitMQ**, **Eureka** (`fitness-eureka`), **user-service**, and AWS credentials with access to the SSM parameters above.

```bash
# from the repo root
./mvnw clean spring-boot:run

# or build + run jar
./mvnw clean package
java -jar target/activity-service-0.0.1-SNAPSHOT.jar
```

Service starts on **port 8072** and registers itself with Eureka.

### Run tests

```bash
./mvnw test
```

## Project layout

```
src/main/java/com/fitness/activity_service
├── ActivityServiceApplication.java
├── aspect/             # request/response logging (AOP)
├── config/             # Mongo, AWS SSM, RabbitMQ, WebClient beans
├── controllers/        # ActivityController, NutritionController, PingController
├── dto/                # request/response DTOs + GlobalResponseDTO/ErrorDTO
├── exceptions/         # RecordNotFoundException + GlobalExceptionHandler
├── model/              # Activity, Nutrition + enums
├── repository/         # Spring Data MongoDB repositories
└── service/            # business logic + UserValidationService + ParameterStoreService
```

## Related services

- **fittrack-eureka** — service discovery
- **fittrack-user-service** — user CRUD + validation endpoint
- **fittrack-host-ui** + content MFEs — frontend
