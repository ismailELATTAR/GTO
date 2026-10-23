package com.pokergto.poker.model;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * One playing card. Plain Java — no JPA, no Spring annotations — per
 * ARCHITECTURE.md §3: the {@code poker/} package stays framework-free so
 * equity math, combinatorics, and card models are unit-testable in
 * isolation and portable if ever extracted into a separate library.
 *
 * <p>A {@code record} rather than the {@code @Entity}-style classes in
 * {@code curriculum/model} or {@code exercise/model} — this never gets
 * persisted or mutated, so there's no reason for it to be anything other
 * than an immutable pair of (rank, suit). Records give you the constructor,
 * {@code equals}/{@code hashCode}, and accessors ({@code rank()}/{@code suit()})
 * for free from the field list alone.
 */
public record Card(Rank rank, Suit suit) {

  /** Ace-high convention (2-14) — the usual case. Low-ace ("wheel") straights are a combinatorics concern, not this type's. */
  public enum Rank {
    TWO('2', 2),
    THREE('3', 3),
    FOUR('4', 4),
    FIVE('5', 5),
    SIX('6', 6),
    SEVEN('7', 7),
    EIGHT('8', 8),
    NINE('9', 9),
    TEN('T', 10),
    JACK('J', 11),
    QUEEN('Q', 12),
    KING('K', 13),
    ACE('A', 14);

    private static final Map<Character, Rank> BY_SYMBOL =
        Stream.of(values()).collect(Collectors.toMap(Rank::symbol, r -> r));

    private final char symbol;
    private final int value;

    Rank(char symbol, int value) {
      this.symbol = symbol;
      this.value = value;
    }

    public char symbol() {
      return symbol;
    }

    public int value() {
      return value;
    }

    public static Rank fromSymbol(char symbol) {
      Rank rank = BY_SYMBOL.get(Character.toUpperCase(symbol));
      if (rank == null) {
        throw new IllegalArgumentException("Unknown rank symbol: " + symbol);
      }
      return rank;
    }
  }

  public enum Suit {
    CLUBS('c'),
    DIAMONDS('d'),
    HEARTS('h'),
    SPADES('s');

    private static final Map<Character, Suit> BY_SYMBOL =
        Stream.of(values()).collect(Collectors.toMap(Suit::symbol, s -> s));

    private final char symbol;

    Suit(char symbol) {
      this.symbol = symbol;
    }

    public char symbol() {
      return symbol;
    }

    public static Suit fromSymbol(char symbol) {
      Suit suit = BY_SYMBOL.get(Character.toLowerCase(symbol));
      if (suit == null) {
        throw new IllegalArgumentException("Unknown suit symbol: " + symbol);
      }
      return suit;
    }
  }

  /**
   * Parses the same two-character notation the frontend's {@code shared/poker/card}
   * component uses ("As", "Td", "2h" — rank char + suit char) so a card can
   * round-trip through JSON without a translation layer on either side.
   */
  public static Card of(String notation) {
    if (notation == null || notation.length() != 2) {
      throw new IllegalArgumentException("Expected a 2-character card notation (e.g. \"As\"), got: " + notation);
    }
    return new Card(Rank.fromSymbol(notation.charAt(0)), Suit.fromSymbol(notation.charAt(1)));
  }

  @Override
  public String toString() {
    return "" + rank.symbol() + suit.symbol();
  }
}
