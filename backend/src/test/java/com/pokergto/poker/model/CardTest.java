package com.pokergto.poker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Plain JUnit, no {@code @SpringBootTest} — Card has no dependency on
 * Spring at all, so it doesn't need the application context spun up to
 * test (that's the whole point of keeping {@code poker/} framework-free).
 */
class CardTest {

  @Test
  void parsesFrontendNotation() {
    Card card = Card.of("As");
    assertEquals(Card.Rank.ACE, card.rank());
    assertEquals(Card.Suit.SPADES, card.suit());
  }

  @Test
  void roundTripsThroughToString() {
    assertEquals("Th", Card.of("Th").toString());
    assertEquals("2c", Card.of("2c").toString());
  }

  @Test
  void rejectsUnknownNotation() {
    assertThrows(IllegalArgumentException.class, () -> Card.of("Xs"));
    assertThrows(IllegalArgumentException.class, () -> Card.of("A"));
    assertThrows(IllegalArgumentException.class, () -> Card.of(null));
  }

  @Test
  void ranksCompareByPokerValue() {
    assertTrue(Card.Rank.ACE.value() > Card.Rank.KING.value());
    assertTrue(Card.Rank.TWO.value() < Card.Rank.THREE.value());
  }

  @Test
  void equalRankAndSuitMeansEqualCard() {
    assertEquals(Card.of("Ks"), Card.of("Ks"));
    assertNotEquals(Card.of("Ks"), Card.of("Kh"));
  }
}
