package com.pokergto.poker.scenario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * A structured poker hand — what a {@code street_decision} exercise renders
 * on an actual table (via {@code PokerTableComponent} on the frontend)
 * instead of describing in prose the way {@code multiple_choice}/{@code
 * numerical} do. Lives in {@code poker/scenario/} rather than under
 * {@code exercise/model/} — a deliberate exception to the rest of the
 * backend's {@code model/}-subfolder convention, since this is poker domain
 * data, not exercise-engine data; {@code exercise/model/Exercise} merely
 * references it.
 *
 * <p>{@code actionHistory} is stored and read as a raw JSON string rather
 * than deserialized into a Java type — nothing in the backend needs to
 * inspect individual actions yet (it's just handed to the frontend to
 * render), so there's no value in modeling it more strictly until something
 * does. The frontend parses it directly as JSON.
 */
@Entity
@Table(name = "poker_scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PokerScenario {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "num_players", nullable = false)
  private int numPlayers;

  @Column(name = "hero_position", nullable = false)
  private String heroPosition;

  @Column(name = "villain_position")
  private String villainPosition;

  @Column(name = "effective_stack_bb", nullable = false)
  private BigDecimal effectiveStackBb;

  @Column(name = "pot_bb", nullable = false)
  private BigDecimal potBb;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "action_history", nullable = false, columnDefinition = "jsonb")
  private String actionHistory;

  /** Hole cards notation, e.g. "AsQs" — see {@code com.pokergto.poker.model.HoleCards#of}. */
  @Column(name = "hero_cards")
  private String heroCards;

  /** Board notation, e.g. "Kh7d2s" — see {@code com.pokergto.poker.model.Board#of}. NULL/empty = preflop. */
  @Column(name = "board_cards")
  private String boardCards;
}
