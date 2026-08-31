package com.pokergto.exercise.grading;

import com.pokergto.exercise.model.ExerciseType;
import org.springframework.stereotype.Component;

/** Pot-odds calculations. See {@link ToleranceGrader} for the actual scoring logic. */
@Component
public class NumericalGrader extends ToleranceGrader {

  @Override
  public ExerciseType supportedType() {
    return ExerciseType.NUMERICAL;
  }
}
