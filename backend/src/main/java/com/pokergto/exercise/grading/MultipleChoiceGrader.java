package com.pokergto.exercise.grading;

import com.pokergto.exercise.model.ExerciseType;
import org.springframework.stereotype.Component;

/** Prose-described spot, answer picked from option buttons. See {@link OptionGrader} for the actual scoring logic. */
@Component
public class MultipleChoiceGrader extends OptionGrader {

  @Override
  public ExerciseType supportedType() {
    return ExerciseType.MULTIPLE_CHOICE;
  }
}
