package de.echtgut.backend.exception;

/** Exception thrown when an operation on a deal candidate violates quality or state invariants. */
public class InvalidDealOperationException extends EchtgutException {

  public InvalidDealOperationException(String message) {
    super(message);
  }
}
