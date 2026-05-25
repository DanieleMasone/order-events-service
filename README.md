# Order Events Service

Production-oriented event-driven Spring Boot microservice for creating orders, publishing Kafka events, consuming those events idempotently, and documenting the build with OpenAPI, JaCoCo, Docker, and GitHub Pages.

## What It Demonstrates

- Java 21 and Maven-only build
- Spring Boot REST API with request validation
- PostgreSQL persistence through Spring Data JPA
- Flyway-managed database schema
- Kafka producer and consumer with JSON events
- Idempotent consumer processing through a `processed_events` table
- Retry with fixed backoff and dead-letter routing to `order.created.v1.dlt`
- Swagger UI and generated OpenAPI JSON
- Docker Compose local environment
- JUnit 5 tests, Mockito, Spring Boot Test, Testcontainers for PostgreSQL
- JaCoCo coverage report under `target/site/jacoco`
- GitHub Pages artifact assembled under `target/pages`

## Architecture

The service keeps one focused workflow:

```text
Client
  -> REST API
  -> Order Service
  -> PostgreSQL
  -> Kafka Producer
  -> order.created.v1
  -> Kafka Consumer
  -> processed_events
  -> order.created.v1.dlt
```

When a client creates an order, the application persists the order and publishes an `OrderCreatedEvent`. The consumer stores the event ID in `processed_events` after successful processing. If the same Kafka event is delivered again, the consumer sees the existing event ID and exits successfully, allowing the offset to be committed without duplicating side effects.

## Runtime Endpoints

- REST API: `POST http://localhost:8080/api/orders`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON while running locally: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`

Example request:

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "customer-001",
  "amount": 199.90
}
```

## Local Development

Start PostgreSQL and Kafka in Docker, then run the Spring Boot app from the IDE or Maven:

```bash
docker compose up -d postgres kafka
./mvnw spring-boot:run
```

Run the complete local environment:

```bash
docker compose up --build
```

Useful validation commands:

```bash
./mvnw clean verify
docker compose config
docker build -t order-events-service:local .
```

## Build Outputs

Generated files are intentionally produced under `target` and should not be committed.

- MapStruct implementations: `target/generated-sources/annotations`
- JaCoCo coverage report: `target/site/jacoco/index.html`
- OpenAPI export: `target/generated-docs/openapi.json`
- GitHub Pages artifact: `target/pages`

The committed landing page source is the only HTML source file:

```text
src/site/index.html
```

During `./mvnw clean verify`, Maven copies the landing page and generated reports into `target/pages`.

## GitHub Pages

The CI workflow publishes GitHub Pages from `main` using the official Pages actions. Replace the placeholder repository URL after creating the GitHub repository:

- Pages placeholder: `https://your-username.github.io/order-events-service/`
- JaCoCo placeholder: `https://your-username.github.io/order-events-service/jacoco/`
- OpenAPI placeholder: `https://your-username.github.io/order-events-service/openapi/openapi.json`

The Pages landing page also links to local Swagger UI and actuator health. Those links work only while the application is running locally.

## Testing

Run all tests and reports:

```bash
./mvnw clean verify
```

The test suite covers:

- Order creation service behavior
- REST controller validation
- Kafka producer interaction
- Kafka consumer idempotency and duplicate handling
- Retry and DLT configuration wiring
- MapStruct mapping behavior
- PostgreSQL repository behavior with Testcontainers
- Spring application context startup
- OpenAPI JSON export under `target/generated-docs`

## CI Pipeline

`.github/workflows/ci.yml` runs on pushes to `main` and pull requests. It:

1. Checks out the repository.
2. Sets up Java 21.
3. Runs `./mvnw -B clean verify`.
4. Builds the Docker image.
5. Uploads test reports.
6. Publishes `target/pages` to GitHub Pages from `main`.

## Design Trade-offs

This project intentionally does not include Kubernetes, multiple services, saga orchestration, or schema registry. The goal is to keep the system small enough to review while still demonstrating important event-driven reliability patterns.

The order creation flow publishes to Kafka after flushing the order row. This keeps the portfolio project simple and observable. In a system requiring stronger database-to-Kafka atomicity, the next step would be a transactional outbox and relay.

The consumer uses an idempotency table because Kafka provides at-least-once delivery. The listener lets persistence exceptions propagate so Spring Kafka retry and DLT handling can decide whether to retry or recover the record.

## Future Improvements

- Transactional outbox pattern
- Schema Registry with Avro or Protobuf
- Consumer replay tooling
- OpenTelemetry tracing
- Prometheus and Grafana dashboards
- Explicit Kafka topic provisioning for non-local environments
