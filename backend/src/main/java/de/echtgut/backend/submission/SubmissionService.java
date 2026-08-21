package de.echtgut.backend.submission;

/** Service interface for community deal recommendations. */
public interface SubmissionService {

  /**
   * Health status ping for submission service.
   *
   * @return A status message indicating submission readiness.
   */
  String getSubmissionStatus();
}
