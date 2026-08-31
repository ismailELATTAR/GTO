-- Support for the `numerical` exercise type (pot-odds calculations per
-- ARCHITECTURE.md §36). `exercise_options` is multiple_choice-specific — a
-- numerical exercise instead has one right numeric value plus a tolerance
-- band, so it needs its own columns on `exercises` directly. All three are
-- NULL for every other exercise type.
ALTER TABLE exercises
    ADD COLUMN correct_answer   NUMERIC(10,2),
    ADD COLUMN answer_tolerance NUMERIC(10,2), -- full credit within this band; see NumericalGrader for the partial-credit taper
    ADD COLUMN answer_unit      VARCHAR(10);   -- e.g. '%', 'BB' — display-only, shown before AND after answering (not the answer key itself)
