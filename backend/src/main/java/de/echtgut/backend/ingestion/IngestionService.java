package de.echtgut.backend.ingestion;

import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealRepository;
import de.echtgut.backend.curation.RawDealStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service orchestrating deal ingestion across registered {@link RawDealSource} adapters.
 *
 * <p>Enforces deduplication by {@code source_ref} (FR-1.2): existing raw deal rows are updated in
 * place while retaining their review status, preventing duplicate staging entries.
 */
@Service
@RequiredArgsConstructor
public class IngestionService {

  private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

  private final List<RawDealSource> rawDealSources;
  private final RawDealRepository rawDealRepository;

  /**
   * Executes ingestion cycle across all registered deal sources.
   *
   * <p>Logs and isolates errors per source adapter so a failing feed does not interrupt other
   * sources or crash the scheduler.
   */
  @Transactional
  public void ingestFromAllSources() {
    log.info("Starting ingestion cycle across {} sources", rawDealSources.size());

    for (RawDealSource source : rawDealSources) {
      try {
        log.info("Processing ingestion for source: {}", source.getSourceId());
        List<RawDeal> candidateDeals = source.fetchCandidateDeals();
        int newCount = 0;
        int updatedCount = 0;

        for (RawDeal candidate : candidateDeals) {
          Optional<RawDeal> existingOpt =
              rawDealRepository.findBySourceRef(candidate.getSourceRef());

          if (existingOpt.isPresent()) {
            RawDeal existing = existingOpt.get();
            existing.setRawTitle(candidate.getRawTitle());
            existing.setRawDescription(candidate.getRawDescription());
            existing.setRawImageUrl(candidate.getRawImageUrl());
            existing.setLocationText(candidate.getLocationText());
            existing.setLat(candidate.getLat());
            existing.setLng(candidate.getLng());
            existing.setPriceHint(candidate.getPriceHint());
            rawDealRepository.save(existing);
            updatedCount++;
          } else {
            if (candidate.getStatus() == null) {
              candidate.setStatus(RawDealStatus.PENDING);
            }
            rawDealRepository.save(candidate);
            newCount++;
          }
        }
        log.info(
            "Completed ingestion for source {}: {} inserted, {} updated",
            source.getSourceId(),
            newCount,
            updatedCount);
      } catch (Exception e) {
        log.error(
            "Failed to execute ingestion for source {}: {}",
            source.getSourceId(),
            e.getMessage(),
            e);
      }
    }
  }
}
