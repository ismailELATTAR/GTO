-- Two `numerical` (pot-odds) exercises. Both are the standard "equity
-- needed to call profitably" calculation: bet / (pot + bet + bet), i.e.
-- call amount over the total pot size after you call.

INSERT INTO topics (id, slug, name, description) VALUES
    ('e0000000-0000-4000-8000-000000000003', 'math.pot-odds', 'Pot Odds',
     'Calculating the equity needed to profitably continue against a bet.');

-- Pot 100 BB, villain bets 50 BB -> call 50 into a 200 BB pot -> 25%.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published,
                        correct_answer, answer_tolerance, answer_unit) VALUES
    ('e0000000-0000-4000-8000-000000000201', 'e0000000-0000-4000-8000-000000000003',
     'NUMERICAL', 'BEGINNER',
     'The pot is 100 BB. Villain bets 50 BB. What equity do you need to call profitably?',
     'Pot odds = the amount you must call, divided by the total pot after you call. Here that''s 50 / (100 + 50 + 50) = 50 / 200 = 25%. Any equity above that makes calling +EV.',
     10, true, 25.00, 2.00, '%');

-- Pot 40 BB, villain bets 40 BB (pot-sized) -> call 40 into a 120 BB pot -> 33.33%.
INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published,
                        correct_answer, answer_tolerance, answer_unit) VALUES
    ('e0000000-0000-4000-8000-000000000202', 'e0000000-0000-4000-8000-000000000003',
     'NUMERICAL', 'INTERMEDIATE',
     'The pot is 40 BB. Villain makes a pot-sized bet of 40 BB. What equity do you need to call profitably?',
     'Pot odds = 40 / (40 + 40 + 40) = 40 / 120 ≈ 33.3%. A pot-sized bet always asks for exactly 33.3% equity to call, regardless of the actual stack sizes — worth memorizing as a landmark number.',
     15, true, 33.33, 2.00, '%');
