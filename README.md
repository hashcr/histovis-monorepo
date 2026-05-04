# HistoVis Monorepo

> Java backend services for HistoVis — a medical image analysis platform for whole slide image (WSI) processing.
> Built as part of an academic thesis project.

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)
![Status: Mostly Complete](https://img.shields.io/badge/status-mostly%20complete-green)

---

## Overview

HistoVis Monorepo contains the core backend services of the HistoVis platform. It handles image storage and metadata, plugin management, job lifecycle, and message publishing to the AI worker layer via RabbitMQ.

**Key responsibilities:**
- REST API for the frontend (image management, plugin registry, job submission)
- Plugin system — admins register plugins that define AI job types, routing keys, and arguments
- Job lifecycle management — jobs are UUID-keyed, saved as `PENDING`, then published to RabbitMQ
- Result ingestion — consumes completed/failed job results from the AI workers
- API Gateway routing to internal services

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Messaging | RabbitMQ (topic exchange) |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| API | REST (Spring MVC) |
| Infrastructure | Docker Compose |

---

## Architecture

```
Frontend (histovis-app)
        │
        ▼
   API Gateway (:8080)
        │
        ▼
 analysis-service
   ├── Plugin registry (PostgreSQL)
   ├── Job management (PostgreSQL)
   ├── RabbitMQ publisher → analysis.exchange
   │       routing key: job.<model>.<task>.<target>
   └── Result consumer ← job.results.completed / job.results.failed
```

### RabbitMQ Message Shape (JobMessage)

```json
{
  "jobId": "uuid",
  "imageUrl": "string",
  "args": { "key": "value" }
}
```

> Note: `pluginCode` is NOT in the message — it is encoded in the routing key suffix.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose
- RabbitMQ and PostgreSQL (provided via Docker Compose)

---

## Getting Started

```bash
# Clone the repo
git clone https://github.com/hashcr/histovis-monorepo.git
cd histovis-monorepo

# Start infrastructure (PostgreSQL + RabbitMQ)
docker compose up -d postgres rabbitmq

# Run the analysis-service
./mvnw spring-boot:run -pl analysis-service

# Or build and run all via Docker
docker compose up --build
```

---

## Environment Configuration

Configure via `application.properties` or environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/histovis
spring.datasource.username=histovis
spring.datasource.password=changeme

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

> ⚠️ Never commit real credentials. Use `.env` files or secrets management.

---

## Docker Network

This service connects to the shared external Docker network used across all HistoVis repos:

```bash
docker network create histovis-network
```

All services in `histovis-monorepo`, `histovis-app`, and `histovis-ai` must be on this network.

---

## Related Repositories

| Repo | Description |
|---|---|
| [histovis-app](https://github.com/hashcr/histovis-app) | Ionic/Angular frontend |
| [histovis-ai](https://github.com/hashcr/histovis-ai) | Python AI workers (StarDist, LLM consumers) |

---

## License

This project is licensed under **CC BY-NC 4.0**.
See the [LICENSE](../LICENSE) file for details.

Commercial use requires written permission from the author.

---

## Author

**Ashuin Sharma**
📧 ashuin.sharma@gmail.com
