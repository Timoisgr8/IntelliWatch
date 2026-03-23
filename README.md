# IntelliWatch

A threat intelligence feed that aggregates articles from HackerNews, analyses them using Google Gemini AI, and presents risk-tagged summaries through a clean dashboard interface.

Built as a portfolio project to demonstrate full stack development with Java, Spring Boot, PostgreSQL, and generative AI integration.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue) ![Gemini](https://img.shields.io/badge/Gemini-2.5%20Flash-purple)

---

## Overview

IntelliWatch fetches the top stories from HackerNews, sends each article to Google Gemini for threat and risk analysis, stores the results in PostgreSQL, and displays them on a filterable dashboard.

Each article is analysed for:
- A 2–3 sentence summary
- Risk tags (`CYBERSECURITY`, `GEOPOLITICAL`, `AI_RISK`, `MISINFORMATION`, `ECONOMIC`, `PRIVACY`, `INFRASTRUCTURE`, `LOW_RELEVANCE`)
- A relevance confidence score (0.0–1.0)

---

## Architecture

```
Frontend (HTML / Tailwind CSS / Vanilla JS)
            ↕ REST API
Spring Boot (Java) — ArticleController
    ├── ArticleFetcherService   → HackerNews Firebase API
    ├── AnalysisService         → Google Gemini API
    └── ArticleRepository       → PostgreSQL via Spring Data JPA
```

### Project Structure

```
intelliwatch/
├── src/
│   ├── main/
│   │   ├── java/com/intelliwatch/
│   │   │   ├── controller/
│   │   │   │   └── ArticleController.java
│   │   │   ├── model/
│   │   │   │   └── Article.java
│   │   │   ├── repository/
│   │   │   │   └── ArticleRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ArticleFetcherService.java
│   │   │   │   └── AnalysisService.java
│   │   │   └── IntelliwatchApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html          ← Frontend dashboard
│   │       ├── db/migration/
│   │       │   └── V1__create_articles_table.sql
│   │       ├── application.properties          ← Local only, gitignored
│   │       └── application.properties.example  ← Safe config template
│   └── test/
├── pom.xml
└── README.md
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| AI | Google Gemini 2.5 Flash |
| HTTP Client | Spring RestTemplate |
| Build Tool | Maven |
| Frontend | HTML, Tailwind CSS, Vanilla JS |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven
- PostgreSQL
- A Google Gemini API key — get one free at [aistudio.google.com](https://aistudio.google.com)

### 1. Clone the repository

```bash
git clone https://github.com/timoisgr8/intelliwatch.git
cd intelliwatch
```

### 2. Create the database

```sql
CREATE DATABASE intelliwatch;
```

### 3. Configure the application

Copy the example config and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/intelliwatch
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

gemini.api.key=YOUR_GEMINI_API_KEY
```

### 4. Run the application

```bash
mvn spring-boot:run
```

Flyway will automatically create the database schema on first run.

Open `http://localhost:8080` to access the dashboard.

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/articles` | Get all stored articles |
| `GET` | `/api/articles/{id}` | Get a single article by ID |
| `POST` | `/api/articles/fetch` | Fetch latest top stories from HackerNews |
| `POST` | `/api/articles/{id}/analyse` | Analyse a single article with Gemini |
| `POST` | `/api/articles/analyse-all` | Analyse all unanalysed articles |

### Example response

```json
{
  "id": 1,
  "title": "New vulnerability found in OpenSSL",
  "url": "https://example.com/article",
  "source": "HackerNews",
  "fetchedAt": "2026-03-23T10:41:36.566",
  "summary": "A critical vulnerability has been discovered in OpenSSL affecting versions 3.x. Researchers recommend immediate patching as the flaw allows remote code execution.",
  "riskTags": ["CYBERSECURITY", "INFRASTRUCTURE"],
  "confidenceScore": 0.92
}
```

---

## Risk Tags

| Tag | Description |
|---|---|
| `CYBERSECURITY` | Security vulnerabilities, breaches, exploits |
| `GEOPOLITICAL` | International relations, conflict, sanctions |
| `AI_RISK` | AI safety, misuse, or systemic risks |
| `MISINFORMATION` | Disinformation campaigns, fake news |
| `ECONOMIC` | Market instability, financial threats |
| `PRIVACY` | Data exposure, surveillance |
| `INFRASTRUCTURE` | Critical systems, supply chain |
| `LOW_RELEVANCE` | General tech news with minimal threat relevance |

---

## Rate Limits

The Gemini free tier allows **20 requests per day** and **5 requests per minute**. The `analyseAll` endpoint includes a 15-second delay between requests to respect the rate limit. For higher throughput, add billing to your Google AI Studio account.

---

## Design Decisions

**Why Spring Boot?**
Spring Boot is the industry standard for Java backend development. Its convention-over-configuration approach, built-in dependency injection, and rich ecosystem made it the right choice for a production-style application.

**Why Flyway for migrations?**
Schema migrations should be versioned and reproducible. Flyway ensures the database schema is always consistent across environments — the same principle as Goose in Go or EF Core migrations in .NET.

**Why a separate `ArticleFetcherService` and `AnalysisService`?**
Separating concerns means each service has a single responsibility. Fetching and AI analysis are independent operations — you can fetch without analysing, and the analysis service could be swapped for a different AI provider without touching the fetching logic.

**Why store `riskTags` as a PostgreSQL array?**
Articles can belong to multiple risk categories. A native `TEXT[]` array avoids the overhead of a join table for this use case while keeping the schema simple. A normalised tags table would be the right choice at larger scale.

**Why vanilla JS for the frontend?**
The frontend is intentionally lightweight — no framework overhead, no build step, served directly by Spring Boot's static file serving. This keeps the project self-contained and demonstrates that clean UI doesn't require a heavy framework.

---

## Future Improvements

- **Semantic search** using pgvector and Gemini embeddings to find similar articles
- **Scheduled fetching** via Spring `@Scheduled` to automatically pull new articles
- **Webhook alerts** for high-confidence threat articles
- **Authentication** with Spring Security and JWT
- **Pagination** on the articles endpoint for larger datasets
- **Containerisation** with Docker and Docker Compose for consistent deployment

---

## License

MIT