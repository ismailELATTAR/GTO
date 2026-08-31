package com.pokergto.exercise.dto;

import java.util.UUID;

/** Pre-answer view of an option — deliberately excludes {@code correct}/{@code correctFrequency}, the answer key. */
public record ExerciseOptionDto(UUID id, String label) {}
