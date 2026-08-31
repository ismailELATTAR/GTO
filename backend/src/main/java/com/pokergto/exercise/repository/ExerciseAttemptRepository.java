package com.pokergto.exercise.repository;

import com.pokergto.exercise.model.ExerciseAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, UUID> {}
