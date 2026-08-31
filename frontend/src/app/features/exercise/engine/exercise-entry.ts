import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Difficulty, ExerciseService, ExerciseType } from '../../../core/api/exercise.service';

interface Choice<T> {
  readonly label: string;
  readonly value: T | null; // null = "Any"
}

const DIFFICULTY_CHOICES: Choice<Difficulty>[] = [
  { label: 'Any', value: null },
  { label: 'Beginner', value: 'BEGINNER' },
  { label: 'Intermediate', value: 'INTERMEDIATE' },
  { label: 'Advanced', value: 'ADVANCED' },
  { label: 'Expert', value: 'EXPERT' },
];

// Only the types with a real frontend renderer so far — offering a type
// with no renderer would just land the user on ExerciseRunner's
// "No renderer yet" fallback. Extend this list alongside `exercise-runner.html`'s
// @switch as new types get built (see the `new-exercise-type` skill).
const TYPE_CHOICES: Choice<ExerciseType>[] = [
  { label: 'Any', value: null },
  { label: 'Multiple Choice', value: 'MULTIPLE_CHOICE' },
  { label: 'Pot Odds', value: 'NUMERICAL' },
  { label: 'Equity Estimation', value: 'EQUITY_ESTIMATION' },
];

/**
 * The `/practice` entry point — lets the user narrow what they're about to
 * practice (difficulty, exercise type) before calling `GET /api/exercises/next`
 * and redirecting into it. Both filters default to "Any", so leaving
 * everything untouched behaves exactly like the old no-picker flow.
 */
@Component({
  selector: 'app-exercise-entry',
  imports: [RouterLink],
  templateUrl: './exercise-entry.html',
})
export class ExerciseEntry {
  private readonly exerciseService = inject(ExerciseService);
  private readonly router = inject(Router);

  protected readonly difficultyChoices = DIFFICULTY_CHOICES;
  protected readonly typeChoices = TYPE_CHOICES;

  protected readonly selectedDifficulty = signal<Difficulty | null>(null);
  protected readonly selectedType = signal<ExerciseType | null>(null);
  protected readonly loading = signal(false);
  protected readonly notFound = signal(false);

  protected start(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.exerciseService
      .getNext(undefined, this.selectedDifficulty() ?? undefined, this.selectedType() ?? undefined)
      .subscribe({
        next: (exercise) => this.router.navigate(['/exercise', exercise.id], { replaceUrl: true }),
        error: () => {
          // 404 = no published exercise matches this filter combination —
          // a real, expected outcome with a small seed set, not a bug.
          this.loading.set(false);
          this.notFound.set(true);
        },
      });
  }
}
