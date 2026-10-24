import { Component, input, output } from '@angular/core';
import type { AttemptResponse, ExerciseDetail } from '../../../../core/api/exercise.service';
import { OptionPicker } from '../../shared/option-picker/option-picker';

/**
 * Renders a `MULTIPLE_CHOICE` exercise: the prompt, then the shared
 * `OptionPicker` (button list + post-answer score banner — same component
 * `StreetDecisionExercise` uses, since both are graded by `OptionGrader` on
 * the backend). Purely presentational — doesn't call the API itself, just
 * forwards `OptionPicker`'s `select` event to `ExerciseRunner` (the dispatcher).
 */
@Component({
  selector: 'app-multiple-choice-exercise',
  imports: [OptionPicker],
  templateUrl: './multiple-choice-exercise.html',
})
export class MultipleChoiceExercise {
  readonly exercise = input.required<ExerciseDetail>();
  readonly attempt = input<AttemptResponse | null>(null);
  readonly submitting = input(false);

  readonly select = output<string>();
}
