package com.pokergto.poker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The community cards — empty preflop, 3 on the flop, 4 on the turn, 5 on
 * the river. Unlike {@link Card}/{@link HoleCards} (fixed shape), a board
 * grows street by street, so it's backed by a `List` rather than named
 * fields, bounded to a max of 5.
 */
public record Board(List<Card> cards) {

  public Board {
    if (cards.size() > 5) {
      throw new IllegalArgumentException("A board can't have more than 5 cards, got " + cards.size());
    }
    cards = List.copyOf(cards); // defensive copy — callers shouldn't be able to mutate a Board after constructing it
  }

  public static final Board PREFLOP = new Board(List.of());

  /** Parses the frontend's board notation — card notations concatenated, e.g. "Kh7d2s" for a flop. Empty/null means preflop. */
  public static Board of(String notation) {
    if (notation == null || notation.isEmpty()) {
      return PREFLOP;
    }
    if (notation.length() % 2 != 0) {
      throw new IllegalArgumentException(
          "Board notation must be an even number of characters (2 per card), got: " + notation);
    }
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < notation.length(); i += 2) {
      cards.add(Card.of(notation.substring(i, i + 2)));
    }
    return new Board(cards);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    cards.forEach(sb::append);
    return sb.toString();
  }
}
