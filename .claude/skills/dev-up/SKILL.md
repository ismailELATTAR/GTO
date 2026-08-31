---
name: dev-up
description: Launch the PokerGTO local dev stack (Postgres, Keycloak, Spring Boot backend, Angular frontend) via docker compose, wait for everything to actually be healthy, and report working URLs. Use when asked to run/start/launch the app locally, verify it's up, or debug why a service isn't responding.
---

# Launching the PokerGTO dev stack

This project's dev stack is `docker-compose.yml` (hot-reload, bind-mounted source — this is the one to use for local dev, *not* `docker-compose.prod.yml` which builds packaged images and requires prod secrets).

## Steps

1. **Check Docker Desktop is running** before anything else — `docker info` failing means Docker itself isn't up, not a project problem.

2. **Start the stack**:
   ```bash
   docker compose up -d
   ```
   Use `-d` so you can poll status rather than blocking on interleaved logs. If you do want live logs for debugging, run without `-d` or `docker compose logs -f <service>` after.

3. **Wait for each service in dependency order** — don't just check "container running", check actual readiness:
   - `postgres`: has a real healthcheck (`pg_isready`). Poll `docker compose ps postgres` until it shows `healthy`.
   - `keycloak`: runs `start-dev --import-realm`, importing `keycloak/realm-export.json` on boot. This takes several seconds after container start — it has no healthcheck in the compose file, so poll the actual endpoint instead: `curl -sf http://localhost:8081/realms/pokergto` (or whatever `KEYCLOAK_REALM` is set to) returning 200 means the realm import finished and it's ready to issue tokens. Don't declare the backend broken just because it can't fetch JWKS in the first few seconds — that's almost always Keycloak still importing.
   - `backend`: first run is slow (`./mvnw spring-boot:run` pulls the internet's worth of Maven deps into the `pokergto_m2` volume). Poll `curl -sf http://localhost:8080/api/health`. If it fails, check `docker compose logs backend` for either "still resolving dependencies" (just wait) or an actual stack trace (real failure — likely Keycloak not ready yet when the backend tried to fetch JWKS, or a Flyway migration error).
   - `frontend`: first run does `npm install` before `ng serve`, also slow. Poll `http://localhost:4200` for a 200/response. Not critical-path for backend work.

4. **Report back**:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080/api (health: `/api/health`)
   - Keycloak admin console: http://localhost:8081 (admin/admin per compose defaults)
   - Postgres: localhost:5432 (db/user/password default to `pokergto`/`pokergto`/`pokergto` unless overridden by `.env`)

## Common failure modes

- **Backend can't validate tokens / 401s that shouldn't happen**: Keycloak realm import wasn't finished when the backend started or when it first fetched JWKS. Restart just the backend after confirming Keycloak's realm endpoint responds: `docker compose restart backend`.
- **Port already in use** (5432, 8080, 8081, 4200): something else on the host is bound to it — check with the platform's port-inspection tool, not by editing the compose file unless the user asks to change ports permanently.
- **Backend fails on a Flyway migration**: check `docker compose logs backend` for the specific `V<n>__*.sql` that failed — usually a bad migration just added, or a leftover Postgres volume from an incompatible earlier schema (`docker compose down -v` to nuke the volume is destructive — confirm with the user before doing that, it deletes all local data).
- **Frontend can't reach backend**: confirm `CORS_ALLOWED_ORIGINS` (backend env) includes `http://localhost:4200` — it does by default in the dev compose file, so this only comes up if it's been overridden.

## Stopping

```bash
docker compose down
```
Add `-v` only if the user explicitly wants to wipe the Postgres volume (all local users/data gone) — never do this without asking.
