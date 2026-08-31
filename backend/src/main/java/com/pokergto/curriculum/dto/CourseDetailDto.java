package com.pokergto.curriculum.dto;

import java.util.List;
import java.util.UUID;

public record CourseDetailDto(
    UUID id, String slug, String title, String description, List<ModuleSummaryDto> modules) {}
