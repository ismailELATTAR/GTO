package com.pokergto.exercise.service;

import com.pokergto.common.exception.ApiException;
import com.pokergto.exercise.dto.AttemptOptionResultDto;
import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.dto.AttemptResponseDto;
import com.pokergto.exercise.dto.ExerciseDetailDto;
import com.pokergto.exercise.grading.AnswerGrader;
import com.pokergto.exercise.grading.GradeResult;
import com.pokergto.exercise.mapper.ExerciseMapper;
import com.pokergto.exercise.model.Difficulty;
import com.pokergto.exercise.model.Exercise;
import com.pokergto.exercise.model.ExerciseAttempt;
import com.pokergto.exercise.model.ExerciseType;
import com.pokergto.exercise.repository.ExerciseAttemptRepository;
import com.pokergto.exercise.repository.ExerciseRepository;
import com.pokergto.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExerciseService {

  private final ExerciseRepository exerciseRepository;
  private final ExerciseAttemptRepository attemptRepository;
  private final UserRepository userRepository;
  private final ExerciseMapper mapper;
  private final Map<ExerciseType, AnswerGrader> graders;

  public ExerciseService(
      ExerciseRepository exerciseRepository,
      ExerciseAttemptRepository attemptRepository,
      UserRepository userRepository,
      ExerciseMapper mapper,
      List<AnswerGrader> graders) {
    this.exerciseRepository = exerciseRepository;
    this.attemptRepository = attemptRepository;
    this.userRepository = userRepository;
    this.mapper = mapper;
    // Spring autowires every AnswerGrader bean into this list; keying it by
    // supportedType() is what lets the service stay unchanged as new
    // exercise types (and their graders) get added.
    this.graders = graders.stream().collect(Collectors.toMap(AnswerGrader::supportedType, Function.identity()));
  }

  /**
   * MVP placeholder: picks uniformly at random among published candidates
   * matching the given (all optional) filters — no weighting by weakest
   * topic, no spaced repetition. That's Phase 6, once progress-tracking
   * data (attempts_count, next_review_at, …) exists to base it on.
   */
  public ExerciseDetailDto getNext(UUID topicId, Difficulty difficulty, ExerciseType type) {
    List<Exercise> candidates = exerciseRepository.findCandidates(topicId, difficulty, type);
    if (candidates.isEmpty()) {
      throw ApiException.notFound("Exercise");
    }
    Exercise exercise = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    return mapper.toDetail(exercise);
  }

  public ExerciseDetailDto getExercise(UUID id) {
    Exercise exercise = findPublishedExercise(id);
    return mapper.toDetail(exercise);
  }

  @Transactional
  public AttemptResponseDto submitAttempt(UUID exerciseId, UUID userId, AttemptRequestDto request) {
    Exercise exercise = findPublishedExercise(exerciseId);
    AnswerGrader grader = graders.get(exercise.getType());
    if (grader == null) {
      throw ApiException.badRequest("No grader available for exercise type " + exercise.getType());
    }

    GradeResult result = grader.grade(exercise, request);

    ExerciseAttempt attempt =
        ExerciseAttempt.builder()
            .user(userRepository.getReferenceById(userId))
            .exercise(exercise)
            .selectedOption(result.selectedOption())
            .numericAnswer(request.numericAnswer())
            .correct(result.correct())
            .score(result.score())
            .responseTimeMs(request.responseTimeMs())
            .build();
    attemptRepository.save(attempt);

    List<AttemptOptionResultDto> optionResults = exercise.getOptions().stream().map(mapper::toOptionResult).toList();
    // correctAnswer is only non-null on a NUMERICAL exercise's row (see
    // Exercise's class-level note) — naturally null in the response for
    // every other type, same as options is naturally empty for NUMERICAL.
    return new AttemptResponseDto(
        result.correct(), result.score(), exercise.getExplanation(), optionResults, exercise.getCorrectAnswer());
  }

  private Exercise findPublishedExercise(UUID id) {
    return exerciseRepository.findByIdAndPublishedTrue(id).orElseThrow(() -> ApiException.notFound("Exercise"));
  }
}
