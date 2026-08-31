import { Component, input, output, signal } from '@angular/core';
import type { AttemptResponse, ExerciseDetail } from '../../../../core/api/exercise.service';

/**
 * Renders a `MULTIPLE_CHOICE` exercise: the prompt + option buttons before
 * an attempt, then (once `attempt` is set) each option's revealed
 * correctness/frequency plus the explanation. Purely presentational — it
 * doesn't call the API itself, just emits which option the user picked and
 * lets `ExerciseRunner` (the dispatcher) own the HTTP call and the result state.
 */
@Component({
  selector: 'app-multiple-choice-exercise',
  templateUrl: './multiple-choice-exercise.html',
})
export class MultipleChoiceExercise {
  readonly exercise = input.required<ExerciseDetail>();
  readonly attempt = input<AttemptResponse | null>(null);
  readonly submitting = input(false);

  readonly select = output<string>();

  // Tracked locally purely to highlight "the option you picked" once the
  // attempt result comes back — the API response itself doesn't echo it
  // back, only the per-option correctness/frequency.
  protected readonly pickedOptionId = signal<string | null>(null);

  protected selectOption(optionId: string): void {
    if (this.attempt() || this.submitting()) return;
    this.pickedOptionId.set(optionId);
    this.select.emit(optionId);
  }

  // Frequency-based partial credit means the option the user picked isn't
  // necessarily the flagged-`correct` one (e.g. picking the 35%-frequency
  // 3-Bet on a mixed spot) — highlighting needs the freq/correct combo per
  // option, not just a single "right answer" id.
  protected optionResult(optionId: string) {
    return this.attempt()?.options.find((o) => o.id === optionId) ?? null;
  }

  // How one option should read once the answer's revealed. `correctFrequency
  // !== null` (not a truthy check — a theoretical 0% option is still a real
  // mixed-strategy branch, not "no frequency") means it's a valid branch of
  // a mixed spot, regardless of the flagged single `correct` option. Anything
  // else that isn't `correct` only reads as "wrong" if it's the one the user
  // actually picked — an unpicked wrong option should just fade, not alarm.
  protected optionTone(optionId: string): 'mixed' | 'correct' | 'wrong' | 'muted' | null {
    const result = this.optionResult(optionId);
    if (!result) return null;
    if (result.correctFrequency !== null) return 'mixed';
    if (result.correct) return 'correct';
    return this.pickedOptionId() === optionId ? 'wrong' : 'muted';
  }

  // Same read applied to whichever option was actually picked — what the
  // "Score: X%" banner colors by. Deliberately NOT `score >= 50`: picking
  // the 35%-frequency side of a mixed spot is still theory-correct, just
  // less favored, and coloring that red would read as "you got this wrong."
  protected resultTone(): 'mixed' | 'correct' | 'wrong' | null {
    const picked = this.pickedOptionId();
    if (!picked) return null;
    const tone = this.optionTone(picked);
    return tone === 'muted' ? null : tone;
  }
}
