package com.pokergto.exercise.model;

import com.pokergto.curriculum.model.Topic;
import com.pokergto.poker.scenario.PokerScenario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * One practice question. {@code explanation} and each {@link ExerciseOption}'s
 * {@code correct}/{@code correctFrequency} are the answer key — never put this
 * entity behind a pre-answer DTO directly (see {@code exercise.dto} package).
 */
@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private Topic topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ExerciseType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Difficulty difficulty;

  @Column(nullable = false)
  private String prompt;

  @Column(nullable = false)
  private String explanation;

  @Column(name = "xp_reward", nullable = false)
  @Builder.Default
  private int xpReward = 10;

  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean published = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("sortOrder ASC")
  @Builder.Default
  private List<ExerciseOption> options = new ArrayList<>();

  // The three below are only set for type = NUMERICAL — exercise_options is
  // multiple_choice-specific, so a numerical exercise's answer key (a single
  // value + tolerance band) lives on the exercise row itself instead. See
  // NumericalGrader for how correctAnswer/answerTolerance are used.
  @Column(name = "correct_answer")
  private BigDecimal correctAnswer;

  @Column(name = "answer_tolerance")
  private BigDecimal answerTolerance;

  /** Display-only unit shown alongside the input, e.g. "%", "BB" — not the answer key, safe pre-answer. */
  @Column(name = "answer_unit")
  private String answerUnit;

  /** Only set for type = STREET_DECISION — the hand this exercise renders on an actual table. No answer-key data of its own. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scenario_id")
  private PokerScenario scenario;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
