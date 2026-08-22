package de.echtgut.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/test");
  }

  @Test
  void testHandleResourceNotFoundException() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
    ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(404);
    assertThat(response.getBody().message()).isEqualTo("Not found");
    assertThat(response.getBody().path()).isEqualTo("/api/test");
  }

  @Test
  void testHandleInvalidDealOperationException() {
    InvalidDealOperationException ex = new InvalidDealOperationException("Invalid operation");
    ResponseEntity<ErrorResponse> response =
        handler.handleInvalidDealOperationException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(400);
    assertThat(response.getBody().message()).isEqualTo("Invalid operation");
  }

  @Test
  void testHandleUnauthorizedException() {
    UnauthorizedException ex = new UnauthorizedException("Unauthorized access");
    ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(401);
  }

  @Test
  void testHandleMethodArgumentNotValidException() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "field", "must not be blank");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodParameter parameter = mock(MethodParameter.class);
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<ErrorResponse> response =
        handler.handleMethodArgumentNotValidException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().details()).containsExactly("field: must not be blank");
  }

  @Test
  void testHandleIllegalArgumentException() {
    IllegalArgumentException ex = new IllegalArgumentException("Bad arg");
    ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().message()).isEqualTo("Bad arg");
  }

  @Test
  void testHandleGenericException() {
    Exception ex = new RuntimeException("Crash");
    ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(500);
  }
}
