package com.pokergto.curriculum.dto;

import java.util.List;
import java.util.UUID;

public record ModuleDetailDto(
    UUID id, String title, String description, UUID courseId, List<LessonSummaryDto> lessons) {}
