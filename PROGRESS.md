# PokerGTO — Progress & Handoff

Status snapshot for starting a fresh conversation. Read this alongside
[ARCHITECTURE.md](ARCHITECTURE.md) (the approved design doc — schema, API,
phase plan, key decisions) and [README.md](README.md) (how to run it).

---

## How to pick this back up

```bash
docker compose up
```

This brings up Postgres, Keycloak, the backend (hot-reload via `mvnw spring-boot:run`),
and the frontend (hot-reload via `ng serve`). URLs:

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api (health: `/api/health`)
- Keycloak admin console: http://localhost:8081 (admin/admin)
- Postgres: localhost:5432 (pokergto/pokergto/pokergto)

A test account already exists (created during the Phase 5 session):
**`phase5test@example.com`** / **`password123`**. You can also register a
new one at http://localhost:4200/auth/register. (Earlier test accounts —
`final@example.com`, `phase4test@example.com` — no longer exist; Postgres
data doesn't survive a `docker compose down`/volume recreation between
sessions. Don't be surprised if `phase5test@example.com` is also gone by
the time you read this — just register a fresh one.)

**Known gotcha**: the Angular dev container occasionally serves a **stale
`.angular/cache`** after edits made outside its own watch cycle. If the
dev server seems to be running old code, `rm -rf frontend/.angular/cache`
and `docker compose restart frontend`.

**Related gotcha, client side**: even after the dev server itself is fresh,
a browser tab left open from before the restart keeps running the old JS
bundle in memory (SPA — no full reload happens on its own) and can misroute
or otherwise act stale. Hit this after the frontend restart earlier in
Phase 4 — a report of "Browse lessons leads to the same page as Start
training" turned out to be a stale tab, not a code bug (confirmed by
testing fresh in a clean Playwright session, which routed correctly). If a
reported frontend bug doesn't reproduce in a fresh page load, ask for a
hard refresh (Ctrl+Shift+R) before digging into the code.

**Note on skills/agents**: this project has `.claude/agents/*.md` and
`.claude/skills/*.md` (reviewer subagents, a `dev-up` launcher skill, a
`frontend-design` skill, a `new-exercise-type` scaffolding skill) from an
earlier setup. We deliberately set those aside for this session to control
token usage — plain inline work only. They're still there if wanted later;
`new-exercise-type/SKILL.md`'s section-number citations were already fixed
to point at real `ARCHITECTURE.md` sections.

---

## What's done

### Phase 1 — Project setup ✅
- `ARCHITECTURE.md` written and approved (schema, API, phased plan).
- Backend: Spring Boot **4.1.1**, Java 17, Maven (`mvnw`, no local Maven needed), package-by-feature under `com.pokergto`.
- Frontend: Angular **22** (zoneless, standalone components), Tailwind **v4** (CSS-based `@theme`, no `tailwind.config.js`).
- `docker-compose.yml` (dev, hot-reload, default for `docker compose up`) + `docker-compose.prod.yml` (packaged build, standalone, needs secrets).
- **Keycloak** added mid-Phase-2 as a deliberate pivot from a hand-rolled JWT system (see ARCHITECTURE.md §1a for the full flow). Realm/client/roles declaratively imported from `keycloak/realm-export.json` on container startup — no manual admin-console setup needed.

### Phase 2 — Authentication & users ✅ (fully verified against the live stack)
- **Backend**: `common/security` (`KeycloakClient` — talks to Keycloak's Admin API + token endpoint; `KeycloakRealmRoleConverter` — maps Keycloak's `realm_access.roles` claim to Spring authorities), `config/SecurityConfig` (OAuth2 Resource Server, validates Keycloak-issued JWTs via `issuer-uri`), `auth/` (`AuthController`/`AuthService`/DTOs — register/login/refresh/logout, refresh token as httpOnly cookie), `user/model` (`User`, `UserProfile`, `UserRole`), `user/repository`.
- **Frontend**: `core/state/session.store.ts` (signals-based session), `core/auth/` (`AuthService` with `withCredentials`, `restoreSession()` for silent refresh; `auth.interceptor.ts` with 401 → single shared refresh → retry; `auth.guard.ts`), `features/auth/login`, `features/auth/register` (Reactive Forms), `app.config.ts` wired with `provideAppInitializer` for session restore on load.
- Migration `V1__create_users_table.sql`.
- **Real bugs found & fixed this session** (all via actually running the stack, not just compiling): `RestClient.Builder` not autowiring (switched to `RestClient.builder()` directly), `GlobalExceptionHandler` swallowing stack traces (added logging), `UserProfile`'s `@MapsId` misuse (two separate bugs — wrong field set, then a JPA `merge()`-returns-a-new-instance gotcha), Keycloak silently requiring `lastName` (not just `firstName`) for a usable account, interceptor missing 401 recovery, `logout()` not navigating away.

### Dashboard (logged-in landing page) ✅
- `shared/poker/chip-stack` — reusable component (signal inputs), stats shown as poker chip stacks rather than generic KPI tiles.
- `features/dashboard` — felt-gradient background, rail-curved content panel, time-of-day greeting, chip-stack stats (**explicitly marked placeholder** in code — no real progress API exists yet, that's Phase 6), "Start training" CTA.
- Fonts (Sora/Inter) actually loaded in `index.html` (were declared as CSS tokens in Phase 1 but never wired up until now).

### Phase 3 — Curriculum ✅ (fully verified, including a real bug fix)
- Migrations: `V2__create_curriculum_tables.sql` (topics, courses, modules, lessons, lesson_topics), `V3__seed_fundamentals_course.sql` (one course, one module, two lessons with real markdown content).
- **Backend**: `curriculum/model` (`Course`, `CourseModule`, `Lesson`, `Topic`), `curriculum/repository`, `curriculum/dto` (summary/detail DTO split), `curriculum/mapper` (first real MapStruct usage in the project), `curriculum/service`, `curriculum/controller`. Endpoints: `GET /api/courses`, `/api/courses/{id}`, `/api/modules/{id}`, `/api/lessons/{id}` — all require an authenticated bearer token.
- **Real bug found and fixed**: publish-state wasn't checked transitively (an unpublished course's published module/lesson was reachable directly by ID). Fixed via repository-level join queries (`findByIdAndPublishedTrueAndCourse_PublishedTrue` etc.) — **verified by actually seeding the leak scenario and confirming 404**, not just by re-reading the code.
- **Naming fix**: entity `Module` renamed to `CourseModule` (was shadowing `java.lang.Module`).
- **Structural convention established**: entities live in a `model/` subfolder within each feature package (`curriculum/model/`, `user/model/`), separate from `controller/service/repository/dto/mapper`. This was a deliberate choice — apply it to any new feature package (`exercise/`, `poker/`, etc.) going forward. (Note: `poker/*` was originally designed with entity+repository colocated per sub-domain package, e.g. `poker/scenario/` — that's a different, still-valid convention for that specific package; not yet built, so not yet reconciled with the `model/` pattern. Worth a conscious decision when `poker/` is actually built.)
- **Frontend**: `core/api/curriculum.service.ts`, `features/curriculum` (course list) → `course-detail` → `module-detail` → `features/lesson` (renders markdown via `marked` + `@tailwindcss/typography`, `prose prose-invert` styling). Routes: `/curriculum`, `/courses/:id`, `/modules/:id`, `/lesson/:id`.

---

## Phase 4: Exercise Engine — `multiple_choice` slice ✅ (fully verified, including a browser click-through)

**Scope decision made**: build the full grading Strategy-pattern architecture, but implement it end-to-end for **one** exercise type first — `multiple_choice` (it's what the spec's own worked example uses, and it exercises the interesting part: frequency-based partial credit, e.g. "Call 65% / 3-Bet 35%"). The other MVP-listed types (`numerical`/pot-odds, `equity_estimation`, `street_decision`) extend the same pattern — same shape of work, not yet started.

- Migrations `V4`/`V5` (tables + seed data) as previously written — unchanged.
- `exercise/model/Exercise.java`, `ExerciseOption.java`, `ExerciseAttempt.java` — following the `model/` folder convention.
- `exercise/repository/ExerciseRepository` (finder methods for the MVP "first published match" selection used by `getNext`), `ExerciseAttemptRepository`.
- `exercise/dto/` — pre-answer (`ExerciseDetailDto`/`ExerciseOptionDto`, no answer key) vs. post-answer (`AttemptResponseDto`/`AttemptOptionResultDto`, answer key revealed) split, enforced by MapStruct only ever mapping into the pre-answer DTO's fields.
- `exercise/grading/AnswerGrader` (strategy interface) + `MultipleChoiceGrader` — `score = option.correctFrequency ?? (option.correct ? 100 : 0)`. `ExerciseService` autowires `List<AnswerGrader>` and builds a `Map<ExerciseType, AnswerGrader>` from each bean's `supportedType()`.
- `exercise/mapper/ExerciseMapper`, `exercise/service/ExerciseService`, `exercise/controller/ExerciseController` — `GET /api/exercises/next`, `GET /api/exercises/{id}`, `POST /api/exercises/{id}/attempt`.
- **Resolved the `@AuthenticationPrincipal` question** flagged at the end of the last session: since the Keycloak pivot it resolves to a `Jwt` (not a raw `UUID`) — read the user id via `jwt.getSubject()`, same as `AuthService` already does.
- Added `ApiException.badRequest(String)`.
- **Frontend**: `core/api/exercise.service.ts`; `features/exercise/engine/exercise-runner.ts` is now a real dispatcher (`@switch` on `exercise.type`, owns the attempt HTTP call/state); `features/exercise/types/multiple-choice/` is the presentational renderer (prompt, option buttons, post-answer per-option frequency/correctness + explanation + score).
- **Real bug found (via the user actually clicking around, not caught by curl or `ng build`)**: nothing in the UI linked to `/exercise/:id` at all — the dashboard's "Start training" button went to `/curriculum` (lessons), and there was no exercise list/browse page either. `ExerciseRunner` only knows how to render a *specific* id, so it was completely unreachable. Fixed by adding `features/exercise/engine/exercise-entry.ts` (`/practice` route — calls `GET /api/exercises/next` and redirects to the returned id) and pointing the dashboard's "Start training" CTA at `/practice`; added a second "Browse lessons" button so the pre-existing `/curriculum` link didn't just disappear. **There's still no real exercise browse/list UI** (pick by topic/difficulty) — `/practice` always takes whatever `getNext()` returns, same MVP-placeholder selection the backend does. A real "pick what to practice" page is follow-up work, not blocking.

**Verified against the live stack** (not just compiled):
- `mvn compile` and `ng build` both clean.
- `curl`'d all three endpoints with a real bearer token: pre-answer DTO confirmed to omit `correct`/`correctFrequency`; picking the 35%-frequency "3-Bet" option on the AQs exercise returned `score: 35.00` (not 0 or 100 — the partial-credit case that matters most); a wrong pick on the same exercise returned `0`; the correct pick on the binary 72o exercise returned `100`; an invalid `selectedOptionId` returned `400`.
- Confirmed the attempt rows actually landed in Postgres (`SELECT * FROM exercise_attempts`).
- Installed Playwright (`frontend/node_modules`, dev-only, not committed) and drove the real browser end-to-end: registered/logged in through the actual login form, navigated to `/exercise/e0000...101`, clicked "3-Bet", confirmed "Score: 35%" rendered with the explanation, and checked computed styles to confirm both `Call` (65%) and `3-Bet` (35%) got the mixed-frequency highlight class while `Fold` got the muted/incorrect one. One console 401 appeared (`POST /api/auth/refresh`) — confirmed **pre-existing Phase 2 behavior**, not a regression: it's the silent-refresh attempt on every fresh page load before any session cookie exists, reproduces identically on the login page alone.

---

## Phase 4: `numerical` (pot-odds) slice ✅ (fully verified, including a browser click-through)

Second exercise type, same shape of work as `multiple_choice` but genuinely different mechanics: a free-typed number instead of picking an option, graded by distance-to-answer instead of frequency.

- `V6__add_numerical_exercise_fields.sql` — `exercises` gets `correct_answer`/`answer_tolerance`/`answer_unit` (NULL for every other type; `exercise_options` is multiple_choice-specific, so this type needed its own answer-key columns). `V7__seed_numerical_exercises.sql` — two pot-odds exercises (new topic `math.pot-odds`).
- `Exercise.java` — three new nullable fields, documented as "only set for NUMERICAL."
- `grading/NumericalGrader.java` — three-tier scoring: within `answerTolerance` → 100, within 2× tolerance → 50 (partial credit for "close"), further → 0. Same "reward being close" philosophy as multiple_choice's frequency credit, just for a continuous value instead of discrete options.
- `ExerciseDetailDto` gained `answerUnit` (display-only, e.g. "%" — safe pre-answer, not the key); `AttemptResponseDto` gained `correctAnswer` (null except for NUMERICAL, revealed only post-attempt).
- Frontend: `features/exercise/types/numerical/` — number input + unit suffix + "Check answer" button (Enter key also submits); `exercise-runner.ts`'s per-type HTTP-submit logic was generalized into one `submit(request: AttemptRequest)` used by both `onSelect`/`onNumericAnswer` rather than duplicated per type.
- **Verified live**: curl'd all tiers (exact→100, close→50, way off→0, missing input→400) against the real backend after a Flyway migration run; confirmed attempts persisted in Postgres; then re-verified the same three cases through an actual browser (Playwright) after restarting the frontend container — screenshots confirm the score banner colors correctly (green for exact, gold/accent for the 50-point "close" tier, matching the tone system built during the multiple_choice polish pass, not a flat red/green split).
- **Real bug found (user noticed "Start training" always returns the exact same exercise)**: `getNext`'s MVP placeholder was `ORDER BY created_at ASC` + take the first — deterministic, not just "no ranking yet" as the comment implied. With 4 published exercises across 2 seed migrations, that always resolved to the same row (the earliest-inserted one). Fixed to pick uniformly at random among candidates instead — still not real spaced-repetition selection (still Phase 6), but no longer stuck in a loop. Verified with 10 consecutive `curl` calls to `/api/exercises/next` returning a mix of all 4 exercise ids.

## Phase 4: `equity_estimation` slice ✅ (fully verified, including a browser click-through)

Third exercise type — the "rule of 4 and 2" outs heuristic (e.g. "9 outs, two cards to come — roughly what's your equity?"). Text-describable, so no Phase 5 card/table visuals needed for it, unlike `street_decision`.

- **Mechanically identical to `numerical`** (free-typed number, tolerance-graded) — so rather than duplicating the grading logic and the frontend renderer, both now share code:
  - Backend: extracted the three-tier scoring out of `NumericalGrader` into an abstract `ToleranceGrader` base class; `NumericalGrader` and the new `EquityEstimationGrader` are now each a ~10-line subclass that only declares `supportedType()`.
  - Frontend: `exercise-runner.html`'s `@case ('EQUITY_ESTIMATION')` reuses `<app-numerical-exercise>` — no new component needed. (Angular's `@switch` has no case-fallthrough, so the case block itself is duplicated even though the component isn't.)
- `V8__seed_equity_estimation_exercises.sql` — two exercises under a new `equity.outs` topic. **No schema migration needed** — reuses the `correct_answer`/`answer_tolerance`/`answer_unit` columns `V6` added for `numerical`.
- **Verified live**: curl'd all three tiers (exact→100, close→50, way off→0) plus the pre-answer DTO omitting `correctAnswer`; then drove it through an actual browser after restarting both containers (V8 migration confirmed applied in backend logs) — typed "36", got "Score: 100% — Correct answer: 36%", zero console errors.

---

## Practice selection screen ✅ (fully verified, including a browser click-through)

User asked for a picker (difficulty + exercise type) before "Start training" drops them into an exercise, rather than a blind random pick.

- **Backend**: `ExerciseRepository`'s 4 branching finder methods (topic × difficulty combinations) were replaced with one `@Query` using `(:param IS NULL OR field = :param)` per filter — adding a 3rd filter (type) would've meant 8 branches otherwise. `getNext(topicId, difficulty, type)` and the `/api/exercises/next?type=` query param now support filtering by exercise type, not just topic/difficulty.
- **Frontend**: `exercise-entry.ts`/`.html` (`/practice`) is now a real selection screen — pill-style toggles for difficulty (Any/Beginner/Intermediate/Advanced/Expert) and exercise type, defaulting to "Any" (so leaving everything untouched behaves like the old no-picker flow). The type list is deliberately hardcoded to only the types with a real frontend renderer (`MULTIPLE_CHOICE`/`NUMERICAL`/`EQUITY_ESTIMATION`) — offering `STREET_DECISION` etc. would just land on `ExerciseRunner`'s "no renderer yet" fallback. A 404 (no exercise matches the chosen combination) shows an inline message rather than a broken navigation.
- **Verified live**: curl'd type-only, difficulty-only (including a combination with zero matches → confirmed 404), and combined type+difficulty filters against the real backend; then drove the full flow through an actual browser — picked "Pot Odds" + "Beginner", clicked Start, landed on exactly the one exercise matching both filters (`...201`).

---

## Real bug found and fixed: `getNext` always returned the same exercise

User noticed "Start training" never varied. The MVP placeholder comment said "no ranking yet," but the actual behavior was `ORDER BY created_at ASC` + take the first — fully deterministic, not just unranked. With a small fixed seed set, that always resolved to the same row. Fixed to pick uniformly at random among candidates (still not real spaced-repetition selection — that's still Phase 6 — but no longer stuck in a loop). Verified via repeated `curl` calls returning a mix of exercise ids; after the `equity_estimation` slice landed, re-verified all 6 published exercises now show up across repeated calls.

---

## Phase 5 — Interactive Poker Table ✅ (fully verified, including a browser click-through)

Built on branch `phase-5-poker-table` (not yet merged to `main` — see "How to pick this back up"). Both halves (frontend components, backend poker domain) are done, and they're wired together end to end via the `street_decision` exercise type, which closes out the last item Phase 4 was blocked on.

**Frontend** (committed earlier in this branch's history):
- `shared/poker/card` — a single playing card, SVG with a `viewBox` so it scales at any size.
- `shared/poker/table` — seats (any count, positioned via computed ellipse trigonometry, not hardcoded per table size), hole cards, folded/acting/dealer states, 5-slot board with dashed placeholders, pot display. Built on `PokerCard`.
- `/poker-demo` — permanent (not user-flow) reference page showing both in isolation.

**Backend poker domain** (`poker/`, framework-free per ARCHITECTURE.md §3):
- `poker/model/Card.java`, `HoleCards.java`, `Board.java` — plain Java records, no JPA/Spring. Parse/render the same two-character notation (`"As"`, `"QsJs"`, `"Kh7d2s"`) the frontend's `shared/poker/card` already uses, so cards round-trip through JSON with no translation layer. Verified via unit tests (`CardTest`/`HoleCardsTest`/`BoardTest`, 24 cases total) rather than curl/browser — these types have no HTTP endpoint of their own.
- `poker/scenario/PokerScenario.java` + `PokerScenarioRepository.java` — the one package with entity+repository colocated (a deliberate, documented exception to the rest of the backend's `model/`-subfolder convention — see "Backend package convention" below). `action_history` is a `jsonb` column mapped to a plain `String` field (`@JdbcTypeCode(SqlTypes.JSON)`) — passed through as raw JSON text rather than deserialized into a Java type, since nothing on the backend inspects individual actions yet, only hands them to the frontend to render.
- `V9`/`V10`/`V11` migrations: create `poker_scenarios`, add the `exercises.scenario_id` FK that `V4` had deliberately deferred, seed one `street_decision` exercise with a real scenario.

**Tying it together** — `street_decision` exercises are mechanically identical to `multiple_choice` (both pick from `exercise_options`, graded the same way) but carry a `scenario` for the frontend to render on an actual table instead of describing the hand in prose:
- Extracted `MultipleChoiceGrader`'s scoring logic into an abstract `OptionGrader` base class; `MultipleChoiceGrader` and the new `StreetDecisionGrader` are each now a ~10-line subclass — same shape of extraction as `ToleranceGrader`/`NumericalGrader`/`EquityEstimationGrader` in Phase 4.
- `ExerciseDetailDto` gained a nullable `scenario: PokerScenarioDto` (safe to show in full pre-answer — a scenario carries no answer-key data, just the scene).
- Frontend: extracted the option-button-list + score-banner UI (previously embedded directly in `MultipleChoiceExercise`) into a shared `OptionPicker` component — the frontend mirror of the `OptionGrader` extraction — now used by both `MultipleChoiceExercise` and the new `StreetDecisionExercise`. The latter also renders `PokerTableComponent` from the scenario data (hero + villain only — a "focus view," matching ARCHITECTURE.md §21's documented mobile-first default, and also just what the data actually has: no full 9-max seating, only hero/villain positions) and a parsed action-history line above it.
- Added `STREET_DECISION` to the `/practice` type picker now that it has a renderer.

**Verified live**: curl'd the pre-answer DTO (scenario present, answer key absent), both grading cases (Call → 70, matching frequency; Fold → 0), confirmed the attempt rows persisted in Postgres, confirmed the `type=STREET_DECISION` filter on `/api/exercises/next` resolves correctly. Then drove the whole thing through a real browser: logged in, navigated to the exercise directly, screenshotted the table rendering the actual scenario (action history, villain's dealer button, hero's real QsJs), clicked "Call," confirmed the score banner and mixed-frequency option tones (Call 70%/3-Bet 30% both gold) render correctly — then separately verified the `/practice` picker's new "Street Decision" filter routes to it. Also ran `mvn test`: all 24 new poker-domain unit tests pass (the only failure in a full `mvn test` run is a pre-existing, environment-dependent `@SpringBootTest` that needs a live Postgres connection — unrelated to this work, not something `mvn test` alone can exercise without `docker compose up` first).

---

## What's left overall (per ARCHITECTURE.md's phase plan)

- **Phase 4**: done — `multiple_choice`, `numerical`, `equity_estimation`, and (as of Phase 5 above) `street_decision` are all built, graded, and verified.
- **Phase 5**: done (above). `phase-5-poker-table` branch is ready to merge to `main` whenever that's wanted — ask before merging, it wasn't done as part of this work.
- **Phase 6 — Progress/Gamification**: `progress`/`gamification` packages (XP, streaks, mastery scoring, spaced repetition via `next_review_at`), `GET /api/progress`, and — not explicitly phased but MVP-scoped per ARCHITECTURE.md §7 — the **profile endpoints** (`GET`/`PATCH /api/profile`) were never built; `user_profiles` table exists and is populated empty on registration, but nothing reads/writes it yet. Dashboard's chip-stack stats become real once this phase lands. This is also when `getNext`'s random-pick placeholder should become real spaced-repetition selection.
- **Phase 7 — Range Visualizer**: `shared/poker/range-matrix` (the 13×13 component), `Range`/`RangeEntry` entities, `poker/range` package, `GET /api/ranges`.
- **Phase 8 — Postflop exercises**: board texture, c-betting, etc. — more content + new exercise types.
- **Phase 9 — Advanced GTO modules**.
- **Phase 10 — Full hand simulator**: `handtrainer` package, multi-street hand review.
- **Post-MVP / explicitly deferred**: admin CMS UI (schema/roles exist, no UI), solver import, everything in ARCHITECTURE.md §36 (leaderboards, daily challenges, etc.).

---

## Useful facts for the next session

- Migrations are at **`V11`**. Next one is `V12`.
- `frontend/node_modules` now has Playwright installed (dev-only, `npm install --no-save playwright` + `npx playwright install chromium`) — not in `package.json`, so a fresh `npm install` (e.g. inside the `frontend` container) won't have it. Reinstall the same way if browser-driven verification is needed again; it's what proved the multiple_choice exercise actually renders/scores correctly in a real browser, not just via curl.
- `@AuthenticationPrincipal` in controllers resolves to a Spring `Jwt`, not a raw `UUID` — always `UUID.fromString(jwt.getSubject())`. Confirmed while building `ExerciseController`.
- Backend package convention: entities in `<feature>/model/`, everything else in `<feature>/{controller,service,repository,dto,mapper}/`. `poker/` is the one package designed differently — `poker/model/` holds framework-free value objects (`Card`/`HoleCards`/`Board`, no JPA), while `poker/scenario/` colocates its entity + repository in one folder rather than splitting across `model/`/`repository/`. This is now settled (Phase 5 built it this way), not just a note to revisit.
- `mvn test` (no args) will show one failure — `BackendApplicationTests.contextLoads`, a pre-existing `@SpringBootTest` that needs a live Postgres connection matching `docker-compose.yml`'s credentials. It's not reachable without `docker compose up` first and isn't related to whatever you just changed; scope to specific test classes (`-Dtest=FooTest,BarTest`) for a quick check, or run the full backend verification against the live stack instead (curl + `psql` + Playwright, as this file's Phase sections do throughout).
- `./mvnw` occasionally fails with a `maven-surefire-plugin ... Unable to load the mojo` classpath error when run with `-o` (offline) right after a cold start — drop `-o` and let it re-resolve once; it then works offline again for the rest of the session.
- JPA enum columns are persisted as the exact Java constant name (`@Enumerated(EnumType.STRING)` default) — seed data must use matching uppercase values.
- Frontend pattern for routed detail views: `toSignal(route.paramMap.pipe(switchMap(id => service.get(id))))` — used identically in `course-detail`, `module-detail`; reuse for the exercise runner too.
- No entity is ever returned directly from a controller anywhere in the codebase — DTOs only, MapStruct for the mapping. Keep this up in `exercise/`.
- The `angular-frontend-reviewer`/`spring-backend-reviewer`/`api-contract-checker`/`poker-logic-reviewer` subagents (in `.claude/agents/`) caught real bugs every time they were used this session (publish-chain leak, `@MapsId` misuse, missing 401 retry). They're being skipped for now to save tokens, but worth reconsidering for anything involving grading/equity math correctness specifically, where a subtle bug is easy to miss by eye.
