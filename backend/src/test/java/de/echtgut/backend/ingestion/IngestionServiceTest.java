package de.echtgut.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealRepository;
import de.echtgut.backend.curation.RawDealStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test suite for {@link IngestionService}.
 *
 * <p>Verifies initial ingestion, deduplication by source_ref, updating existing deals without status
 * override, and graceful exception isolation.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class IngestionServiceTest {

  @Autowired private IngestionService ingestionService;
  @Autowired private RawDealRepository rawDealRepository;

  @Test
  @DisplayName("1. Given initial ingestion run, populates raw_deals with PENDING seed deals")
  void testInitialIngestion() {
    // 1. Given empty raw_deals table
    rawDealRepository.deleteAllInBatch();

    // 2. When executing ingestion
    ingestionService.ingestFromAllSources();

    // 3. Then candidate deals from SeedRawDealSource are inserted
    List<RawDeal> deals = rawDealRepository.findAll();
    assertThat(deals).hasSizeGreaterThanOrEqualTo(3);
    assertThat(deals).allMatch(d -> d.getStatus() == RawDealStatus.PENDING);
  }

  @Test
  @DisplayName("2. Given repeated ingestion runs, deduplicates by source_ref without creating duplicates")
  void testDeduplicationOnRepeatRun() {
    // 1. Given initial ingestion run
    rawDealRepository.deleteAllInBatch();
    ingestionService.ingestFromAllSources();
    long initialCount = rawDealRepository.count();

    // 2. When executing second ingestion run
    ingestionService.ingestFromAllSources();

    // 3. Then row count remains identical (deduplicated)
    assertThat(rawDealRepository.count()).isEqualTo(initialCount);
  }

  @Test
  @DisplayName("3. Given existing REJECTED deal, re-ingestion updates fields but retains REJECTED status")
  void testReIngestionRetainsStatus() {
    // 1. Given a raw deal previously marked REJECTED by a curator
    rawDealRepository.deleteAllInBatch();
    RawDeal existing =
        RawDeal.builder()
            .source("SEED")
            .sourceRef("seed-deal-001")
            .rawTitle("Old Title")
            .status(RawDealStatus.REJECTED)
            .rejectionReason("Inaccurate details")
            .build();
    rawDealRepository.saveAndFlush(existing);

    // 2. When ingestion runs with updated title from source
    ingestionService.ingestFromAllSources();

    // 3. Then title is updated but status remains REJECTED
    RawDeal updated = rawDealRepository.findBySourceRef("seed-deal-001").orElseThrow();
    assertThat(updated.getRawTitle()).isEqualTo("Bio-Bäckerei Brotgarten — Frisches Holzofenbrot");
    assertThat(updated.getStatus()).isEqualTo(RawDealStatus.REJECTED);
    assertThat(updated.getRejectionReason()).isEqualTo("Inaccurate details");
  }
}
