package com.pokergto.exercise.repository;

import com.pokergto.exercise.model.Difficulty;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

  Optional<Exercise> findByIdAndPublishedTrue(UUID id);

  // Backs ExerciseService#getNext — each filter is optional (NULL = "any"),
  // so one query covers every combination of topic/difficulty/type instead
  // of a finder method per combination (that was manageable at 2 filters,
  // not at 3). Ordering doesn't matter here since the caller picks randomly
  // among the results anyway; kept for determinism in tests/debugging.
  @Query(
      "SELECT e FROM Exercise e WHERE e.published = true "
          + "AND (:topicId IS NULL OR e.topic.id = :topicId) "
          + "AND (:difficulty IS NULL OR e.difficulty = :difficulty) "
          + "AND (:type IS NULL OR e.type = :type) "
          + "ORDER BY e.createdAt ASC")
  List<Exercise> findCandidates(
      @Param("topicId") UUID topicId, @Param("difficulty") Difficulty difficulty, @Param("type") ExerciseType type);
}
