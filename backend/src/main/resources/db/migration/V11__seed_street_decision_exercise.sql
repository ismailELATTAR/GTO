-- One `street_decision` exercise — mechanically the same grading as
-- multiple_choice (exercise_options, picked via OptionGrader), but this one
-- has a scenario_id so the frontend renders it on an actual table instead
-- of describing the hand in prose.

INSERT INTO poker_scenarios (id, num_players, hero_position, villain_position, effective_stack_bb, pot_bb,
                              action_history, hero_cards, board_cards) VALUES
    ('e0000000-0000-4000-8000-000000000401', 6, 'BB', 'BTN', 100.00, 3.30,
     '[{"position":"BTN","action":"raise","sizeBb":2.3}]'::jsonb,
     'QsJs', NULL);

INSERT INTO exercises (id, topic_id, type, difficulty, prompt, explanation, xp_reward, is_published, scenario_id) VALUES
    ('e0000000-0000-4000-8000-000000000402', 'e0000000-0000-4000-8000-000000000001',
     'STREET_DECISION', 'INTERMEDIATE',
     'BTN opens to 2.3 BB. Action is on you in the BB.',
     'QJs is a classic BB defense candidate: great pot odds (you''re only adding 1.3 BB more to a pot that''s already 3.3 BB), position disadvantage is less costly with a hand that flops well, and 3-betting applies pressure against a wide BTN open while still having strong postflop equity when called. Folding gives up far too much equity to ever be correct here.',
     15, true, 'e0000000-0000-4000-8000-000000000401');

INSERT INTO exercise_options (id, exercise_id, label, is_correct, correct_frequency, sort_order) VALUES
    ('e0000000-0000-4000-8000-000000000411', 'e0000000-0000-4000-8000-000000000402', 'Fold', false, NULL, 0),
    ('e0000000-0000-4000-8000-000000000412', 'e0000000-0000-4000-8000-000000000402', 'Call', true, 70.00, 1),
    ('e0000000-0000-4000-8000-000000000413', 'e0000000-0000-4000-8000-000000000402', '3-Bet', false, 30.00, 2);
