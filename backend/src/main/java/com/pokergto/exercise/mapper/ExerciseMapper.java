package com.pokergto.exercise.mapper;

import com.pokergto.exercise.dto.AttemptOptionResultDto;
import com.pokergto.exercise.dto.ExerciseDetailDto;
import com.pokergto.exercise.dto.ExerciseOptionDto;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseOption;
import org.mapstruct.Mapper;

/**
 * Two views of the same {@link ExerciseOption}: {@link #toOptionDto} for the
 * pre-answer DTO (MapStruct only copies fields the target has, so `correct`/
 * `correctFrequency` are never touched) and {@link #toOptionResult} for the
 * post-attempt DTO where revealing them is the point.
 */
@Mapper(componentModel = "spring")
public interface ExerciseMapper {

  // `options` (List<ExerciseOption> -> List<ExerciseOptionDto>) is mapped
  // automatically via toOptionDto below — MapStruct finds it by matching types.
  ExerciseDetailDto toDetail(Exercise exercise);

  ExerciseOptionDto toOptionDto(ExerciseOption option);

  AttemptOptionResultDto toOptionResult(ExerciseOption option);
}
