package com.pokergto.poker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HoleCardsTest {

  @Test
  void parsesFrontendNotation() {
    HoleCards holeCards = HoleCards.of("AsQs");
    assertEquals(Card.of("As"), holeCards.first());
    assertEquals(Card.of("Qs"), holeCards.second());
  }

  @Test
  void roundTripsThroughToString() {
    assertEquals("AsQs", HoleCards.of("AsQs").toString());
  }

  @Test
  void rejectsDuplicateCard() {
    assertThrows(IllegalArgumentException.class, () -> HoleCards.of("AsAs"));
  }

  @Test
  void rejectsWrongLength() {
    assertThrows(IllegalArgumentException.class, () -> HoleCards.of("As"));
  }
}
