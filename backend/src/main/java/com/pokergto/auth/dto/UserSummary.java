package com.pokergto.auth.dto;

import java.util.UUID;

/** Matches the frontend's `AuthenticatedUser` shape (core/state/session.store.ts). */
public record UserSummary(UUID id, String email, String displayName) {}
