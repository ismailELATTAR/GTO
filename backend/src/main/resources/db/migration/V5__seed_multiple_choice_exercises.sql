-- Minimal demo set (per the earlier "minimal demo set" MVP decision) — two
-- multiple_choice exercises, chosen to exercise both grading paths:
-- one mixed-frequency (partial credit), one binary (clean correct/incorrect).

INSERT INTO topics (id, slug, name, description) VALUES
    ('e0000000-0000-4000-8000-000000000001', 'preflop.facing-opens', 'Facing Opens',
     'Deciding whether to fold, call, or 3-bet against an opening raise.'),
    ('e0000000-0000-4000-8000-000000000002', 'preflop.blind-defense', 'Blind Defense',
     'Deciding which hands are worth defending from the blinds, and which to give up.');

-- Exercise 1: the spec's own worked example — AQs on the BTN facing a CO open.
-- Mixed frequency: both calling and 3-betting are theory-correct, at different rates.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published) VALUES
    ('e0000000-0000-4000-8000-000000000101', 'e0000000-0000-4000-8000-000000000001',
     'MULTIPLE_CHOICE', 'INTERMEDIATE',
     '9-handed NLHE, 100 BB effective. CO opens to 2.5 BB. You''re on the BTN with A♠Q♠. What''s your action?',
     'AQs is commonly mixed between calling and 3-betting here. You''re in position against a wide CO opening range, so calling keeps the pot smaller with strong playability postflop. 3-betting also gains value by applying pressure and denying equity to CO''s weaker opens. Folding gives up far too much equity against a wide range to ever be correct in this spot.',
     15, true);

INSERT INTO exercise_options (id, exercise_id, label, is_correct, correct_frequency, sort_order) VALUES
    ('e0000000-0000-4000-8000-000000000111', 'e0000000-0000-4000-8000-000000000101', 'Fold', false, NULL, 0),
    ('e0000000-0000-4000-8000-000000000112', 'e0000000-0000-4000-8000-000000000101', 'Call', true, 65.00, 1),
    ('e0000000-0000-4000-8000-000000000113', 'e0000000-0000-4000-8000-000000000101', '3-Bet', false, 35.00, 2);

-- Exercise 2: a clean binary spot — no correct_frequency, one right answer.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published) VALUES
    ('e0000000-0000-4000-8000-000000000102', 'e0000000-0000-4000-8000-000000000002',
     'MULTIPLE_CHOICE', 'BEGINNER',
     '9-handed NLHE, 100 BB effective. UTG opens to 2.5 BB. You''re in the BB with 7♦2♣. What''s your action?',
     '7-2 offsuit is the weakest starting hand in hold''em and has no business continuing against an early-position range, which is tight and strong by definition. Folding costs nothing here — defending this hand only bleeds chips over time.',
     10, true);

INSERT INTO exercise_options (id, exercise_id, label, is_correct, correct_frequency, sort_order) VALUES
    ('e0000000-0000-4000-8000-000000000121', 'e0000000-0000-4000-8000-000000000102', 'Fold', true, NULL, 0),
    ('e0000000-0000-4000-8000-000000000122', 'e0000000-0000-4000-8000-000000000102', 'Call', false, NULL, 1),
    ('e0000000-0000-4000-8000-000000000123', 'e0000000-0000-4000-8000-000000000102', '3-Bet', false, NULL, 2);
