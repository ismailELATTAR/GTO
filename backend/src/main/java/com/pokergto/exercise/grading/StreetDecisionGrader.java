package com.pokergto.exercise.grading;

import com.pokergto.exercise.model.ExerciseType;
import org.springframework.stereotype.Component;

/**
 * Same mechanics as {@code multiple_choice} (pick from {@code
 * exercise_options}) but rendered on an actual table (`scenario` is set) —
 * see {@link OptionGrader} for the scoring logic and {@code
 * PokerScenarioDto} for the hand data the frontend renders.
 */
@Component
public class StreetDecisionGrader extends OptionGrader {

  @Override
  public ExerciseType supportedType() {
    return ExerciseType.STREET_DECISION;
  }
}
