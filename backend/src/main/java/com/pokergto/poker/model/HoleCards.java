package com.pokergto.poker.model;

import java.util.List;

/** A player's two private cards. Same framework-free style as {@link Card}. */
public record HoleCards(Card first, Card second) {

  public HoleCards {
    if (first.equals(second)) {
      throw new IllegalArgumentException("Hole cards must be two distinct cards, got two of " + first);
    }
  }

  /** Parses the frontend's 4-character notation ("AsQs" = first two chars, second two chars — each a {@link Card#of}). */
  public static HoleCards of(String notation) {
    if (notation == null || notation.length() != 4) {
      throw new IllegalArgumentException("Expected a 4-character hole-cards notation (e.g. \"AsQs\"), got: " + notation);
    }
    return new HoleCards(Card.of(notation.substring(0, 2)), Card.of(notation.substring(2, 4)));
  }

  public List<Card> asList() {
    return List.of(first, second);
  }

  @Override
  public String toString() {
    return first.toString() + second.toString();
  }
}
