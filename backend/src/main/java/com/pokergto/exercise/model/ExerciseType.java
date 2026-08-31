package com.pokergto.exercise.model;

/**
 * The full set from ARCHITECTURE.md §13 — only MULTIPLE_CHOICE has a
 * grader/renderer implemented so far (Phase 4 MVP slice). The rest exist
 * here so the schema/DTOs don't need to change shape when each is added;
 * see the `new-exercise-type` skill for that process.
 */
public enum ExerciseType {
  MULTIPLE_CHOICE,
  NUMERICAL,
  RANGE_SELECTION,
  RANGE_CONSTRUCTION,
  HAND_READING,
  EQUITY_ESTIMATION,
  POT_ODDS,
  EV_CALCULATION,
  STREET_DECISION,
  FULL_HAND,
}
