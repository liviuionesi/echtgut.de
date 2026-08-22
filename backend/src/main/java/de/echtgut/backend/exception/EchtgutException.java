package de.echtgut.backend.exception;

/** Base abstract exception class for all domain-specific echtgut application exceptions. */
public abstract class EchtgutException extends RuntimeException {

  protected EchtgutException(String message) {
    super(message);
  }

  protected EchtgutException(String message, Throwable cause) {
    super(message, cause);
  }
}
