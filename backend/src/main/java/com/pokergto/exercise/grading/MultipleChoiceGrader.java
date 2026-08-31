package com.pokergto.exercise.grading;

import com.pokergto.common.exception.ApiException;
import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseOption;
import com.pokergto.exercise.model.ExerciseType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * score = the chosen option's `correctFrequency` if the spot is a
 * mixed-frequency one (e.g. "Call 65% / 3-Bet 35%"), otherwise 100 for the
 * single correct option and 0 for everything else. This is deliberately NOT
 * binary even for the frequency case — picking the 35%-frequency option
 * scores 35, not 0, because it's still theory-correct, just less often.
 */
@Component
public class MultipleChoiceGrader implements AnswerGrader {

  @Override
  public ExerciseType supportedType() {
    return ExerciseType.MULTIPLE_CHOICE;
  }

  @Override
  public GradeResult grade(Exercise exercise, AttemptRequestDto request) {
    if (request.selectedOptionId() == null) {
      throw ApiException.badRequest("selectedOptionId is required for multiple_choice exercises");
    }

    ExerciseOption selected =
        exercise.getOptions().stream()
            .filter(option -> option.getId().equals(request.selectedOptionId()))
            .findFirst()
            .orElseThrow(() -> ApiException.badRequest("Invalid option for this exercise"));

    BigDecimal score =
        selected.getCorrectFrequency() != null
            ? selected.getCorrectFrequency()
            : (selected.isCorrect() ? BigDecimal.valueOf(100) : BigDecimal.ZERO);

    return new GradeResult(selected.isCorrect(), score, selected);
  }
}
