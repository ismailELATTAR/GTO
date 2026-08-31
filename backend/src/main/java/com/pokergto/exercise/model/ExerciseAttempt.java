package com.pokergto.exercise.model;

import com.pokergto.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * One user's answer to one {@link Exercise}. {@code score} is 0-100 —
 * frequency-based partial credit for multiple_choice (the frequency of the
 * option the user picked, not a binary right/wrong), same scale other
 * exercise types will use once they're built.
 */
@Entity
@Table(name = "exercise_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id", nullable = false)
  private Exercise exercise;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "selected_option_id")
  private ExerciseOption selectedOption;

  @Column(name = "numeric_answer")
  private BigDecimal numericAnswer;

  @Column(name = "is_correct", nullable = false)
  private boolean correct;

  @Column(nullable = false)
  private BigDecimal score;

  @Column(name = "response_time_ms")
  private Integer responseTimeMs;

  @Column(name = "attempted_at", nullable = false, updatable = false)
  private Instant attemptedAt;

  @PrePersist
  void onCreate() {
    if (attemptedAt == null) {
      attemptedAt = Instant.now();
    }
  }
}
