package de.echtgut.backend.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standardized DTO payload returned on REST API error conditions.
 *
 * @param status HTTP status code.
 * @param error HTTP status phrase.
 * @param message Human-readable error description.
 * @param path Request URI path.
 * @param timestamp Epoch timestamp of failure.
 * @param details Optional field-level validation error details.
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    List<String> details) {

  public ErrorResponse(int status, String error, String message, String path) {
    this(status, error, message, path, Instant.now(), List.of());
  }

  public ErrorResponse(int status, String error, String message, String path, List<String> details) {
    this(status, error, message, path, Instant.now(), details);
  }
}
