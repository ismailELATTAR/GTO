package com.pokergto.poker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardTest {

  @Test
  void nullOrEmptyMeansPreflop() {
    assertTrue(Board.of(null).cards().isEmpty());
    assertTrue(Board.of("").cards().isEmpty());
    assertEquals(Board.PREFLOP, Board.of(null));
  }

  @Test
  void parsesAFlop() {
    Board flop = Board.of("Kh7d2s");
    assertEquals(3, flop.cards().size());
    assertEquals(Card.of("Kh"), flop.cards().get(0));
  }

  @Test
  void roundTripsThroughToString() {
    assertEquals("Kh7d2s", Board.of("Kh7d2s").toString());
  }

  @Test
  void rejectsMoreThanFiveCards() {
    assertThrows(IllegalArgumentException.class, () -> Board.of("Kh7d2s9hAc3d"));
  }

  @Test
  void rejectsOddLengthNotation() {
    assertThrows(IllegalArgumentException.class, () -> Board.of("Kh7"));
  }
}
