package com.pokergto.curriculum.dto;

import java.util.UUID;

public record ModuleSummaryDto(UUID id, String title, String description, int orderIndex) {}
