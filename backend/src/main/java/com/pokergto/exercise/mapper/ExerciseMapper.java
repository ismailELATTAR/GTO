package com.pokergto.exercise.mapper;

import com.pokergto.exercise.dto.AttemptOptionResultDto;
import com.pokergto.exercise.dto.ExerciseDetailDto;
import com.pokergto.exercise.dto.ExerciseOptionDto;
import com.pokergto.exercise.dto.PokerScenarioDto;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseOption;
import com.pokergto.poker.scenario.PokerScenario;
import org.mapstruct.Mapper;

/**
 * Two views of the same {@link ExerciseOption}: {@link #toOptionDto} for the
 * pre-answer DTO (MapStruct only copies fields the target has, so `correct`/
 * `correctFrequency` are never touched) and {@link #toOptionResult} for the
 * post-attempt DTO where revealing them is the point.
 */
@Mapper(componentModel = "spring")
public interface ExerciseMapper {

  // `options` (List<ExerciseOption> -> List<ExerciseOptionDto>) and `scenario`
  // (PokerScenario -> PokerScenarioDto) are both mapped automatically via the
  // methods below — MapStruct finds them by matching types. A null `scenario`
  // (every non-STREET_DECISION exercise) maps to a null PokerScenarioDto.
  ExerciseDetailDto toDetail(Exercise exercise);

  ExerciseOptionDto toOptionDto(ExerciseOption option);

  AttemptOptionResultDto toOptionResult(ExerciseOption option);

  PokerScenarioDto toScenarioDto(PokerScenario scenario);
}
