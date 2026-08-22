package de.echtgut.backend.exception;

/** Exception thrown when Spring Security configuration fails to build or initialize. */
public class SecurityConfigurationException extends EchtgutException {

  public SecurityConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
