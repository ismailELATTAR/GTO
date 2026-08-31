package com.pokergto.exercise.dto;

import com.pokergto.exercise.model.Difficulty;
import com.pokergto.exercise.model.ExerciseType;
import java.util.List;
import java.util.UUID;

/**
 * What a user sees before answering — no `explanation`, no option
 * `correct`/`correctFrequency`, no `correctAnswer`. `answerUnit` (e.g. "%",
 * "BB") is safe to show pre-answer — it's a display label, not the answer
 * key — and lets a `numerical` exercise's input show the right suffix.
 */
public record ExerciseDetailDto(
    UUID id,
    ExerciseType type,
    Difficulty difficulty,
    String prompt,
    int xpReward,
    List<ExerciseOptionDto> options,
    String answerUnit) {}
