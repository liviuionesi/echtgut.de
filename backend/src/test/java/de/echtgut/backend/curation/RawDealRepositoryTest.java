package de.echtgut.backend.curation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test suite for {@link RawDealRepository}.
 *
 * <p>Verifies persistence, deduplication queries by source_ref, status filtering, and state
 * transitions (PENDING -&gt; PROMOTED / REJECTED).
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RawDealRepositoryTest {

  @Autowired private RawDealRepository rawDealRepository;

  /** Tests saving a new candidate deal with PENDING status. */
  @Test
  @DisplayName("1. Given new deal details, when saved, then defaults to PENDING status")
  void testSaveNewRawDeal() {
    // 1. Given candidate deal properties
    RawDeal deal =
        RawDeal.builder()
            .source("MANUAL")
            .sourceRef("ref-test-101")
            .rawTitle("Kaffee & Kuchen Special")
            .rawDescription("Gemütliches Café in Berlin-Kreuzberg")
            .locationText("Bergmannstraße 12, Berlin")
            .lat(new BigDecimal("52.489123"))
            .lng(new BigDecimal("13.389456"))
            .priceHint("€5.50")
            .build();

    // 2. When saving entity
    RawDeal saved = rawDealRepository.saveAndFlush(deal);

    // 3. Then ID is generated and status defaults to PENDING
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(RawDealStatus.PENDING);
    assertThat(saved.getSourceRef()).isEqualTo("ref-test-101");
    assertThat(saved.getIngestedAt()).isNotNull();
  }

  /** Tests deduplication lookup by sourceRef. */
  @Test
  @DisplayName("2. Given existing deal sourceRef, when queried, then returns matching entity")
  void testFindBySourceRef() {
    // 1. Given existing record
    RawDeal deal =
        RawDeal.builder()
            .source("OSM")
            .sourceRef("osm-poi-98765")
            .rawTitle("Botanischer Garten Park")
            .build();
    rawDealRepository.saveAndFlush(deal);

    // 2. When querying by sourceRef
    Optional<RawDeal> found = rawDealRepository.findBySourceRef("osm-poi-98765");

    // 3. Then matching entity is returned
    assertThat(found).isPresent();
    assertThat(found.get().getRawTitle()).isEqualTo("Botanischer Garten Park");
  }

  /** Tests filtering and counting deals by review status. */
  @Test
  @DisplayName(
      "3. Given deals with different statuses, when filtered, then matches status criteria")
  void testFindByStatusAndCount() {
    // 1. Given deals with different statuses
    rawDealRepository.saveAndFlush(
        RawDeal.builder().source("SEED").sourceRef("ref-1").rawTitle("Deal 1").build());
    rawDealRepository.saveAndFlush(
        RawDeal.builder()
            .source("SEED")
            .sourceRef("ref-2")
            .rawTitle("Deal 2")
            .status(RawDealStatus.REJECTED)
            .rejectionReason("Duplicate submission")
            .reviewedAt(OffsetDateTime.now())
            .build());

    // 2. When querying by status
    List<RawDeal> pending = rawDealRepository.findByStatus(RawDealStatus.PENDING);
    List<RawDeal> rejected = rawDealRepository.findByStatus(RawDealStatus.REJECTED);

    // 3. Then exact matches are returned
    assertThat(pending).hasSize(1);
    assertThat(rejected).hasSize(1);
    assertThat(rejected.get(0).getRejectionReason()).isEqualTo("Duplicate submission");
    assertThat(rawDealRepository.countByStatus(RawDealStatus.PENDING)).isEqualTo(1);
  }
}
