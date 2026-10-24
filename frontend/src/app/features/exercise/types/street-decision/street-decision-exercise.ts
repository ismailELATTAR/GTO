import { Component, computed, input, output } from '@angular/core';
import type { AttemptResponse, ExerciseDetail, PokerScenarioAction } from '../../../../core/api/exercise.service';
import type { Rank, Suit } from '../../../../shared/poker/card/card';
import { PokerTableComponent, TableCard, TableSeat } from '../../../../shared/poker/table/table';
import { OptionPicker } from '../../shared/option-picker/option-picker';

/** Splits a card-notation string ("Kh7d2s") into 2-char chunks and reads each as {rank, suit} — same notation `Card.toString()` emits on the backend. */
function parseCards(notation: string | null): TableCard[] {
  if (!notation) return [];
  const cards: TableCard[] = [];
  for (let i = 0; i < notation.length; i += 2) {
    cards.push({ rank: notation[i] as Rank, suit: notation[i + 1] as Suit });
  }
  return cards;
}

/**
 * Renders a `STREET_DECISION` exercise: the prompt, the hand on an actual
 * `PokerTableComponent` (a "focus view" of just hero + villain, per
 * ARCHITECTURE.md §21's mobile-first default — the scenario data doesn't
 * carry full 9-max seating anyway, just hero/villain positions), the action
 * history leading up to the decision, then the same `OptionPicker`
 * `MultipleChoiceExercise` uses (both are graded by `OptionGrader` on the backend).
 */
@Component({
  selector: 'app-street-decision-exercise',
  imports: [PokerTableComponent, OptionPicker],
  templateUrl: './street-decision-exercise.html',
})
export class StreetDecisionExercise {
  readonly exercise = input.required<ExerciseDetail>();
  readonly attempt = input<AttemptResponse | null>(null);
  readonly submitting = input(false);

  readonly select = output<string>();

  // ExerciseRunner only ever routes a STREET_DECISION exercise here, and
  // every STREET_DECISION exercise has a scenario (enforced by how it's
  // seeded, not by the type system) — so `scenario` is treated as present.
  protected readonly seats = computed<TableSeat[]>(() => {
    const scenario = this.exercise().scenario;
    if (!scenario) return [];
    const heroCards = parseCards(scenario.heroCards);
    const seats: TableSeat[] = [
      {
        id: 'hero',
        label: `Hero (${scenario.heroPosition})`,
        stackBb: Number(scenario.effectiveStackBb),
        holeCards: heroCards.length ? heroCards : undefined,
        isDealer: scenario.heroPosition === 'BTN',
      },
    ];
    if (scenario.villainPosition) {
      seats.push({
        id: 'villain',
        label: `Villain (${scenario.villainPosition})`,
        stackBb: Number(scenario.effectiveStackBb),
        isDealer: scenario.villainPosition === 'BTN',
      });
    }
    return seats;
  });

  protected readonly board = computed<TableCard[]>(() => parseCards(this.exercise().scenario?.boardCards ?? null));

  protected readonly actions = computed<PokerScenarioAction[]>(() => {
    const raw = this.exercise().scenario?.actionHistory;
    if (!raw) return [];
    try {
      return JSON.parse(raw) as PokerScenarioAction[];
    } catch {
      return []; // malformed JSON shouldn't crash the exercise — just skip the action-history line
    }
  });
}
