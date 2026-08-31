import { Component, computed, input } from '@angular/core';
import { PokerCard, Rank, Suit } from '../card/card';

export interface TableCard {
  rank: Rank;
  suit: Suit;
}

export interface TableSeat {
  /** Stable id for `@for` tracking — a seat position id (e.g. "BTN") or a user id, not necessarily unique across tables. */
  id: string;
  /** Displayed under the seat, e.g. "BTN", "Hero", "CO". */
  label: string;
  stackBb: number;
  /** undefined/null = unknown (rendered face-down); omit entirely for an empty seat's hole cards. */
  holeCards?: TableCard[] | null;
  folded?: boolean;
  isDealer?: boolean;
  /** Highlights this seat as the one currently on to act. */
  isActing?: boolean;
}

/**
 * The poker table itself — seats arranged around an ellipse, board cards,
 * pot, dealer button. Pure/presentational (per ARCHITECTURE.md §20), built
 * on `PokerCard`. Seat count is whatever `seats` contains (2–9 handed);
 * positions are computed, not hardcoded per seat count, so it works for any
 * table size the caller passes in.
 */
@Component({
  selector: 'app-poker-table',
  imports: [PokerCard],
  templateUrl: './table.html',
})
export class PokerTableComponent {
  readonly seats = input.required<TableSeat[]>();
  /** Community cards revealed so far — length 0/3/4/5 in a real hand, but any length up to 5 renders fine. */
  readonly board = input<TableCard[]>([]);
  readonly potBb = input(0);

  // Fixed 5 slots so the board area doesn't reflow as cards are dealt street
  // by street — undealt slots render as empty dashed outlines.
  protected readonly boardSlots = [0, 1, 2, 3, 4] as const;

  // Seats are placed around a parametric ellipse (not a hardcoded position
  // per seat count) so this works unchanged from heads-up to 9-handed. Seat
  // 0 is placed at the bottom center (the conventional "hero" spot in every
  // poker client), and the rest proceed clockwise around it.
  protected readonly seatPositions = computed(() => {
    const seats = this.seats();
    const n = seats.length;
    if (n === 0) return [];
    return seats.map((seat, i) => {
      const angle = Math.PI / 2 + (i * (2 * Math.PI)) / n;
      const xPct = 50 + 44 * Math.cos(angle);
      const yPct = 50 + 42 * Math.sin(angle);
      return { seat, xPct, yPct };
    });
  });
}
