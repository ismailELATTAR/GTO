package com.pokergto.exercise.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * `options` is empty for non-multiple_choice types; `correctAnswer` is null
 * for everything except `numerical` — same "only the relevant fields are
 * non-null for this type" shape as `Exercise` itself.
 */
public record AttemptResponseDto(
    boolean correct,
    BigDecimal score,
    String explanation,
    List<AttemptOptionResultDto> options,
    BigDecimal correctAnswer) {}
