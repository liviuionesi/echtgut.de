package de.echtgut.backend.ingestion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration enabling scheduled ingestion background processing.
 *
 * <p>Can be disabled in testing environments via property {@code echtgut.ingestion.enabled=false}.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "echtgut.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class IngestionConfig {}
