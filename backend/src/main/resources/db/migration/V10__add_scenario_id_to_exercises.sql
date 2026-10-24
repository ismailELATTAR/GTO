-- exercises.scenario_id was deliberately left out in V4 (poker_scenarios
-- didn't exist yet — see that migration's comment). Phase 5 adds it now
-- that the table does.
ALTER TABLE exercises ADD COLUMN scenario_id UUID REFERENCES poker_scenarios (id);
