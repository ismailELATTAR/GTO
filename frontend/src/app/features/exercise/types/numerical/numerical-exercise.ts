import { Component, input, output, signal } from '@angular/core';
import type { AttemptResponse, ExerciseDetail } from '../../../../core/api/exercise.service';

/**
 * Renders a `NUMERICAL` exercise (pot-odds calculations): a free-typed
 * number instead of picking from options, graded by how close it is to
 * `correctAnswer` rather than by matching an id. Same split of
 * responsibility as `MultipleChoiceExercise` — purely presentational,
 * `ExerciseRunner` owns the HTTP call and the `attempt` result state.
 */
@Component({
  selector: 'app-numerical-exercise',
  templateUrl: './numerical-exercise.html',
})
export class NumericalExercise {
  readonly exercise = input.required<ExerciseDetail>();
  readonly attempt = input<AttemptResponse | null>(null);
  readonly submitting = input(false);

  readonly submitAnswer = output<number>();

  // Kept as text (not a number signal) so an empty/in-progress field like
  // "-" or "12." doesn't get silently coerced to 0 or NaN while typing.
  protected readonly answerText = signal('');

  protected onInput(event: Event): void {
    this.answerText.set((event.target as HTMLInputElement).value);
  }

  protected get canSubmit(): boolean {
    return this.answerText().trim() !== '' && !Number.isNaN(Number(this.answerText()));
  }

  protected submit(): void {
    if (this.attempt() || this.submitting() || !this.canSubmit) return;
    this.submitAnswer.emit(Number(this.answerText()));
  }

  // Mirrors NumericalGrader's three tiers (100 / 50 / 0) so the banner
  // reads "close, but not quite" rather than a flat right/wrong for a
  // 50-point partial-credit answer.
  protected resultTone(): 'correct' | 'close' | 'wrong' | null {
    const result = this.attempt();
    if (!result) return null;
    if (result.score >= 100) return 'correct';
    if (result.score > 0) return 'close';
    return 'wrong';
  }
}
