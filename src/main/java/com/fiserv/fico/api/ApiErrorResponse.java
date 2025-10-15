package com.fiserv.fico.api;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

  private Instant timestamp;
  private String message;
  private Map<String, ?> details;

  public static ApiErrorResponse of(String message, Map<String, ?> details) {
    return new ApiErrorResponse(Instant.now(), message, details);
  }

  public static ApiErrorResponse of(String message) {
    return of(message, Map.of());
  }
}
