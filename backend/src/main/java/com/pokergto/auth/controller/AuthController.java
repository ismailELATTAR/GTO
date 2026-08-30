package com.pokergto.auth.controller;

import com.pokergto.auth.dto.AuthResponse;
import com.pokergto.auth.dto.LoginRequest;
import com.pokergto.auth.dto.RegisterRequest;
import com.pokergto.auth.service.AuthResult;
import com.pokergto.auth.service.AuthService;
import com.pokergto.common.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private static final String REFRESH_COOKIE = "refreshToken";

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/api/auth/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return respond(authService.register(request));
  }

  @PostMapping("/api/auth/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return respond(authService.login(request));
  }

  @PostMapping("/api/auth/refresh")
  public ResponseEntity<AuthResponse> refresh(
      @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
    if (refreshToken == null) {
      throw ApiException.unauthorized("No active session");
    }
    return respond(authService.refresh(refreshToken));
  }

  @PostMapping("/api/auth/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
    authService.logout(refreshToken);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .header("Set-Cookie", clearRefreshCookie().toString())
        .build();
  }

  /** Shared by register/login/refresh: body carries the access token + user, cookie carries the refresh token. */
  private ResponseEntity<AuthResponse> respond(AuthResult result) {
    return ResponseEntity.ok()
        .header("Set-Cookie", refreshCookie(result.refreshToken(), result.refreshExpiresInSeconds()).toString())
        .body(result.response());
  }

  private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(REFRESH_COOKIE, value)
        .httpOnly(true)
        .secure(false) // TODO: true once served over HTTPS in production
        .sameSite("Lax")
        .path("/api/auth")
        .maxAge(maxAgeSeconds)
        .build();
  }

  private ResponseCookie clearRefreshCookie() {
    return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).path("/api/auth").maxAge(0).build();
  }
}
