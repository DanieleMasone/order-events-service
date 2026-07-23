# User Guide

## Prerequisites

- Java 21
- Docker
- Docker Compose
- Maven Wrapper from this repository

## Local Development

### Infrastructure Only

Start PostgreSQL and Kafka for IDE or Maven-based development:

```bash
docker compose up -d postgres kafka
```

### Spring Boot Application

Run the service from the repository root:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### Full Stack

Build and run PostgreSQL, Kafka, and the application:

```bash
docker compose up --build
```

## API Usage

### Create Order

Request:

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "customer-001",
  "amount": 199.90
}
```

Response:

```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "4f8b21a8-54e7-4f39-b37e-bad7c8734d95",
  "customerId": "customer-001",
  "amount": 199.90,
  "status": "CREATED",
  "createdAt": "2026-06-18T10:15:30Z"
}
```

### Runtime Endpoints

- REST endpoint: `POST http://localhost:8080/api/orders`
- OpenAPI endpoint: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8080/actuator/health`

## Event Flow

1. The REST API validates the create-order request.
2. `OrderService` persists the order in PostgreSQL.
3. `KafkaOrderProducer` publishes an `OrderCreatedEvent` to `order.created.v1`.
4. `KafkaOrderConsumer` receives the event after Kafka delivery.
5. The consumer checks `processed_events` before applying side effects.
6. A new event is recorded as processed after successful handling.
7. A duplicate event ID is treated as a successful no-op so the Kafka offset can be acknowledged.
8. Transient failures propagate to Spring Kafka retry handling.
9. Exhausted retries are routed to `order.created.v1.dlt`.

Kafka is treated as an at-least-once delivery system. The `processed_events` table is the idempotency boundary that prevents duplicate processing while still allowing redelivered records to complete successfully.

## Generated Documentation

All generated outputs stay under `target` and are not committed.

- JaCoCo report: `target/site/jacoco/index.html`
- OpenAPI JSON: `target/generated-docs/openapi/openapi.json`
- Static OpenAPI documentation: `target/generated-docs/openapi/index.html`
- Static user guide documentation: `target/generated-docs/user-guide/index.html`
- GitHub Pages artifact: `target/pages`
- Published landing page copy: `target/pages/index.html`
- Published user guide copy: `target/pages/docs/index.html`
- Published coverage copy: `target/pages/jacoco/index.html`
- Published OpenAPI documentation: `target/pages/openapi/index.html`
- Published OpenAPI JSON: `target/pages/openapi/openapi.json`

The OpenAPI JSON is exported by a Spring MockMvc test during `./mvnw clean verify`. The OpenAPI Generator Maven plugin renders static `html2` documentation from that JSON. The user guide HTML is generated from this Markdown source during the Maven test phase. Maven then copies the landing page, generated user guide, JaCoCo report, OpenAPI HTML, and OpenAPI JSON into `target/pages`.

## GitHub Pages

Published URLs:

- Landing page: `https://danielemasone.github.io/order-events-service/`
- User guide: `https://danielemasone.github.io/order-events-service/docs/`
- Coverage: `https://danielemasone.github.io/order-events-service/jacoco/`
- OpenAPI docs: `https://danielemasone.github.io/order-events-service/openapi/`
- OpenAPI JSON: `https://danielemasone.github.io/order-events-service/openapi/openapi.json`

GitHub Pages must be configured to use **GitHub Actions** as the Pages source.

## CI Validation

Run the same checks locally before pushing:

```bash
./mvnw clean verify
docker compose config
docker build -t order-events-service:local .
```

CI also verifies that the generated Pages artifact contains the landing page, user guide, JaCoCo report, OpenAPI documentation, and raw OpenAPI JSON. The workflow deploys Pages only from `main`; pull requests build and validate the artifact without deploying it.

## Troubleshooting

### Docker Not Running

`docker compose config` or Testcontainers-backed tests may fail if Docker Desktop or the Docker daemon is not running. Start Docker and rerun the command.

### Kafka Unavailable

If the application cannot reach Kafka, verify that the `kafka` service is healthy:

```bash
docker compose ps kafka
```

For local Maven runs, Kafka should be reachable at `localhost:29092`.

### PostgreSQL Unavailable

If the application cannot connect to PostgreSQL, verify that the `postgres` service is healthy:

```bash
docker compose ps postgres
```

For local Maven runs, PostgreSQL should be reachable at `localhost:5432` with the credentials from `docker-compose.yml`.

### Stale Target Folder

If generated docs or reports look stale, remove previous build output by running:

```bash
./mvnw clean verify
```

### Missing Generated OpenAPI Files

The static OpenAPI page depends on `target/generated-docs/openapi/openapi.json`. If it is missing, run the full Maven verification command instead of a partial package build:

```bash
./mvnw clean verify
```

### Pages Links Not Generated

If `target/pages` is missing published files, verify these paths after Maven finishes:

```text
target/pages/index.html
target/pages/docs/index.html
target/pages/jacoco/index.html
target/pages/openapi/index.html
target/pages/openapi/openapi.json
```
