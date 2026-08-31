import { Component, input } from '@angular/core';

/**
 * A small stack of poker chips used to represent a stat (XP, accuracy,
 * streak, …) — the poker-native alternative to a generic icon+number
 * dashboard tile. Pure presentational component: it only renders whatever
 * `label`/`value`/`active` it's given, no business logic, no HTTP — safe
 * to reuse from the dashboard, progress page, or anywhere else a stat
 * needs to be shown (per ARCHITECTURE.md §2's shared/poker/* convention).
 */
@Component({
  selector: 'app-chip-stack',
  templateUrl: './chip-stack.html',
})
export class ChipStack {
  /** e.g. "XP", "Accuracy", "Streak" */
  readonly label = input.required<string>();

  /** Already-formatted display value, e.g. "0", "—", "3-day". Formatting stays the caller's job. */
  readonly value = input.required<string>();

  /** Gold chips once there's real progress behind this stat; quiet ghost chips for an honest zero-state. */
  readonly active = input(false);
}
