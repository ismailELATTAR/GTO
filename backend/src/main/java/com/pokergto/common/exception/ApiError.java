package com.pokergto.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error response shape returned by every failed API call.
 */
public record ApiError(
    Instant timestamp,
    int status,
    String code,
    String message,
    List<FieldError> fieldErrors) {

  public record FieldError(String field, String message) {}

  public static ApiError of(int status, String code, String message) {
    return new ApiError(Instant.now(), status, code, message, List.of());
  }

  public static ApiError of(int status, String code, String message, List<FieldError> fieldErrors) {
    return new ApiError(Instant.now(), status, code, message, fieldErrors);
  }
}
