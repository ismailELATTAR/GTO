package com.pokergto.curriculum.dto;

import java.util.UUID;

public record CourseSummaryDto(UUID id, String slug, String title, String description, int orderIndex) {}
