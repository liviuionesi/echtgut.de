package de.echtgut.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit test suite for {@link SeedRawDealSource}. */
class SeedRawDealSourceTest {

  private final SeedRawDealSource seedRawDealSource = new SeedRawDealSource();

  @Test
  @DisplayName("1. Given SeedRawDealSource, when getSourceId called, returns 'SEED'")
  void testGetSourceId() {
    assertThat(seedRawDealSource.getSourceId()).isEqualTo("SEED");
  }

  @Test
  @DisplayName(
      "2. Given SeedRawDealSource, when fetchCandidateDeals called, returns non-empty valid sample deals")
  void testFetchCandidateDeals() {
    List<RawDeal> deals = seedRawDealSource.fetchCandidateDeals();

    assertThat(deals)
        .hasSizeGreaterThanOrEqualTo(3)
        .allSatisfy(
            deal ->
                assertThat(deal)
                    .extracting(
                        RawDeal::getSource,
                        RawDeal::getSourceRef,
                        RawDeal::getRawTitle,
                        RawDeal::getStatus)
                    .satisfies(
                        tuple -> {
                          assertThat(tuple.get(0)).isEqualTo("SEED");
                          assertThat((String) tuple.get(1)).isNotBlank();
                          assertThat((String) tuple.get(2)).isNotBlank();
                          assertThat(tuple.get(3)).isEqualTo(RawDealStatus.PENDING);
                        }));
  }
}
