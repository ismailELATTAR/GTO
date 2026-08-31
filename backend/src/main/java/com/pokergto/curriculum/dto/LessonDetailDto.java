package com.pokergto.curriculum.dto;

import java.util.List;
import java.util.UUID;

/** `content` is raw markdown — the frontend renders it, the backend never does (keeps rendering a client concern). */
public record LessonDetailDto(UUID id, String title, String content, List<String> topics) {}
