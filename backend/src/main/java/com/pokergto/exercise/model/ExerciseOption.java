package com.pokergto.exercise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * One answer choice for a {@code multiple_choice} {@link Exercise}.
 *
 * <p>{@code correctFrequency} is null for a plain binary exercise (one right
 * answer) and set for a mixed-frequency spot (e.g. "Call 65% / 3-Bet 35%") —
 * see {@code MultipleChoiceGrader} for how the two cases are scored.
 */
@Entity
@Table(name = "exercise_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseOption {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @Column(nullable = false)
  private String label;

  @Column(name = "is_correct", nullable = false)
  @Builder.Default
  private boolean correct = false;

  @Column(name = "correct_frequency")
  private BigDecimal correctFrequency;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private int sortOrder = 0;
}
