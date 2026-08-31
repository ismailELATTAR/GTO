import { Component } from '@angular/core';
import { PokerCard, Rank, Suit } from '../../shared/poker/card/card';
import { PokerTableComponent, TableSeat, TableCard } from '../../shared/poker/table/table';

/**
 * A living reference/demo page for the shared/poker/* components (Phase 5) —
 * not part of the MVP user flow, just a permanent place to look at PokerCard
 * and PokerTableComponent in isolation while more of Phase 5 gets built on
 * top of them (street_decision exercises, the hand trainer). Reachable at
 * /poker-demo; not linked from the nav since it isn't user-facing content.
 */
@Component({
  selector: 'app-poker-demo',
  imports: [PokerCard, PokerTableComponent],
  templateUrl: './poker-demo.html',
})
export class PokerDemo {
  protected readonly suits: Suit[] = ['s', 'h', 'd', 'c'];
  protected readonly ranks: Rank[] = ['A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2'];

  protected readonly sixMaxSeats: TableSeat[] = [
    { id: 'hero', label: 'Hero (BTN)', stackBb: 97, holeCards: [{ rank: 'A', suit: 's' }, { rank: 'Q', suit: 's' }] },
    { id: 'sb', label: 'SB', stackBb: 103, folded: true },
    { id: 'bb', label: 'BB', stackBb: 88, isActing: true },
    { id: 'utg', label: 'UTG', stackBb: 120, folded: true },
    { id: 'hj', label: 'HJ', stackBb: 76, folded: true },
    { id: 'co', label: 'CO', stackBb: 140, isDealer: true, folded: true },
  ];
  protected readonly sixMaxBoard: TableCard[] = [
    { rank: 'K', suit: 'h' },
    { rank: '7', suit: 'd' },
    { rank: '2', suit: 's' },
  ];

  protected readonly headsUpSeats: TableSeat[] = [
    {
      id: 'hero',
      label: 'Hero (BB)',
      stackBb: 64,
      holeCards: [{ rank: 'K', suit: 'd' }, { rank: 'K', suit: 'c' }],
      isActing: true,
    },
    { id: 'villain', label: 'Villain (BTN)', stackBb: 136, isDealer: true },
  ];
}
