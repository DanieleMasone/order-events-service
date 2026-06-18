# AGENTS.md

## Project Overview

`order-events-service` is a portfolio-grade Spring Boot microservice that creates orders, persists them in PostgreSQL, publishes `OrderCreatedEvent` records to Kafka, and consumes those records idempotently.

Keep the project focused. It is meant to demonstrate event-driven reliability patterns without adding extra platforms, services, or orchestration layers.

## Architecture Summary

Runtime flow:

1. `POST /api/orders` validates the request.
2. `OrderService` persists the order.
3. `KafkaOrderProducer` publishes an `OrderCreatedEvent` to `order.created.v1`.
4. `KafkaOrderConsumer` consumes the event.
5. `processed_events` records processed event IDs so duplicate deliveries become successful no-ops.
6. Spring Kafka retries transient failures and publishes exhausted records to `order.created.v1.dlt`.

The current design intentionally does not implement a transactional outbox. Add one only if database-to-Kafka atomicity becomes the explicit focus.

## Tech Stack

- Java 21
- Maven
- Spring Boot Web, Validation, Actuator
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Flyway
- MapStruct
- springdoc-openapi
- JUnit 5, Mockito, Spring Boot Test, Testcontainers
- JaCoCo
- Docker and Docker Compose
- GitHub Actions and GitHub Pages

## Build And Test Commands

Use Maven only:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

Useful checks:

```bash
docker compose config
docker build -t order-events-service:local .
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd clean verify
```

## Docker Commands

Run only infrastructure for local IDE development:

```bash
docker compose up -d postgres kafka
```

Run the full stack:

```bash
docker compose up --build
```

Do not commit Docker volumes, local environment files, logs, or temporary runtime output.

## Generated Documentation

All generated files must stay under `target`.

Expected Maven outputs after `./mvnw clean verify`:

- JaCoCo report: `target/site/jacoco/index.html`
- OpenAPI export: `target/generated-docs/openapi/openapi.json`
- Static OpenAPI documentation: `target/generated-docs/openapi/index.html`
- GitHub Pages artifact: `target/pages`
- Published landing page copy: `target/pages/index.html`
- Published user guide copy: `target/pages/docs/user-guide.md`
- Published coverage copy: `target/pages/jacoco/index.html`
- Published OpenAPI documentation: `target/pages/openapi/index.html`
- Published OpenAPI copy: `target/pages/openapi/openapi.json`

The only committed HTML source file should be `src/site/index.html`.
Do not handwrite OpenAPI HTML pages; generate them from the OpenAPI specification with Maven.
Keep README as the project entry point and `docs/user-guide.md` as the only operational documentation file. Do not add more markdown docs unless there is a clear, documented reason.

## CI/CD And GitHub Pages

The workflow must stay explicit and small: run `./mvnw clean verify`, verify generated documentation files, validate Docker Compose, build the Docker image, and upload the Pages artifact only from `main`.

GitHub Pages must use the official GitHub Pages actions:

- `actions/configure-pages`
- `actions/upload-pages-artifact`
- `actions/deploy-pages`

Do not use third-party Pages deployment actions unless there is a clear, documented reason. The repository Pages source must be set to **GitHub Actions** in GitHub settings. Pull requests must build and validate the Pages artifact locally under `target/pages`, but must not upload or deploy it.

## Testing Strategy

Prefer behavior-focused tests over broad brittle coverage. The suite should cover:

- order creation service behavior
- REST validation
- mapper behavior
- Kafka producer interaction
- consumer idempotency and duplicate handling
- retry and DLT configuration
- repository persistence with Testcontainers when Docker is available
- application context startup
- OpenAPI export and static documentation generation
- landing page documentation links

Do not add Arquillian unless there is a clear Java EE/Jakarta EE container-managed testing requirement. This is a Spring Boot service, so Spring Boot Test, MockMvc, Mockito, and Testcontainers are the appropriate default tools.

## Code Style Rules

- Keep package structure aligned with API, application, domain, infrastructure, and config boundaries.
- Put business decisions in services or infrastructure classes, not in MapStruct mappers.
- Use MapStruct only for DTO, domain, and entity transformations.
- Prefer expressive names and small methods over explanatory comments.
- Do not introduce new frameworks unless they directly improve correctness or maintainability.
- Keep changes scoped to the behavior being improved.

## JavaDoc And Comments

Public classes should have concise, meaningful JavaDoc explaining their responsibility and architectural role.

Add public method JavaDoc only when the method has non-trivial behavior, transactional relevance, idempotency semantics, retry semantics, side effects, or important trade-offs.

Avoid boilerplate comments such as getter/setter descriptions or restating obvious code.

## Generated Files Policy

Do not commit:

- `target/`
- generated MapStruct implementations
- JaCoCo reports
- OpenAPI exported JSON
- GitHub Pages publishable artifacts
- IDE metadata
- local Docker volumes
- logs or `.env` files

Keep generated sources under `target/generated-sources/annotations`.

## OpenAPI And Swagger

Runtime Swagger UI is available when the app is running:

```text
http://localhost:8080/swagger-ui.html
```

Runtime OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

The build exports OpenAPI JSON through a Spring MockMvc test to:

```text
target/generated-docs/openapi/openapi.json
```

Maven generates static OpenAPI documentation from that JSON to:

```text
target/generated-docs/openapi/index.html
```

Maven copies both files to:

```text
target/pages/openapi/index.html
target/pages/openapi/openapi.json
```

The landing page must link to `./openapi/` for API documentation and `./openapi/openapi.json` for the raw spec. Do not commit generated OpenAPI output, and do not add Node or frontend build tooling for docs unless there is a clear technical reason.

## Kafka, Idempotency, Retry

The service assumes Kafka at-least-once delivery. Consumer idempotency is enforced through the `processed_events` table.

Duplicates must be treated as successful no-ops so the listener can acknowledge the record. Transient processing failures should propagate to Spring Kafka so retry and DLT handling remain centralized in `KafkaConfig`.

Do not commit offsets before successful processing. Listener acknowledgment mode should continue to reflect that requirement.

## Rules For Future Agents

- Read the existing code before changing architecture.
- Keep Maven as the only build tool.
- Keep generated output under `target`.
- Update README and the landing page when documentation links or generated artifact paths change.
- Update `docs/user-guide.md` when operational commands, runtime endpoints, or generated documentation paths change.
- Validate with `./mvnw clean verify`, `docker compose config`, and Docker image build when relevant.
- Check `git status --short` before finishing.

## Do Not Add Without Clear Justification

- Arquillian
- Kubernetes
- extra microservices
- saga orchestration
- schema registry
- frontend frameworks
- generated artifacts outside `target`
- committed build outputs or IDE files
