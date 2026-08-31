-- Minimal demo content (per the "minimal demo set" MVP decision) — enough
-- to exercise the curriculum API and lesson viewer end-to-end. Explicit
-- literal UUIDs rather than gen_random_uuid() so the seed rows are stable
-- and referenceable across statements in this same file.

INSERT INTO courses (id, slug, title, description, order_index, is_published) VALUES
    ('c0000000-0000-4000-8000-000000000001', 'fundamentals', 'Poker Fundamentals',
     'Hand rankings, terminology, positions, and the basic math every other module builds on.',
     0, true);

INSERT INTO modules (id, course_id, title, description, order_index, is_published) VALUES
    ('c0000000-0000-4000-8000-000000000101', 'c0000000-0000-4000-8000-000000000001',
     'Getting Started', 'The vocabulary and table layout you need before anything else makes sense.', 0, true);

INSERT INTO topics (id, slug, name, description) VALUES
    ('c0000000-0000-4000-8000-000000000201', 'fundamentals.hand-rankings', 'Hand Rankings',
     'The relative strength of poker hands, high card through royal flush.'),
    ('c0000000-0000-4000-8000-000000000202', 'fundamentals.positions', 'Positions & Blinds',
     'Seat positions relative to the button and their strategic weight.');

INSERT INTO lessons (id, module_id, title, content, order_index, is_published) VALUES
    ('c0000000-0000-4000-8000-000000000301', 'c0000000-0000-4000-8000-000000000101',
     'Hand Rankings',
     E'# Hand rankings\n\nFrom strongest to weakest:\n\n1. Royal flush\n2. Straight flush\n3. Four of a kind\n4. Full house\n5. Flush\n6. Straight\n7. Three of a kind\n8. Two pair\n9. One pair\n10. High card\n\nEvery hand you''ll ever play resolves to one of these ten categories — the rest of this course is about deciding *when* a hand strong or weak enough to act on, not memorizing this list.',
     0, true),
    ('c0000000-0000-4000-8000-000000000302', 'c0000000-0000-4000-8000-000000000101',
     'Positions & Blinds',
     E'# Positions & blinds\n\nAt a 9-handed table, seats are named by how far they act before the flop:\n\n- **UTG** (under the gun) — acts first, tightest range\n- **HJ** (hijack), **CO** (cutoff) — middle-late position\n- **BTN** (button) — acts last every street after preflop, the strongest seat at the table\n- **SB** / **BB** (small/big blind) — post forced bets before cards are dealt, act first postflop\n\nPosition is one of the biggest edges in No-Limit Hold''em: acting last means more information before every decision.',
     1, true);

INSERT INTO lesson_topics (lesson_id, topic_id) VALUES
    ('c0000000-0000-4000-8000-000000000301', 'c0000000-0000-4000-8000-000000000201'),
    ('c0000000-0000-4000-8000-000000000302', 'c0000000-0000-4000-8000-000000000202');
