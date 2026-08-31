-- Taxonomy of poker concepts (e.g. "preflop.rfi", "postflop.cbet"). Lessons
-- and (later) exercises both tag against this, which is what lets progress
-- tracking answer "what topic is this user weakest at" across both.
CREATE TABLE topics (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    description TEXT
);

CREATE TABLE courses (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(100) NOT NULL UNIQUE,
    title        VARCHAR(150) NOT NULL,
    description  TEXT,
    order_index  INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE modules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id    UUID NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    title        VARCHAR(150) NOT NULL,
    description  TEXT,
    order_index  INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE lessons (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id    UUID NOT NULL REFERENCES modules (id) ON DELETE CASCADE,
    title        VARCHAR(150) NOT NULL,
    content      TEXT NOT NULL, -- markdown, rendered client-side
    order_index  INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE lesson_topics (
    lesson_id UUID NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    topic_id  UUID NOT NULL REFERENCES topics (id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, topic_id)
);

CREATE INDEX idx_modules_course_order ON modules (course_id, order_index);
CREATE INDEX idx_lessons_module_order ON lessons (module_id, order_index);
