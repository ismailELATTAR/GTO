package com.pokergto.auth.dto;

/**
 * The refresh token is deliberately NOT in this body — it's set as an
 * httpOnly cookie by the controller instead, so frontend JS never touches
 * it (only the access token, which is short-lived and safe to hold in a
 * signal per the frontend's SessionStore).
 */
public record AuthResponse(String accessToken, UserSummary user) {}
