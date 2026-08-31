-- Two `equity_estimation` exercises using the "rule of 4 and 2" heuristic —
-- outs x 4 (two cards to come) or outs x 2 (one card to come) approximates
-- equity to hit. No board/card visuals needed (that's Phase 5); the outs
-- count is given directly in the prompt text, same as multiple_choice's
-- hole cards are spelled out as text (e.g. "A♠Q♠").
--
-- Reuses `exercises.correct_answer`/`answer_tolerance`/`answer_unit` — the
-- same columns `numerical` exercises use (see V6) — no schema change needed.

INSERT INTO topics (id, slug, name, description) VALUES
    ('e0000000-0000-4000-8000-000000000004', 'equity.outs', 'Equity & Outs',
     'Estimating hand equity from an outs count using the rule of 4 and 2.');

-- Flop, two cards to come, flush draw (9 outs) -> rule of 4: 9 x 4 = 36%.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published,
                        correct_answer, answer_tolerance, answer_unit) VALUES
    ('e0000000-0000-4000-8000-000000000301', 'e0000000-0000-4000-8000-000000000004',
     'EQUITY_ESTIMATION', 'BEGINNER',
     'You flop a flush draw (9 outs) with two cards to come. Using the rule of 4, roughly what''s your equity to hit by the river?',
     'The rule of 4 and 2: with two cards still to come, multiply your outs by 4 for an approximate equity percentage. 9 outs x 4 = 36%. (The precise figure is closer to 35%, which is why this is an estimate, not exact math.)',
     10, true, 36.00, 3.00, '%');

-- Turn, one card to come, open-ended straight draw (8 outs) -> rule of 2: 8 x 2 = 16%.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published,
                        correct_answer, answer_tolerance, answer_unit) VALUES
    ('e0000000-0000-4000-8000-000000000302', 'e0000000-0000-4000-8000-000000000004',
     'EQUITY_ESTIMATION', 'INTERMEDIATE',
     'Turn card is out and you have an open-ended straight draw (8 outs) with one card to come. Using the rule of 2, roughly what''s your equity to hit on the river?',
     'With only one card left to come, multiply your outs by 2 instead of 4. 8 outs x 2 = 16%, close to the true value (~17.4%).',
     15, true, 16.00, 2.00, '%');
