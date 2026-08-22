package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.SubmissionRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for community local gem submissions.
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class PublicSubmissionController {

  private final SubmissionService submissionService;

  @PostMapping
  public ResponseEntity<Void> submitGem(
      @Valid @RequestBody SubmissionRequestDto request,
      HttpServletRequest httpServletRequest) {
    
    // Extract IP address for rate limiting
    String ipAddress = getClientIp(httpServletRequest);
    submissionService.submitLocalGem(request, ipAddress);
    
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
  
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
