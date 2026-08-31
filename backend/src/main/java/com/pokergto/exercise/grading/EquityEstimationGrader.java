package com.pokergto.exercise.grading;

import com.pokergto.exercise.model.ExerciseType;
import org.springframework.stereotype.Component;

/**
 * Basic equity estimation (e.g. "9 outs with one card to come — roughly what's
 * your equity?", answered via the rule of 4 and 2). See {@link ToleranceGrader}
 * for the actual scoring logic — mechanically identical to `numerical`, just a
 * distinct exercise type so future equity-specific nuance (e.g. weighting by
 * how many outs were involved) has somewhere to live without disturbing pot-odds grading.
 */
@Component
public class EquityEstimationGrader extends ToleranceGrader {

  @Override
  public ExerciseType supportedType() {
    return ExerciseType.EQUITY_ESTIMATION;
  }
}
