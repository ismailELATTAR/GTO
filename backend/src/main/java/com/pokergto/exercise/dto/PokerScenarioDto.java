package com.pokergto.exercise.dto;

import java.math.BigDecimal;

/**
 * The hand a `street_decision` exercise renders on a table — safe to show
 * in full pre-answer (unlike `exercise_options`/`correctAnswer`, a scenario
 * carries no answer-key data of its own, just the scene).
 *
 * <p>`actionHistory` is passed through as a raw JSON string rather than a
 * typed list — nothing on the backend needs to inspect individual actions,
 * it's only ever handed to the frontend to render. The frontend `JSON.parse`s it.
 */
public record PokerScenarioDto(
    int numPlayers,
    String heroPosition,
    String villainPosition,
    BigDecimal effectiveStackBb,
    BigDecimal potBb,
    String actionHistory,
    String heroCards,
    String boardCards) {}
