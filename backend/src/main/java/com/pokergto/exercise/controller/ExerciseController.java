package com.pokergto.exercise.controller;

import com.pokergto.exercise.dto.AttemptRequestDto;
import com.pokergto.exercise.dto.AttemptResponseDto;
import com.pokergto.exercise.dto.ExerciseDetailDto;
import com.pokergto.exercise.model.Difficulty;
import com.pokergto.exercise.model.ExerciseType;
import com.pokergto.exercise.service.ExerciseService;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

  private final ExerciseService exerciseService;

  public ExerciseController(ExerciseService exerciseService) {
    this.exerciseService = exerciseService;
  }

  @GetMapping("/next")
  public ExerciseDetailDto getNext(
      @RequestParam(required = false) UUID topicId,
      @RequestParam(required = false) Difficulty difficulty,
      @RequestParam(required = false) ExerciseType type) {
    return exerciseService.getNext(topicId, difficulty, type);
  }

  @GetMapping("/{id}")
  public ExerciseDetailDto getExercise(@PathVariable UUID id) {
    return exerciseService.getExercise(id);
  }

  @PostMapping("/{id}/attempt")
  public AttemptResponseDto submitAttempt(
      @PathVariable UUID id, @RequestBody AttemptRequestDto request, @AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return exerciseService.submitAttempt(id, userId, request);
  }
}
