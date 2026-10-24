-- poker_scenarios: structured hand data for exercises that render on a real
-- table (street_decision, and later street_decision-like types) instead of
-- describing the spot in prose (what multiple_choice/numerical do today).
--
-- action_history is JSONB rather than a normalized actions table — it's a
-- variable-length sequence that's always read/displayed wholesale, never
-- queried by individual action, so a join table would add complexity with
-- no query benefit (see ARCHITECTURE.md's "Decisions" section).

CREATE TABLE poker_scenarios (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    num_players        INT NOT NULL,
    hero_position      VARCHAR(10) NOT NULL,
    villain_position   VARCHAR(10),
    effective_stack_bb NUMERIC(6,2) NOT NULL,
    pot_bb             NUMERIC(6,2) NOT NULL,
    action_history     JSONB NOT NULL DEFAULT '[]'::jsonb,
    hero_cards         VARCHAR(4),  -- e.g. "AsQs" — Card.of()/HoleCards.of()'s notation
    board_cards        VARCHAR(10)  -- e.g. "Kh7d2s" — Board.of()'s notation; NULL/empty = preflop
);
