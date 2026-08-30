package com.pokergto.auth.service;

import com.pokergto.auth.dto.AuthResponse;

/**
 * Internal carrier only — never serialized directly. Splits the refresh
 * token out from {@link AuthResponse} because it belongs in an httpOnly
 * cookie (set by AuthController), not in the JSON body.
 */
public record AuthResult(AuthResponse response, String refreshToken, long refreshExpiresInSeconds) {}
