# PokerGTO — Architecture Proposal (Pre-Implementation)

Status: **Approved.** Phase 1 (project setup/scaffolding) complete. Phase 2 (authentication and users) in progress.

---

## 1. High-Level Architecture

```
┌─────────────────────┐        HTTPS/JSON        ┌──────────────────────────┐
│   Angular SPA        │ ────────────────────────▶│  Spring Boot REST API     │
│  (mobile-first)       │◀──────────────────────── │  (stateless, JWT)         │
└─────────────────────┘                            └──────────────┬───────────┘
                                                             │ JPA │ Admin API / token endpoint
                                                             ▼     ▼
                                              ┌───────────────────┐  ┌───────────┐
                                              │   PostgreSQL       │  │ Keycloak  │
                                              │   (Flyway-managed) │  │ (identity)│
                                              └───────────────────┘  └───────────┘
```

- **Single Angular app**, single Spring Boot app, single Postgres instance for the MVP. No microservices — the spec explicitly says avoid overengineering, and there's no scaling driver yet that justifies splitting services.
- **Identity: Keycloak**, not a hand-rolled auth system. Keycloak owns accounts/credentials/sessions; the backend proxies register/login/refresh/logout to it (Admin API for account creation, the token endpoint for login/refresh/logout grants) and validates the JWTs Keycloak issues as an OAuth2 Resource Server. The frontend never talks to Keycloak directly — it only calls `/api/auth/*`, which happens to be backed by Keycloak internally. This keeps the login UX fully custom (your own form, your own styling) while getting Keycloak's credential storage, hashing, and token issuing for free. Access token short-lived (returned in the response body); refresh token long-lived (httpOnly cookie, set by the backend). See §4a for detail.
- **Equity calculation** runs server-side (Monte Carlo / exact enumeration on small boards) as a plain Java service — not a separate microservice yet, but isolated behind an interface (`EquityCalculator`) so it can be extracted or replaced by a native solver service later without touching callers.
- **Content is data, not code**: every exercise, scenario, range, and lesson lives in Postgres and is served through DTOs. The Angular app has zero hard-coded poker content — only *renderers* for each exercise type.
- Everything runs via `docker compose up`: `postgres`, `backend`, `frontend` (nginx-served build in prod profile, `ng serve` proxy in dev profile).

### 1a. Auth flow (Keycloak, proxied)

```
Register: Angular form → POST /api/auth/register → AuthService →
  KeycloakClient.createUser()   [Admin API, backend's service-account token]
  → local User + UserProfile row created (id = Keycloak's user id)
  → KeycloakClient.passwordLogin()  [token endpoint, password grant]
  → 200 { accessToken, user } + Set-Cookie: refreshToken (httpOnly)

Login: Angular form → POST /api/auth/login → KeycloakClient.passwordLogin()
  → decode returned access token's "sub" → look up local User
  → 200 { accessToken, user } + Set-Cookie: refreshToken

Silent refresh (app init / access token expiry): POST /api/auth/refresh
  (browser sends the httpOnly cookie automatically) → KeycloakClient.refresh()
  → 200 { accessToken, user } + rotated Set-Cookie

Every other API call: Authorization: Bearer <accessToken>
  → Spring's oauth2ResourceServer validates the signature against
    Keycloak's JWKS (fetched from the realm's issuer-uri) — no DB hit,
    no call to Keycloak per request.
```

The backend's Keycloak client (`pokergto-backend`) is a **confidential client** — its secret lives only in backend env vars (`KEYCLOAK_CLIENT_SECRET`), never shipped to the browser. That's what makes the Resource Owner Password Credentials grant acceptable here despite OAuth2 generally discouraging it for public/browser clients: the password never leaves the boundary of (browser → our own backend over HTTPS in prod → Keycloak over the internal docker network).

## 2. Angular Project Structure

Angular 18+, standalone components (no NgModules), signals for local component state, a small `@ngrx/signals`-style store only where cross-cutting state is genuinely needed (auth/session, active exercise session). Avoid NgRx/Redux-style boilerplate — the spec asks for minimal state management.

```
frontend/
  src/app/
    core/                        # singleton services, guards, interceptors
      auth/                      # AuthService, JwtInterceptor, authGuard
      api/                       # typed HttpClient wrappers per resource
      state/                     # SessionStore (signals), UserStore
    shared/
      ui/                        # dumb, reusable UI atoms (Button, ProgressBar, XpBadge, Modal)
      poker/                     # domain-reusable, presentational poker widgets
        card/                    # <poker-card> (suit/rank render)
        range-matrix/            # <poker-range-matrix> — the 13x13 component (§20)
        table/                   # <poker-table> seats/chips/pot/dealer button
        chip-stack/
      pipes/ directives/
    features/
      auth/                      # login, register, profile
      dashboard/                 # §19 dashboard
      curriculum/                # course/module/lesson browsing
      lesson/                    # lesson viewer (theory content + inline exercises)
      exercise/                  # exercise engine — the core of §24
        engine/                  # ExerciseRunnerComponent: dispatches by exercise.type
        types/                   # one component per exercise type:
          multiple-choice/
          numerical/
          range-selection/
          range-construction/
          hand-reading/
          equity-estimation/
          pot-odds/
          street-decision/
      hand-trainer/              # §15 full hand simulation flow
      equity-calculator/         # §6 standalone tool
      progress/                  # progress + spaced-repetition views
      achievements/
    app.routes.ts                # lazy-loaded feature routes
    app.config.ts
  tailwind.config.js
```

Key decisions:
- **`shared/poker/*` components are pure/presentational** (inputs/outputs only, no HTTP) — reused by lesson viewer, exercise engine, and hand trainer alike, per §20's "reusable throughout the application" requirement.
- **Exercise engine is a dispatcher, not a monolith**: `ExerciseRunnerComponent` receives a generic `ExerciseDto`, looks at `.type`, and renders the matching typed component via `@Component` + `NgComponentOutlet` or a `switch` in the template. Adding a new exercise type = new component + registry entry, not a rewrite.
- **Mobile-first Tailwind**: base styles target the smallest breakpoint; `sm:`/`md:`/`lg:` add desktop enhancements. Poker table and range matrix use `viewBox`-based SVG/CSS scaling (not fixed px) so they resize cleanly, including a landscape layout variant for the table on phones.

## 3. Spring Boot Package Structure

Java 17, Spring Boot 3.x, package-by-feature (not package-by-layer) to keep each domain's controller/service/repo/dto together — scales better than a global `controllers/`, `services/` split as the curriculum/exercise domains grow.

```
backend/src/main/java/com/pokergto/
  PokerGtoApplication.java
  config/                # SecurityConfig, JwtConfig, WebConfig (CORS), OpenApiConfig
  common/
    exception/            # GlobalExceptionHandler (@ControllerAdvice), ApiError, custom exceptions
    validation/            # custom Bean Validation constraints (e.g. @ValidHoleCards)
    security/              # SecurityConfig support: KeycloakClient, KeycloakRealmRoleConverter, *Properties
  auth/
    controller/ AuthController                          # /api/auth/{register,login,refresh,logout}
    service/ AuthService                                 # orchestrates Keycloak calls + local User sync
    dto/ RegisterRequest, LoginRequest, AuthResponse, UserSummary
  user/
    User entity, UserRepository, ProfileController, ProfileService, dto/
  curriculum/               # Course, Module, Lesson, Topic
    controller/ service/ repository/ dto/ mapper/ (MapStruct)
  exercise/                 # the exercise engine domain
    Exercise, ExerciseOption, ExerciseAttempt
    controller/ service/ repository/ dto/ mapper/
    engine/                  # ExerciseSelectionService (next-exercise + spaced repetition logic)
    grading/                 # per-type AnswerGrader strategy interface + implementations
  poker/                     # pure poker domain logic, no JPA/web dependency
    model/                    # Card, HoleCards, Board, Range, Combo (value objects)
    equity/                    # EquityCalculator interface + MonteCarloEquityCalculator
    combinatorics/             # combo counting utilities (§9)
    scenario/                  # PokerScenario entity + repository/dto
    range/                     # Range, RangeEntry entities + RangeService
  handtrainer/               # HandSimulation entity/service — orchestrates street-by-street play
  progress/                  # Progress, Streak, XPTransaction, mastery/spaced-repetition scoring
  gamification/              # Achievement, UserAchievement, XP/level rules
  analytics/                 # lightweight event logging (attempt outcomes, session stats)
  admin/                     # (post-MVP) content CRUD, gated by ROLE_ADMIN
```

Key decisions:
- **`poker/` package has no Spring/JPA imports where possible** — equity math, combinatorics, and card models are plain Java, unit-testable in isolation, and portable if extracted into a shared library or a future solver microservice.
- **Grading is a Strategy pattern** (`AnswerGrader` per exercise type) rather than a giant `if/else` in one service, mirroring the frontend's per-type renderer split.
- **DTOs only cross the controller boundary**; MapStruct mappers convert entity ⇄ DTO. No entity ever gets serialized directly (avoids lazy-loading leaks and lets DB schema evolve independently of API contracts).
- **Flyway** migrations under `db/migration`, versioned `V1__init.sql`, `V2__...`.

## 4. PostgreSQL Schema (Core Entities)

Grouped by domain. Types are indicative (Postgres), not final DDL.

### Identity
- **users**(id, email UNIQUE, password_hash, display_name, role, created_at, last_login_at)
- **user_profiles**(user_id FK→users, avatar, bio, timezone, …) — split from `users` to keep auth table lean

### Curriculum
- **courses**(id, slug, title, description, order_index, is_published)
- **modules**(id, course_id FK, title, description, order_index, is_published)
- **lessons**(id, module_id FK, title, content (markdown/rich text), order_index, is_published)
- **topics**(id, slug, name, description) — a tagging/taxonomy table (e.g. `preflop.rfi`, `postflop.cbet`, `equity.outs`) referenced by both lessons and exercises for cross-cutting concept tracking (§28 "most frequently missed concepts")
- **lesson_topics**(lesson_id, topic_id) — M:N

### Exercise Engine
- **exercises**(id, topic_id FK, type ENUM[multiple_choice, numerical, range_selection, range_construction, hand_reading, equity_estimation, pot_odds, ev_calculation, street_decision, full_hand], difficulty ENUM[beginner, intermediate, advanced, expert], scenario_id FK→poker_scenarios NULLABLE, prompt, explanation, xp_reward, is_published, created_at)
- **exercise_options**(id, exercise_id FK, label, is_correct, correct_frequency NUMERIC(5,2) NULLABLE, sort_order) — supports both single-correct and mixed-frequency answers (§30)
- **exercise_concepts**(exercise_id, topic_id) — M:N, concepts touched by an exercise (an exercise can span multiple topics)
- **exercise_attempts**(id, user_id FK, exercise_id FK, selected_option_id FK NULLABLE, numeric_answer NUMERIC NULLABLE, submitted_range_id FK NULLABLE, is_correct, score NUMERIC(5,2) — partial credit for mixed-frequency answers, response_time_ms, attempted_at)

### Poker Domain
- **poker_scenarios**(id, num_players, hero_position, villain_position NULLABLE, effective_stack_bb, pot_bb, action_history JSONB, hero_cards VARCHAR(4) NULLABLE, board_cards VARCHAR(10) NULLABLE) — JSONB for the action sequence keeps this table from needing an `actions` join table for the MVP; can be normalized later if querying by individual actions becomes necessary
- **ranges**(id, name, position, vs_position NULLABLE, action_type ENUM[open, call, threebet, fourbet, defend], num_players, effective_stack_bb, source ENUM[curated, solver_import], created_by FK→users NULLABLE)
- **range_entries**(id, range_id FK, hand_combo VARCHAR(4) e.g. "AKs"/"AKo"/"22", action ENUM[raise, call, fold, threebet, fourbet], frequency NUMERIC(5,2)) — one row per hand-combo per action; a "mixed" cell is 2 rows (e.g. AQs: call 65 / threebet 35)

### Hand Trainer
- **hand_simulations**(id, user_id FK, scenario_id FK NULLABLE, num_players, effective_stack_bb, created_at, completed_at)
- **hand_simulation_streets**(id, simulation_id FK, street ENUM[preflop, flop, turn, river], board_cards, pot_bb, hero_action, hero_equity NUMERIC(5,2) NULLABLE, ev_estimate NUMERIC(6,2) NULLABLE, is_optimal, feedback TEXT)

### Progress & Gamification
- **progress**(id, user_id FK, topic_id FK, attempts_count, correct_count, mastery_score NUMERIC(5,2), consecutive_correct, last_attempted_at, next_review_at) — one row per user×topic; `next_review_at` drives spaced repetition (§18)
- **streaks**(id, user_id FK UNIQUE, current_streak, longest_streak, last_active_date)
- **xp_transactions**(id, user_id FK, amount, source_type ENUM[exercise, lesson_complete, streak_bonus, achievement], source_id, created_at)
- **user_levels**(user_id FK PK, level, total_xp) — denormalized rollup, recomputed from xp_transactions, kept for cheap dashboard reads
- **achievements**(id, code UNIQUE, name, description, icon, criteria JSONB)
- **user_achievements**(user_id FK, achievement_id FK, earned_at) — composite PK

### Indexes worth calling out up front
- `exercise_attempts(user_id, exercise_id, attempted_at)`, `progress(user_id, topic_id)` UNIQUE, `range_entries(range_id, hand_combo)` UNIQUE, `exercises(topic_id, difficulty, is_published)` for the "next exercise" selection query.

## 5. Entity Relationship Summary

```
User 1───* ExerciseAttempt *───1 Exercise *───1 Topic
User 1───1 Streak
User 1───1 UserLevel
User 1───* XPTransaction
User 1───* Progress *───1 Topic
User *───* Achievement  (via UserAchievement)
Course 1───* Module 1───* Lesson *───* Topic
Exercise *───1 PokerScenario
Exercise 1───* ExerciseOption
Range 1───* RangeEntry
HandSimulation 1───* HandSimulationStreet
HandSimulation *───1 PokerScenario (optional)
```

## 6. Initial REST API (MVP surface)

```
Auth
  POST   /api/auth/register
  POST   /api/auth/login
  POST   /api/auth/refresh
  POST   /api/auth/logout

Profile
  GET    /api/profile
  PATCH  /api/profile

Curriculum
  GET    /api/courses
  GET    /api/courses/{id}
  GET    /api/modules/{id}
  GET    /api/lessons/{id}

Exercises
  GET    /api/exercises/next?topicId=&difficulty=      # spaced-repetition-aware selection
  GET    /api/exercises/{id}
  POST   /api/exercises/{id}/attempt

Progress
  GET    /api/progress                                  # per-topic mastery breakdown
  GET    /api/progress/summary                          # dashboard rollup (XP, streak, level)

Ranges
  GET    /api/ranges?position=&vsPosition=&actionType=
  GET    /api/ranges/{id}

Equity
  POST   /api/equity/calculate                          # {heroCards, villainRange|villainCards, board}

Achievements
  GET    /api/achievements
  GET    /api/achievements/mine

Hand Simulations (MVP: create + record street decisions)
  POST   /api/hand-simulations
  POST   /api/hand-simulations/{id}/streets
  GET    /api/hand-simulations/{id}
```

All list endpoints paginated (`page`/`size`), all mutating endpoints validated via Bean Validation with a `GlobalExceptionHandler` returning a consistent `ApiError{status, code, message, fieldErrors[]}` shape.

## 7. MVP Scope (confirming §31)

- **Auth**: register/login/profile via Keycloak-backed `/api/auth/*`, access+refresh tokens.
- **Curriculum**: Fundamentals + Preflop + basic Postflop content, read-only lesson viewer.
- **Exercises**: multiple_choice, numerical (pot odds), equity_estimation (basic), street_decision (preflop only).
- **Interactive table**: static/simple `<poker-table>` used inside street_decision exercises — seats, cards, pot, one villain action shown, hero action buttons. Not the full animated multi-street engine yet.
- **Progress**: XP, accuracy, completed count, daily streak. Mastery scoring exists in schema but the *adaptive selection* algorithm can start simple (weakest-topic-first) and get smarter later.
- **Range matrix**: `<poker-range-matrix>` fully built and reusable, backing at least the "facing opens" preflop exercises.
- **Deferred past MVP**: full multi-street hand trainer with EV review, GTO module, combinatorics trainer, admin CMS UI (schema exists, no UI), achievements UI polish, solver import.

## 8. Technical Risks

1. **Equity calculation performance**: exact range-vs-range enumeration is expensive for wide ranges on the turn/river. Mitigation: Monte Carlo sampling with a fixed iteration budget for wide-range queries, exact enumeration only for narrow/heads-up-card cases; cache nothing user-specific, keep the calculator stateless and pure so it's trivially testable and swappable.
2. **Content authoring bottleneck**: curriculum quality depends on manually authored ranges/exercises with correct GTO-approximate frequencies. Mitigation: ship with a curated seed dataset (Flyway data migrations) sourced from well-established solver-approximate charts, and treat the schema's `source: solver_import` field as a forward door — not built in MVP.
3. **Mixed-frequency grading UX**: scoring "CALL 65% / 3-BET 35%" answers against a single user click needs a defined partial-credit formula (e.g. score = frequency of chosen action). This needs to be decided explicitly before building the grading service — flagging for your input rather than assuming.
4. **Mobile poker table complexity**: rendering 9 seats + cards + chips + pot legibly on a narrow portrait screen is a real design challenge. Mitigation: table component built mobile-first from day one (per §21), with a reduced-seat "focus view" (hero + active villain only) as the default mobile layout rather than shrinking a 9-max table.
5. **Scope creep vs. MVP discipline**: the spec is very large (GTO, solver import, hand trainer, admin CMS). Mitigation: schema is designed now to accommodate all of it (so no painful migrations later), but implementation strictly follows the Phase 1–10 order in §32.
6. **JWT refresh + Angular route guards**: needs care to avoid flicker/redirect loops on token expiry; standard interceptor + silent-refresh pattern, not novel.
7. **Keycloak as a moving part**: local dev now has a fourth container (Keycloak) with a realm imported from `keycloak/realm-export.json` on startup. This is convenient once running but is one more thing that can fail to start cleanly — worth an explicit `docker compose up` smoke test on a fresh machine before relying on it.

## 9. Key Architectural Decisions (rationale)

- **Package-by-feature backend, standalone-component frontend**: both minimize ceremony and keep related code together, matching "avoid overengineering" while staying scalable per-domain.
- **JSONB for `action_history` on scenarios**: action sequences are inherently variable-length and mostly read/displayed wholesale, not queried by individual action — a join table would add complexity with no query benefit yet.
- **Frequency-based `range_entries` and `exercise_options`** (rather than a single "correct answer" field) is the one piece of schema that *must* be right from day one, since it's structurally how §30's core teaching philosophy (frequencies, not absolutes) gets expressed everywhere downstream.
- **`poker/` domain package kept framework-free**: equity/combinatorics logic is the most reusable, most test-heavy, and most likely to be extracted (solver service) part of the backend — isolating it now avoids a painful untangling later.
- **No NgRx**: session/auth state and "current exercise session" state are the only genuinely cross-component state; Angular signals + a couple of small injectable stores cover this without Redux boilerplate.

---

## Decisions (confirmed)

1. **Partial-credit formula**: score = the frequency of the chosen action (e.g. choosing 3-bet when the answer is "Call 65% / 3-bet 35%" scores 0.35). Applies uniformly across `exercise_options.correct_frequency` and `range_entries.frequency`-graded exercises.
2. **MVP table sizi hae**: 9-max. Preflop content/seed data authored for the full UTG–BB position set.
3. **Seed content volume**: minimal demo set — enough per topic to exercise the engine end-to-end, expanded in later phases.
4. **Deployment target**: local only via `docker compose up`. Prod compose config stays cloud-agnostic; no provider-specific secrets/config wiring yet.

Proposal approved. Phase 1 (project setup and architecture scaffolding) begins next.
