-- exercises.scenario_id (referencing a future poker_scenarios table) is
-- deliberately NOT included yet — that table doesn't exist until Phase 5
-- needs to render exercises on the visual table. For now, scenario context
-- lives in `prompt` as plain text. Added via a later migration once needed.

CREATE TABLE exercises (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id     UUID NOT NULL REFERENCES topics (id),
    type         VARCHAR(30) NOT NULL,
    difficulty   VARCHAR(20) NOT NULL,
    prompt       TEXT NOT NULL,
    explanation  TEXT NOT NULL,
    xp_reward    INT NOT NULL DEFAULT 10,
    is_published BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exercise_options (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exercise_id       UUID NOT NULL REFERENCES exercises (id) ON DELETE CASCADE,
    label             VARCHAR(100) NOT NULL,
    is_correct        BOOLEAN NOT NULL DEFAULT false,
    correct_frequency NUMERIC(5,2), -- NULL = binary correct/incorrect exercise; set = mixed-frequency (§30)
    sort_order        INT NOT NULL DEFAULT 0
);

CREATE TABLE exercise_attempts (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    exercise_id        UUID NOT NULL REFERENCES exercises (id) ON DELETE CASCADE,
    selected_option_id UUID REFERENCES exercise_options (id),
    numeric_answer     NUMERIC,
    is_correct         BOOLEAN NOT NULL,
    score              NUMERIC(5,2) NOT NULL, -- 0-100, frequency-based partial credit (see "Decisions" in ARCHITECTURE.md)
    response_time_ms   INT,
    attempted_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_exercises_topic_difficulty_published ON exercises (topic_id, difficulty, is_published);
CREATE INDEX idx_exercise_options_exercise ON exercise_options (exercise_id, sort_order);
CREATE INDEX idx_exercise_attempts_user_exercise ON exercise_attempts (user_id, exercise_id, attempted_at);
