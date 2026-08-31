import { Component, computed, input } from '@angular/core';

export type Suit = 's' | 'h' | 'd' | 'c';
export type Rank = '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | 'T' | 'J' | 'Q' | 'K' | 'A';

const SUIT_SYMBOL: Record<Suit, string> = { s: '♠', h: '♥', d: '♦', c: '♣' };
const RED_SUITS = new Set<Suit>(['h', 'd']);

/**
 * A single playing card — pure/presentational (per ARCHITECTURE.md §20:
 * shared/poker/* components are inputs/outputs only, no HTTP), reusable by
 * the exercise engine, the lesson viewer, and (later) the hand trainer
 * alike. SVG with a `viewBox` rather than fixed px so it scales cleanly at
 * any size the caller renders it at — set width via a wrapper class, not
 * props on this component.
 */
@Component({
  selector: 'app-poker-card',
  templateUrl: './card.html',
})
export class PokerCard {
  readonly rank = input.required<Rank>();
  readonly suit = input.required<Suit>();

  /** Renders the card back instead of its face — e.g. an opponent's hidden hole cards. */
  readonly faceDown = input(false);

  protected readonly suitSymbol = computed(() => SUIT_SYMBOL[this.suit()]);
  protected readonly isRed = computed(() => RED_SUITS.has(this.suit()));
}
