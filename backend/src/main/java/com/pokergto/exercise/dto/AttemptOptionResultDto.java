package com.pokergto.exercise.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Post-answer view of an option — the answer key, now safe to reveal. */
public record AttemptOptionResultDto(UUID id, String label, boolean correct, BigDecimal correctFrequency) {}
