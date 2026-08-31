package com.pokergto.exercise.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * `selectedOptionId` is what {@code multiple_choice} grading uses;
 * `numericAnswer` is unused for now — reserved for the numerical/pot-odds
 * exercise type that extends this same endpoint shape later.
 */
public record AttemptRequestDto(UUID selectedOptionId, BigDecimal numericAnswer, Integer responseTimeMs) {}
