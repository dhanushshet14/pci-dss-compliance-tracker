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

- Java 17+
- Node.js 18+
- Python 3.11+
- PostgreSQL 15
- Redis 7
- Docker & Docker Compose (optional)

### Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and fill in the values
3. Run with Docker Compose:
   ```bash
   docker-compose up --build
   ```

   Or run each service individually — see each subfolder for instructions.

## License

MIT