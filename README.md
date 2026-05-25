# Order Events Service

Event-driven microservice built with Spring Boot, Kafka, PostgreSQL and Docker Compose.

The goal of this project is to demonstrate a production-oriented implementation of an event-driven service, including:

- Kafka producer and consumer
- idempotent event processing
- retry handling
- dead letter topic
- PostgreSQL persistence
- Flyway database migrations
- Swagger UI
- Docker Compose local environment
- GitHub Actions CI

---

## Architecture

The service exposes a REST API to create orders.

When an order is created:

1. The API receives a `CreateOrderRequest`
2. The order is persisted in PostgreSQL
3. An `OrderCreatedEvent` is published to Kafka
4. A Kafka consumer processes the event
5. The consumer stores the processed `eventId` to guarantee idempotency
6. Failed events are retried and eventually sent to a Dead Letter Topic

```text
Client
  ↓
REST API
  ↓
Order Service
  ↓
PostgreSQL
  ↓
Kafka Producer
  ↓
order.created.v1
  ↓
Kafka Consumer
  ↓
Idempotency Check
  ↓
Business Processing
  ↓
processed_events
```

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Flyway
- Docker Compose
- Swagger UI / OpenAPI
- GitHub Actions

---

## Event Design

### Main Topic

```text
order.created.v1
```

### Dead Letter Topic

```text
order.created.v1.dlt
```

### Example Event

```json
{
  "eventId": "0e1c8700-9b2a-4e4c-a9f4-73b8f8a2a111",
  "eventType": "OrderCreated",
  "occurredAt": "2026-05-20T10:15:30Z",
  "orderId": "0a2ff913-41cf-4aa2-8b01-79f7b0cc4d44",
  "customerId": "customer-001",
  "amount": 199.90
}
```

---

## Idempotency Strategy

Kafka consumers can receive the same message more than once.

To avoid duplicated side effects, every event contains a unique `eventId`.

The consumer follows this flow:

1. Check whether `eventId` already exists in `processed_events`
2. If it exists, skip processing and acknowledge the message
3. If it does not exist, process the event inside a transaction
4. Store the `eventId` in `processed_events`
5. Commit the Kafka offset only after successful processing

This provides at-least-once delivery with idempotent processing.

---

## Retry and Dead Letter Topic

Transient failures are retried with backoff.

After the maximum number of retry attempts, the event is sent to:

```text
order.created.v1.dlt
```

This avoids message loss and allows failed events to be inspected or replayed manually.

---

## Local Development

### Start infrastructure

```bash
docker compose up -d postgres kafka
```

### Run application locally

Run the Spring Boot application from IntelliJ IDEA.

### Run everything with Docker Compose

```bash
docker compose up --build
```

---

## Swagger UI

After starting the application:

```text
http://localhost:8080/swagger-ui.html
```

---

## Health Check

```text
http://localhost:8080/actuator/health
```

---

## Example Request

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "customer-001",
  "amount": 199.90
}
```

---

## Docker Compose

Example services:

- PostgreSQL
- Kafka
- Spring Boot application

The entire local environment is reproducible with a single command.

---

## CI Pipeline

GitHub Actions executes:

- Maven build
- unit tests
- Docker image build

Workflow location:

```text
.github/workflows/ci.yml
```

---

## Design Trade-offs

This project intentionally keeps the domain small.

The goal is not to create a distributed system for its own sake, but to demonstrate the hard parts of event-driven architecture:

- duplicate message handling
- retry behavior
- dead letter routing
- transactional boundaries
- local reproducibility
- API documentation
- CI automation

Kafka is used because it better represents event streaming and event-log based architecture than a simple queue-based demo.

---

## What This Project Is Not

This is not a full microservice ecosystem.

It does not include:

- service discovery
- Kubernetes deployment
- distributed tracing
- schema registry
- multi-service orchestration
- saga orchestration

Those additions would make sense only if the project evolved into multiple services.

---

## Future Improvements

- Transactional outbox pattern
- Schema Registry with Avro or Protobuf
- Testcontainers integration tests
- Prometheus and Grafana
- OpenTelemetry tracing
- Kafka topic provisioning script
- Consumer replay tooling

---

## Repository Goals

This repository is intended to demonstrate:

- clean project structure
- production-oriented engineering practices
- event-driven architecture fundamentals
- Docker-based local development
- resilient Kafka consumer design
- maintainable Spring Boot architecture

The focus is not on complexity, but on correctness and engineering discipline.
