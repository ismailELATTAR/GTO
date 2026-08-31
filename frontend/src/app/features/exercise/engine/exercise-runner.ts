import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AttemptRequest, AttemptResponse, ExerciseService } from '../../../core/api/exercise.service';
import { MultipleChoiceExercise } from '../types/multiple-choice/multiple-choice-exercise';
import { NumericalExercise } from '../types/numerical/numerical-exercise';

/**
 * Dispatcher: fetches the exercise, then picks which typed renderer
 * component to show based on `exercise().type`. `MULTIPLE_CHOICE` and
 * `NUMERICAL` have renderers so far — adding a new exercise type means
 * adding a case here and a new component under `features/exercise/types/`,
 * same pattern as the backend's grading strategy (see `new-exercise-type` skill).
 *
 * This component also owns the one HTTP call for submitting an attempt and
 * the resulting `attempt` state — each typed renderer is purely
 * presentational, just emitting the shape of `AttemptRequest` it fills in.
 */
@Component({
  selector: 'app-exercise-runner',
  imports: [RouterLink, MultipleChoiceExercise, NumericalExercise],
  templateUrl: './exercise-runner.html',
})
export class ExerciseRunner {
  private readonly route = inject(ActivatedRoute);
  private readonly exerciseService = inject(ExerciseService);

  protected readonly exercise = toSignal(
    this.route.paramMap.pipe(switchMap((params) => this.exerciseService.getExercise(params.get('id')!))),
  );

  protected readonly attempt = signal<AttemptResponse | null>(null);
  protected readonly submitting = signal(false);

  protected onSelect(optionId: string): void {
    this.submit({ selectedOptionId: optionId });
  }

  protected onNumericAnswer(numericAnswer: number): void {
    this.submit({ numericAnswer });
  }

  private submit(request: AttemptRequest): void {
    const exercise = this.exercise();
    if (!exercise || this.submitting()) return;

    this.submitting.set(true);
    this.exerciseService.submitAttempt(exercise.id, request).subscribe({
      next: (result) => {
        this.attempt.set(result);
        this.submitting.set(false);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }
}
