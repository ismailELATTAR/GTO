package com.pokergto.exercise.grading;

import com.pokergto.common.exception.ApiException;
import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseOption;
import java.math.BigDecimal;

/**
 * Shared grading logic for any exercise type answered by picking from
 * {@code exercise_options} — {@code multiple_choice} and {@code
 * street_decision} both work this way and would otherwise duplicate the
 * exact same scoring (same relationship as {@link ToleranceGrader} to
 * {@code numerical}/{@code equity_estimation}). The two types differ only
 * in how the frontend renders the question — prose vs. an actual table —
 * not in how an answer gets scored once picked.
 *
 * <p>score = the chosen option's {@code correctFrequency} if the spot is a
 * mixed-frequency one (e.g. "Call 65% / 3-Bet 35%"), otherwise 100 for the
 * single correct option and 0 for everything else. Deliberately NOT binary
 * even for the frequency case — picking the 35%-frequency option scores 35,
 * not 0, because it's still theory-correct, just less often.
 */
public abstract class OptionGrader implements AnswerGrader {

  @Override
  public GradeResult grade(Exercise exercise, AttemptRequestDto request) {
    if (request.selectedOptionId() == null) {
      throw ApiException.badRequest("selectedOptionId is required for this exercise type");
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
