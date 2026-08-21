package de.echtgut.backend.ingestion;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component triggering periodic ingestion runs.
 *
 * <p>Runs automatically based on configurable interval property {@code
 * echtgut.ingestion.rate-ms}.
 */
@Component
@RequiredArgsConstructor
public class IngestionScheduler {

  private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

  private final IngestionService ingestionService;

  /** Triggers scheduled ingestion cycle across all registered adapters. */
  @Scheduled(fixedRateString = "${echtgut.ingestion.rate-ms:3600000}")
  public void runScheduledIngestion() {
    log.info("Triggering scheduled ingestion cycle...");
    ingestionService.ingestFromAllSources();
  }
}
