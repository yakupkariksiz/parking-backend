# Parking Backend

Backend service for managing resident parking access, recording license-plate scans, monitoring outsider vehicles, and surfacing audit/stats data for a small HOA-style deployment. The service exposes REST APIs and serves a lightweight static UI under `src/main/resources/static` for common workflows.

## Features
- Resident and vehicle registry with plate-to-resident relationships
- Scan session + scan entry ingestion for patrol devices
- Outsider plate tracking and aggregate session statistics
- Audit logging of API activity, including security-filtered requests
- Static web UI (index/login/residents/stats) bundled for quick ops access

## Tech Stack
- Java 21 with Spring Boot 3.3 (web, security, data-JPA starters)
- PostgreSQL via Spring Data JPA/Hibernate
- Docker Compose for local Postgres + app orchestration
- Lombok for DTO/entity boilerplate, Jakarta annotations

## Getting Started
1. Ensure Java 21+, Maven Wrapper permissions (`mvnw`) and Docker are available.
2. Provision dependencies locally:
   ```bash
   ./mvnw clean package
   ```
3. Start the stack (PostgreSQL + app) with Docker Compose:
   ```bash
   docker compose up --build
   ```
4. Access the APIs or static UI at `http://localhost:8080` (configurable via `PORT`).

Configuration defaults live in `src/main/resources/application.properties`; override the `SPRING_DATASOURCE_*` environment variables or `PORT` as needed for your environment.

## Project Layout Highlights
- `src/main/java/com/example/parking` — Spring Boot entry point, controllers, services, repositories, and config (security, audit filter, data initializers)
- `src/main/resources/static` — bundled HTML screens for operators
- `docker-compose.yml` — local Postgres service plus app image

## Next Steps
- Seed initial users/locations via the `UserInitializer` and `LocationDefinitionInitializer`
- Extend controllers or DTOs for new parking rules as requirements evolve
- Add integration tests around scan ingestion and stats endpoints once flows stabilize

