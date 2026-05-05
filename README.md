# PCI-DSS Compliance Tracker

A full-stack application for tracking and managing PCI-DSS compliance requirements with AI-powered recommendations.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Spring Security + JWT |
| Database | PostgreSQL 15, Flyway Migrations |
| Cache | Redis 7 |
| AI Service | Python 3.11, Flask, Groq, ChromaDB |
| Frontend | React 18, Vite, Axios |
| DevOps | Docker, Docker Compose |

## Project Structure

```
pci-dss-compliance-tracker/
│
├── backend/                          <- Spring Boot project
│   ├── src/main/java/com/internship/tool/
│   │   ├── controller/               <- REST endpoints (@RestController)
│   │   ├── service/                  <- Business logic (@Service)
│   │   ├── repository/               <- DB queries (JpaRepository)
│   │   ├── entity/                   <- JPA table models (@Entity)
│   │   ├── dto/                      <- Request and Response POJOs
│   │   ├── config/                   <- Security, Redis, Mail, Async
│   │   ├── exception/                <- Custom exceptions + @ControllerAdvice
│   │   └── scheduler/                <- @Scheduled reminder jobs
│   ├── src/main/resources/
│   │   ├── db/migration/             <- V1__init.sql, V2__audit.sql ...
│   │   ├── templates/                <- Thymeleaf HTML email templates
│   │   └── application.yml           <- All config (env variable references)
│   └── pom.xml
│
├── ai-service/                       <- Flask Python microservice
│   ├── routes/                       <- describe.py, recommend.py ...
│   ├── services/                     <- groq_client.py, chroma_client.py
│   ├── prompts/                      <- Prompt template text files
│   ├── app.py                        <- Flask entry point
│   ├── Dockerfile
│   └── requirements.txt
│
├── frontend/                         <- React + Vite frontend
│   ├── src/
│   │   ├── components/               <- Reusable UI components
│   │   ├── pages/                    <- List, Detail, Form, Login, Dashboard
│   │   ├── services/                 <- Axios API call functions
│   │   └── App.jsx                   <- Root component + routing
│   └── package.json
│
├── docker-compose.yml                <- Runs all 5 services together
├── .env.example                      <- All required environment variables
└── README.md
```

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose v2)
- Git

---

## 🚀 Quick Start

### 1 — Clone and configure

```bash
git clone https://github.com/dhanushshet14/pci-dss-compliance-tracker.git
cd pci-dss-compliance-tracker

# Copy the example env file and fill in your values
cp .env.example .env
```

Open `.env` and replace every `change_me_*` placeholder with real values:

| Variable | Description |
|----------|-------------|
| `POSTGRES_PASSWORD` | Strong password for the PostgreSQL superuser |
| `REDIS_PASSWORD` | Password for the Redis instance |
| `JWT_SECRET` | Random string ≥ 32 characters for signing JWTs |
| `GROQ_API_KEY` | Your Groq cloud API key |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP credentials for email alerts |

### 2 — Build and start all services

```bash
docker-compose up --build
```

Docker Compose will start the five services in dependency order:

```
postgres (healthy) ─┐
                    ├─► backend (healthy) ─► frontend
redis   (healthy) ─┘
                         ai-service (independent)
```

### 3 — Verify all services are healthy

```bash
# List all running containers and their status
docker-compose ps

# Check backend health endpoint
curl http://localhost:8080/actuator/health

# Check AI service health endpoint
curl http://localhost:5000/health

# Open the frontend in your browser
open http://localhost          # Linux/macOS
start http://localhost         # Windows
```

Expected output from `docker-compose ps`:

```
NAME                   STATUS
pci-dss-postgres       Up (healthy)
pci-dss-redis          Up (healthy)
pci-dss-backend        Up (healthy)
pci-dss-ai-service     Up (healthy)
pci-dss-frontend       Up
```

### 4 — Reset (wipe all data and rebuild)

```bash
# Stop containers AND remove all named volumes (database data, redis data)
docker-compose down -v

# Rebuild images from scratch and restart
docker-compose up --build
```

> ⚠️ `down -v` permanently deletes all persisted data. Use only when you want a clean slate.

---

## License

MIT