package com.pokergto.exercise.grading;

import com.pokergto.exercise.model.ExerciseOption;
import java.math.BigDecimal;

/** What grading one attempt produces — `selectedOption` is null for types (like numerical) that don't pick from a list. */
public record GradeResult(boolean correct, BigDecimal score, ExerciseOption selectedOption) {}
