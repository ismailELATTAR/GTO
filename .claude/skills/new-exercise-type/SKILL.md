---
name: new-exercise-type
description: Scaffold a new poker exercise type end-to-end across the backend (grading strategy, DTO wiring, migration if the type needs new columns/tables) and the frontend (typed renderer component registered in the exercise engine dispatcher). Use when asked to add a new exercise type, a new question format, or a new way for users to practice/answer.
---

# Adding a new exercise type

Per [ARCHITECTURE.md](../../../ARCHITECTURE.md) §2 (Angular Project Structure), exercise types are meant to be added by *extension*, not by editing a monolith: "Adding a new exercise type = new component + registry entry, not a rewrite." Follow that shape. Before starting, ask the user (if not already specified) what the new type actually is and how it's graded — this determines whether it fits the existing schema or needs new columns.

The known exercise types today (from `exercises.type` enum in the schema): `multiple_choice`, `numerical`, `range_selection`, `range_construction`, `hand_reading`, `equity_estimation`, `pot_odds`, `ev_calculation`, `street_decision`, `full_hand`. A genuinely new type gets added to this list; if the request is actually a variant of an existing type (e.g. a different prompt style for `multiple_choice`), it likely doesn't need a new type at all — say so rather than over-engineering.

## Backend (`backend/src/main/java/com/pokergto/exercise/`)

Package-by-feature — everything for the exercise domain lives under `exercise/`, not spread across generic `controllers/`/`services/` packages. Sub-packages: `controller`, `dto`, `engine`, `grading`, `mapper`, `repository`, `service`. (These currently only contain `package-info.java` placeholders — Phase 1 scaffolding — so you may be creating first-of-their-kind classes; look at the sibling `auth` or `user` packages for this project's actual layering/naming/style conventions to match, since they're the ones already implemented.)

1. **Schema** — if the new type needs a new enum value on `exercises.type`, or new columns/tables (e.g. a type-specific answer shape that doesn't fit `exercise_options`/`numeric_answer`/`submitted_range_id`), write a Flyway migration under `backend/src/main/resources/db/migration/` (next `V<n>__description.sql`). Keep it additive — don't drop/rename columns other code still reads.

2. **Grading** — per §9 (Key Architectural Decisions), "Grading is a Strategy pattern (`AnswerGrader` per exercise type) rather than a giant if/else." Add a new implementation of that grader interface (find it under `exercise/grading` once other types exist, or establish it if this is the first) that scores an attempt for the new type. Remember the core teaching philosophy from the "Decisions (confirmed)" section: score is *frequency-based partial credit* (e.g. choosing the 35%-frequency action scores 0.35), not binary correct/incorrect — make sure the new grader follows this unless the type genuinely can't support it (say so explicitly if so).

3. **DTOs/mapper/controller/service** — wire the new type into `ExerciseDto`/`ExerciseAttempt` request-response shapes (`dto`), entity↔DTO mapping (`mapper`), and whatever attempt-submission endpoint exists (`controller`/`service`) so the new type's answer shape round-trips correctly. Cross-check with `api-contract-checker` (if that subagent exists in `.claude/agents/`) once both sides are wired, since this is exactly the kind of change that drifts.

4. **Selection logic** — if the new type has selection quirks (e.g. requires a `scenario_id`, or specific difficulty gating), check `exercise/engine`'s `ExerciseSelectionService` (spaced-repetition/next-exercise logic, per the `engine/` line in §3's Spring Boot package tree) for whether it needs to know about the new type.

## Frontend (`frontend/src/app/features/exercise/`)

1. **New renderer component** under `exercise/types/` (one component per exercise type, per the ARCHITECTURE.md §2 tree) — standalone, signals for local state, per this project's Angular conventions. It receives the typed `ExerciseDto` variant for the new type and emits the user's answer.

2. **Register in the dispatcher** — `exercise/engine/exercise-runner.ts` (`ExerciseRunnerComponent`) dispatches by `exercise.type` via `NgComponentOutlet` or a template `switch`, per §2's "Exercise engine is a dispatcher, not a monolith" decision. Add the new type's case/registry entry there. Do not add type-specific logic into the runner itself — it stays a thin dispatcher.

3. **Reuse shared poker UI** — `shared/poker/*` (card, chip-stack, range-matrix, table) are pure presentational components meant to be reused across lesson viewer, exercise engine, and hand trainer, per §2's key-decisions bullet on `shared/poker/*`. Build the new renderer out of these rather than reimplementing card/range/table UI locally.

4. **Zero hardcoded content** — the new renderer must be a pure renderer of whatever DTO shape the backend sends; no poker content (sample hands, ranges, correct answers) hardcoded in the component, per §1's "content is data, not code" principle.

## After scaffolding

- If Postgres needs a fresh migration applied, restart the backend (`dev-up` skill, or `docker compose restart backend`) — Flyway runs on startup.
- Seed at least one exercise row of the new type (via a Flyway data migration or manual insert) so the type can actually be exercised end-to-end in dev, per the "Decisions (confirmed)" section's "minimal demo set" approach.
- Suggest running `poker-logic-reviewer` if the new type involves equity/range/combinatorics grading, and `angular-frontend-reviewer` / `spring-backend-reviewer` for the respective sides.
