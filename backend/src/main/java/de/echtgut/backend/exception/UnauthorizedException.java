package de.echtgut.backend.exception;

/** Exception thrown when an operation fails due to missing or invalid authentication credentials. */
public class UnauthorizedException extends EchtgutException {

  public UnauthorizedException(String message) {
    super(message);
  }
}
