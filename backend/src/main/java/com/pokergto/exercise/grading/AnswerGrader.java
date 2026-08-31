package com.pokergto.exercise.grading;

import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseType;

/**
 * Strategy interface for scoring one attempt. `ExerciseService` collects all
 * beans of this type into a `Map<ExerciseType, AnswerGrader>` keyed by
 * {@link #supportedType()} — adding a new exercise type means adding a new
 * implementation here, no changes to the service or controller.
 */
public interface AnswerGrader {

  ExerciseType supportedType();

  GradeResult grade(Exercise exercise, AttemptRequestDto request);
}
