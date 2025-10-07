package com.fiserv.fico.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(Instant timestamp, String message, Map<String, ?> details) {

  public static ApiErrorResponse of(String message, Map<String, ?> details) {
    return new ApiErrorResponse(Instant.now(), message, details);
  }

  public static ApiErrorResponse of(String message) {
    return of(message, Map.of());
  }
}
