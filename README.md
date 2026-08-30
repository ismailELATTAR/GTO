# PokerGTO

A mobile-first, gamified No-Limit Hold'em theory and training platform —
learn → practice → get feedback → track progress → advance. See
[ARCHITECTURE.md](ARCHITECTURE.md) for the full design proposal and current
build phase.

## Stack

- **Frontend**: Angular 22 (standalone, zoneless), TypeScript, Tailwind CSS
- **Backend**: Java 17, Spring Boot 4 (Web, Data JPA, Security, Validation, Flyway)
- **Database**: PostgreSQL 16
- **Auth**: JWT (email/password now, OAuth-ready later)

## Run locally

Requires Docker Desktop.

```bash
docker compose up
```

That's it — this runs the **dev** stack (hot-reload, bind-mounted source) by default:

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api (health check: `/api/health`)
- Postgres: localhost:5432

First run pulls base images and installs dependencies inside the containers
(cached in named volumes afterward), so it's slower the first time.

### Production-style build

```bash
docker compose -f docker-compose.prod.yml up --build
```

Builds the packaged jar + nginx-served Angular bundle. Requires `DB_PASSWORD`,
`JWT_SECRET`, and `CORS_ALLOWED_ORIGINS` to be set (see `.env.example`) — it
fails fast if they're missing rather than falling back to dev defaults.

### Without Docker

**Backend** (needs a local Postgres, or point `DB_URL` at one):
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend**:
```bash
cd frontend
npm install
npx ng serve
```

## Project status

Currently: **Phase 1 — project setup and architecture scaffolding**, per the
phased plan in `ARCHITECTURE.md`. Most feature packages/routes exist as
scaffolding placeholders (see the `package-info.java` files in the backend
and the placeholder components in the frontend) — they're implemented
incrementally, one phase at a time.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — architecture proposal, schema, API, decisions
- API.md, DATABASE.md, CONTRIBUTING.md — added as their respective domains are built
