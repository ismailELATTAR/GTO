package com.pokergto.exercise.grading;

import com.pokergto.common.exception.ApiException;
import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.model.Exercise;
import java.math.BigDecimal;

/**
 * Shared grading logic for any exercise type that's answered with a free-typed
 * number scored against `exercise.correctAnswer` within a tolerance band,
 * rather than picking from options — `numerical` (pot odds) and
 * `equity_estimation` both work this way and would otherwise duplicate the
 * exact same three-tier scoring. Subclasses only need to declare which
 * {@code ExerciseType} they handle.
 *
 * <p>Three tiers, in the same "reward being close, not just exact" spirit as
 * multiple_choice's frequency-based partial credit:
 *
 * <ul>
 *   <li>within {@code answerTolerance} of the correct value → 100 (tight/exact)
 *   <li>within 2x {@code answerTolerance} → 50 (close, partial credit)
 *   <li>further off → 0
 * </ul>
 */
public abstract class ToleranceGrader implements AnswerGrader {

  private static final BigDecimal FULL_CREDIT_SCORE = BigDecimal.valueOf(100);
  private static final BigDecimal PARTIAL_CREDIT_SCORE = BigDecimal.valueOf(50);
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  @Override
  public GradeResult grade(Exercise exercise, AttemptRequestDto request) {
    if (request.numericAnswer() == null) {
      throw ApiException.badRequest("numericAnswer is required for this exercise type");
    }

    BigDecimal diff = request.numericAnswer().subtract(exercise.getCorrectAnswer()).abs();
    BigDecimal tolerance = exercise.getAnswerTolerance();

    BigDecimal score;
    if (diff.compareTo(tolerance) <= 0) {
      score = FULL_CREDIT_SCORE;
    } else if (diff.compareTo(tolerance.multiply(BigDecimal.valueOf(2))) <= 0) {
      score = PARTIAL_CREDIT_SCORE;
    } else {
      score = ZERO;
    }

    // "correct" tracks full credit only — a close-but-outside-tolerance
    // guess still earns 50 points but isn't flagged correct, same as how
    // multiple_choice's `correct` means "the single flagged option," not
    // "earned any credit at all."
    boolean correct = score.compareTo(FULL_CREDIT_SCORE) == 0;
    return new GradeResult(correct, score, null);
  }
}
