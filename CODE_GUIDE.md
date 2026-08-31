# Code Guide — how this codebase actually works

You've watched a lot of code get written without reading any of it yet. This
file is the antidote: a guided walkthrough of the real, current code (not a
summary of what happened), written for someone who knows backend concepts
reasonably well but is still learning Angular specifically.

It's organized as a *reading path*, not a reference — read it top to bottom
once, then come back to specific sections later. Every code block below is
copied verbatim from the actual file (path given above each block), so you
can open the file next to this guide and follow along.

---

## 1. The big picture

This is a normal client-server app:

```
Browser (Angular, :4200)  <--HTTP/JSON-->  Backend (Spring Boot, :8080/api)  <--SQL-->  Postgres
                                                    |
                                              Keycloak (:8081) — issues/validates JWTs
```

The Angular app never talks to Postgres or Keycloak directly. It calls the
Spring Boot API; the API calls Postgres and Keycloak. Every authenticated
request from the browser carries a JWT (a signed token) in an
`Authorization: Bearer <token>` header, which Spring Security validates
against Keycloak's public key before any controller method runs.

**Concrete example — what happens when you click "3-Bet" on an exercise:**

1. Angular's `MultipleChoiceExercise` component emits the option id you clicked.
2. `ExerciseRunner` (the parent) calls `ExerciseService.submitAttempt(exerciseId, { selectedOptionId })`, which does an HTTP `POST /api/exercises/{id}/attempt` with your JWT attached.
3. Spring Security checks the JWT is valid, then routes the request to `ExerciseController.submitAttempt(...)`.
4. The controller extracts your user id from the JWT and calls `ExerciseService.submitAttempt(...)`.
5. The service loads the `Exercise` row from Postgres, picks the right `AnswerGrader` for its type, grades your answer, saves an `ExerciseAttempt` row, and returns a DTO.
6. That DTO comes back as JSON, Angular's `HttpClient` parses it, and the component re-renders with the result.

Everything below is explaining the pieces of that flow in more depth, plus
the poker-table pieces that don't have a live flow like this yet (Phase 5 is
still in progress).

---

## 2. Backend: how a feature is organized

The backend is **package-by-feature**, not package-by-layer. That means
`exercise/` (or `curriculum/`, `auth/`, `user/`) is a *domain*, and inside it
you find every layer that domain needs:

```
exercise/
  model/        the JPA entities (map 1:1 to database tables)
  repository/   Spring Data interfaces (query the database)
  grading/      the Strategy pattern for scoring an answer (see §4)
  mapper/       entity <-> DTO conversion (MapStruct, generated at compile time)
  dto/          the shapes that actually cross the HTTP boundary
  service/      business logic — orchestrates repository + grading + mapper
  controller/   HTTP endpoints — thin, just calls the service
```

This scales better than a global `controllers/`, `services/`, `models/`
split once you have several domains — everything about "exercises" lives in
one folder, not scattered across three parallel folder trees.

### Why entities and DTOs are different classes

`Exercise.java` (the entity, in `model/`) has fields like `correctAnswer`
and `answerTolerance` — that's the answer key. `ExerciseDetailDto.java` (in
`dto/`) has no such fields at all — it *can't* leak the answer key, because
the field doesn't exist on the type. This is enforced by the type system,
not by remembering to strip a field before sending a response. No entity is
ever returned directly from a controller anywhere in this codebase.

`ExerciseMapper.java` (in `mapper/`) is the thing that copies fields from
one shape to the other. It's an `interface`, not a class — MapStruct is a
compile-time code generator that reads the interface and writes the actual
copying code for you (into a generated `ExerciseMapperImpl` you never see or
edit). You only write mapping *hints* for fields that can't be copied
automatically (different name, needs a computed value); everything else is
inferred by matching field names.

```java
// backend/src/main/java/com/pokergto/exercise/mapper/ExerciseMapper.java
@Mapper(componentModel = "spring")
public interface ExerciseMapper {
  ExerciseDetailDto toDetail(Exercise exercise);       // strips the answer key just by having a narrower target type
  ExerciseOptionDto toOptionDto(ExerciseOption option); // same idea, for options
  AttemptOptionResultDto toOptionResult(ExerciseOption option); // the *revealing* mapping — used only post-attempt
}
```

---

## 3. Backend deep-dive: the exercise engine

This is the richest example in the codebase, so it's worth walking start to
finish. Four exercise types exist right now (`MULTIPLE_CHOICE`, `NUMERICAL`,
`EQUITY_ESTIMATION` are built; the rest of the 10-value enum has no grader
yet), but the *pattern* is what matters — it's designed so adding a 5th type
never requires touching existing code.

### The Strategy pattern for grading

Every exercise type needs different logic to decide if an answer is right.
Rather than one giant `if (type == MULTIPLE_CHOICE) ... else if (type ==
NUMERICAL) ...` method, there's one interface and one implementation per type:

```java
// backend/src/main/java/com/pokergto/exercise/grading/AnswerGrader.java
public interface AnswerGrader {
  ExerciseType supportedType();
  GradeResult grade(Exercise exercise, AttemptRequestDto request);
}
```

`MultipleChoiceGrader`, `NumericalGrader`, and `EquityEstimationGrader` each
implement this. Spring's dependency injection collects *every* class
annotated `@Component` that implements `AnswerGrader` into a `List<AnswerGrader>`
automatically — you don't wire this list by hand:

```java
// backend/src/main/java/com/pokergto/exercise/service/ExerciseService.java, constructor
public ExerciseService(..., List<AnswerGrader> graders) {
  ...
  this.graders = graders.stream().collect(Collectors.toMap(AnswerGrader::supportedType, Function.identity()));
}
```

That turns the list into a `Map<ExerciseType, AnswerGrader>`. Then grading
an attempt is just a map lookup:

```java
AnswerGrader grader = graders.get(exercise.getType());
GradeResult result = grader.grade(exercise, request);
```

**To add a 6th exercise type**, you write one new class implementing
`AnswerGrader`, annotate it `@Component`, and nothing else changes — the map
picks it up automatically at startup.

Two of the three graders (`NumericalGrader`, `EquityEstimationGrader`) are
mechanically identical — both grade "a typed number within a tolerance
band" — so they don't duplicate the scoring logic. They both extend an
abstract base class that has the actual algorithm, and only override which
`ExerciseType` they claim:

```java
// backend/src/main/java/com/pokergto/exercise/grading/NumericalGrader.java — the entire file
@Component
public class NumericalGrader extends ToleranceGrader {
  @Override
  public ExerciseType supportedType() {
    return ExerciseType.NUMERICAL;
  }
}
```

The real scoring logic lives once, in `ToleranceGrader.grade(...)`: within
`answerTolerance` of the correct value scores 100, within 2× that scores 50
(a "close, not exact" partial credit), further off scores 0.

### The DTO split, concretely

```java
// backend/src/main/java/com/pokergto/exercise/dto/ExerciseDetailDto.java — pre-answer
public record ExerciseDetailDto(
    UUID id, ExerciseType type, Difficulty difficulty, String prompt,
    int xpReward, List<ExerciseOptionDto> options, String answerUnit) {}
```

```java
// backend/src/main/java/com/pokergto/exercise/dto/AttemptResponseDto.java — post-answer
public record AttemptResponseDto(
    boolean correct, BigDecimal score, String explanation,
    List<AttemptOptionResultDto> options, BigDecimal correctAnswer) {}
```

Notice `ExerciseDetailDto` has no `explanation`, no `correctAnswer`, and its
`options` are `ExerciseOptionDto` (id + label only — no `correct` flag).
`AttemptResponseDto` has all of that, because by the time it's built, you've
already submitted an answer — revealing the key is safe. This is the whole
security model for "don't let the answer leak" — there's no field to
accidentally serialize, because the pre-answer type never had it.

(Aside: `record` is a Java feature — an immutable data class where the
constructor, `equals`/`hashCode`/`toString`, and getters are all generated
from the field list on the first line. `Course`/`Exercise`/etc. in `model/`
are regular classes with Lombok annotations instead, because JPA entities
need mutability and a no-args constructor that `record` doesn't provide.)

### The service ties it together

```java
// backend/src/main/java/com/pokergto/exercise/service/ExerciseService.java, submitAttempt (trimmed)
Exercise exercise = findPublishedExercise(exerciseId);
AnswerGrader grader = graders.get(exercise.getType());
GradeResult result = grader.grade(exercise, request);

ExerciseAttempt attempt = ExerciseAttempt.builder()
    .user(userRepository.getReferenceById(userId))
    .exercise(exercise)
    .correct(result.correct())
    .score(result.score())
    .build();
attemptRepository.save(attempt);

return new AttemptResponseDto(result.correct(), result.score(), exercise.getExplanation(), optionResults, exercise.getCorrectAnswer());
```

`userRepository.getReferenceById(userId)` is worth knowing: it does **not**
run a `SELECT` to fetch the user. It hands JPA a lazy proxy that only
becomes a real query if you call a getter on it — here we only need it to
set the foreign key on `ExerciseAttempt`, so no query is wasted.

### The controller is deliberately thin

```java
// backend/src/main/java/com/pokergto/exercise/controller/ExerciseController.java, submitAttempt
@PostMapping("/{id}/attempt")
public AttemptResponseDto submitAttempt(
    @PathVariable UUID id, @RequestBody AttemptRequestDto request, @AuthenticationPrincipal Jwt jwt) {
  UUID userId = UUID.fromString(jwt.getSubject());
  return exerciseService.submitAttempt(id, userId, request);
}
```

`@AuthenticationPrincipal Jwt jwt` is Spring Security handing you the
already-validated token for the current request. `jwt.getSubject()` is the
`sub` claim — Keycloak's user id, which is also this app's `users.id` (see
`User.java`'s class comment for why those two ids are kept identical). The
controller does no logic of its own — it just translates HTTP into a method
call and a method result back into HTTP. All three endpoints look like this.

---

## 4. Frontend: Angular concepts you'll see everywhere

This project uses Angular's newer style throughout, which looks different
from older Angular tutorials you may have seen. Four things to know before
any component will make sense:

### No NgModules — "standalone" components

Every component declares its own dependencies directly:

```typescript
@Component({
  selector: 'app-exercise-runner',
  imports: [RouterLink, MultipleChoiceExercise, NumericalExercise],  // <- like Java imports, but for template usage
  templateUrl: './exercise-runner.html',
})
export class ExerciseRunner { ... }
```

`imports` lists every other component/directive this component's *template*
uses. There's no central `AppModule` declaring everything — each component
is self-contained. If a template uses `<app-poker-card>` and `PokerCard`
isn't in that component's `imports` array, it's a compile error.

### Signals — the local-state primitive

A **signal** is a reactive container: `signal(false)` creates one holding
`false`; calling it as a function (`mySignal()`) reads the current value;
`.set(newValue)` writes a new one. Angular automatically re-renders any
template that read a signal, whenever that signal changes — no manual
"subscribe" bookkeeping.

```typescript
// frontend/src/app/features/exercise/engine/exercise-runner.ts
protected readonly attempt = signal<AttemptResponse | null>(null);
protected readonly submitting = signal(false);
...
this.submitting.set(true);
this.exerciseService.submitAttempt(exercise.id, request).subscribe({
  next: (result) => {
    this.attempt.set(result);      // template re-renders wherever attempt() is read
    this.submitting.set(false);
  },
});
```

`input()` and `output()` are signal-flavored versions of the old
`@Input()`/`@Output()` decorators — a component declares what it accepts
from its parent and what it emits back:

```typescript
// frontend/src/app/features/exercise/types/multiple-choice/multiple-choice-exercise.ts
readonly exercise = input.required<ExerciseDetail>();  // parent MUST pass this
readonly attempt = input<AttemptResponse | null>(null); // parent may omit; defaults to null
readonly select = output<string>();                     // this component emits option ids upward
```

The parent wires it up in its own template with `[exercise]="ex"` (property
binding, parent → child) and `(select)="onSelect($event)"` (event binding,
child → parent) — see `exercise-runner.html`.

`computed()` is a signal derived from other signals — it recalculates only
when something it reads actually changes:

```typescript
// frontend/src/app/shared/poker/table/table.ts
protected readonly seatPositions = computed(() => {
  const seats = this.seats();  // reading a signal inside computed() creates the dependency
  ...
  return seats.map((seat, i) => { /* ellipse math */ });
});
```

### `inject()` instead of constructor parameters

Old Angular injected dependencies via the constructor. This codebase uses
`inject()` at the field-declaration site instead — same dependency
injection system, different syntax:

```typescript
private readonly route = inject(ActivatedRoute);
private readonly exerciseService = inject(ExerciseService);
```

### The new template control-flow syntax

`@if`, `@for`, `@switch`, `@let` are built into Angular's template compiler
(replacing the old `*ngIf`/`*ngFor` directives):

```html
<!-- frontend/src/app/features/exercise/engine/exercise-runner.html -->
@if (exercise(); as ex) {
  @switch (ex.type) {
    @case ('MULTIPLE_CHOICE') { <app-multiple-choice-exercise ... /> }
    @case ('NUMERICAL')       { <app-numerical-exercise ... /> }
    @default { <p>No renderer yet for exercise type "{{ ex.type }}".</p> }
  }
}
```

Note `exercise()` — signals are read by *calling* them, including right
inside a template. `@for (option of ex.options; track option.id)` is the
loop syntax; the `track` expression is required (tells Angular how to match
old/new items efficiently on re-render, like React's `key`). `@let` declares
a local template variable, e.g. `@let picked = pickedOptionId() === option.id;`.

### `toSignal` — bridging HTTP calls (Observables) into signals

`HttpClient` methods return RxJS `Observable`s, not signals. `toSignal`
converts one into the other so a template can just read it like any other
signal, without a manual `.subscribe()`:

```typescript
protected readonly exercise = toSignal(
  this.route.paramMap.pipe(switchMap((params) => this.exerciseService.getExercise(params.get('id')!))),
);
```

Read right-to-left: `route.paramMap` is an Observable of the current route's
URL params; `switchMap` says "whenever the id param changes, cancel any
in-flight request and fetch the new exercise instead"; `toSignal` exposes
the latest emitted value as `exercise()`. This exact pattern (`toSignal` +
`switchMap` over `route.paramMap`) is reused in every routed detail view
in the app — `course-detail`, `module-detail`, `lesson`, `exercise-runner`.

---

## 5. Frontend deep-dive: the exercise dispatcher

This mirrors the backend's Strategy pattern almost exactly, just implemented
with components instead of classes.

`ExerciseRunner` (`features/exercise/engine/exercise-runner.ts`) is the
**dispatcher**. It:
1. Fetches the exercise by route id (via `toSignal`, above).
2. Owns the one shared piece of mutable state (`attempt`, `submitting`) and the one HTTP call for submitting an answer.
3. Looks at `exercise().type` and renders the matching **typed renderer** component.

```typescript
private submit(request: AttemptRequest): void {
  const exercise = this.exercise();
  if (!exercise || this.submitting()) return;
  this.submitting.set(true);
  this.exerciseService.submitAttempt(exercise.id, request).subscribe({
    next: (result) => { this.attempt.set(result); this.submitting.set(false); },
    error: () => { this.submitting.set(false); },
  });
}
```

Both `MultipleChoiceExercise` and `NumericalExercise` call this same method
via different public methods (`onSelect`/`onNumericAnswer`) that just build a
different-shaped `AttemptRequest`. Neither typed component makes an HTTP
call itself — they're purely presentational: given `exercise` + `attempt` +
`submitting` as inputs, render the right UI, and emit what the user did.

**Why split it this way?** `ExerciseRunner` doesn't need to know *how*
`multiple_choice` renders, and `MultipleChoiceExercise` doesn't need to know
*how* attempts get submitted over HTTP. Adding exercise type #4 means: one
new component in `features/exercise/types/`, one new `@case` in
`exercise-runner.html`. Nothing about `ExerciseRunner`'s logic changes —
same shape of change as adding a new `AnswerGrader` on the backend.

### Reading the "answer revealed" state

`MultipleChoiceExercise` is worth a close read because of how it handles
mixed-frequency answers (§30 of ARCHITECTURE.md — an option can be
"correct" at, say, 65% frequency, with a different option also valid at
35%). The interesting method:

```typescript
// frontend/src/app/features/exercise/types/multiple-choice/multiple-choice-exercise.ts
protected optionTone(optionId: string): 'mixed' | 'correct' | 'wrong' | 'muted' | null {
  const result = this.optionResult(optionId);
  if (!result) return null;
  if (result.correctFrequency !== null) return 'mixed';   // a real branch of a mixed strategy
  if (result.correct) return 'correct';                    // the single flagged-correct option
  return this.pickedOptionId() === optionId ? 'wrong' : 'muted'; // only "wrong" if YOU picked it
}
```

This is why picking the 35%-frequency option on the AQs exercise shows gold
("still theory-correct, just less favored"), not red — the template colors
by this four-way `tone`, not by a flat `correct`/`incorrect` boolean.

---

## 6. Phase 5: the poker table components

These two are simpler than the exercise engine — no HTTP, no backend
counterpart yet, just presentational rendering from whatever data they're
given.

`PokerCard` (`shared/poker/card/card.ts` + `.html`) renders one card as SVG.
The interesting bit is *why* SVG with a `viewBox` instead of a fixed-size
image or a plain styled `<div>`: a `viewBox="0 0 100 140"` defines an
internal coordinate system, and the actual rendered size is whatever CSS box
the SVG sits in (`class="h-full w-full"`) — so the same markup looks correct
whether it's rendered at 40px or 400px wide, no separate "small card" /
"big card" variants needed.

`PokerTableComponent` (`shared/poker/table/table.ts` + `.html`) is built
*on* `PokerCard` (it lists `PokerCard` in its own `imports`). The one piece
of real logic is computing where each seat sits around the table for an
*arbitrary* number of players, rather than hardcoding "here's where 6 seats
go, here's where 9 seats go":

```typescript
// frontend/src/app/shared/poker/table/table.ts
protected readonly seatPositions = computed(() => {
  const seats = this.seats();
  const n = seats.length;
  return seats.map((seat, i) => {
    const angle = Math.PI / 2 + (i * (2 * Math.PI)) / n;  // spread n seats evenly around a circle
    const xPct = 50 + 44 * Math.cos(angle);                // parametric ellipse: x = center + radius*cos(angle)
    const yPct = 50 + 42 * Math.sin(angle);
    return { seat, xPct, yPct };
  });
});
```

Seat 0 always lands at `angle = 90°`, which (because SVG/CSS y-coordinates
increase *downward*, unlike a math textbook) is the bottom of the ellipse —
that's why whichever seat you put first in the `seats` array (conventionally
"Hero") always renders at the bottom, matching every real poker client. The
rest of the seats increase `i`, which walks the angle clockwise from there.
This is genuinely just trigonometry, not an Angular-specific idea — the
Angular-specific part is only that it's wrapped in `computed()` so it
recalculates automatically if `seats` ever changes.

You can see both of these live at `/poker-demo` (behind login) — see
`features/poker-demo/poker-demo.ts` for the example data that page renders.

---

## 7. Suggested reading order, if you want to go file-by-file

1. `backend/.../curriculum/` — the simplest full vertical slice (read-only, no grading, no auth complexity beyond "must be logged in"). Read `CurriculumController` → `CurriculumService` → `CurriculumMapper` → `Course`/`Lesson` in that order.
2. `frontend/.../core/api/curriculum.service.ts` next to it — see how the TS interfaces are hand-written to mirror the Java DTOs field-for-field.
3. `frontend/.../features/curriculum/module-detail/` — the `toSignal` + `switchMap` routed-detail pattern, in its smallest form.
4. Then the exercise engine, backend first (`exercise/grading/AnswerGrader.java` → `MultipleChoiceGrader.java` → `ExerciseService.java` → `ExerciseController.java`), then frontend (`exercise.service.ts` → `exercise-runner.ts`/`.html` → `multiple-choice-exercise.ts`/`.html`).
5. `shared/poker/card/` and `shared/poker/table/` last — smallest scope, but assumes you already know signals/`computed`/`input` from step 4.

`ARCHITECTURE.md` has the full schema/API/phase reference if you want the
"why this shape" design rationale behind any of it. `PROGRESS.md` has the
session-by-session history, including every real bug that got found and
fixed along the way — worth skimming if you're curious what actually went
wrong during development, not just what the final code looks like.
