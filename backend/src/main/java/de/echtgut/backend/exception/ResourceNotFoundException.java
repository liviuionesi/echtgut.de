package de.echtgut.backend.exception;

/** Exception thrown when a requested domain resource (e.g. raw deal, experience) is not found. */
public class ResourceNotFoundException extends EchtgutException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
