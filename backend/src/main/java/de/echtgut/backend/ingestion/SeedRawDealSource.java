package de.echtgut.backend.ingestion;

import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Seed data adapter implementation of {@link RawDealSource}.
 *
 * <p>Provides initial seed candidate deals for local development, staging, and automated testing
 * without requiring live external network APIs.
 */
@Component
public class SeedRawDealSource implements RawDealSource {

  public static final String SOURCE_ID = "SEED";

  @Override
  public String getSourceId() {
    return SOURCE_ID;
  }

  /**
   * Fetches sample candidate raw deals representing authentic German local experiences.
   *
   * @return List of raw deal candidates.
   */
  public List<RawDeal> fetchCandidateDeals() {
    return List.of(
        RawDeal.builder()
            .source(SOURCE_ID)
            .sourceRef("seed-deal-001")
            .rawTitle("Bio-Bäckerei Brotgarten — Frisches Holzofenbrot")
            .rawDescription("Traditionelles Sauerteigbrot aus 100% regionalem Bio-Mehl.")
            .locationText("Kastanienallee 12, 10435 Berlin")
            .lat(new BigDecimal("52.535123"))
            .lng(new BigDecimal("13.408456"))
            .priceHint("ab €4.80")
            .status(RawDealStatus.PENDING)
            .build(),
        RawDeal.builder()
            .source(SOURCE_ID)
            .sourceRef("seed-deal-002")
            .rawTitle("Töpferkunst Keramik-Workshop in Hamburg")
            .rawDescription("2-stündiger Einführungskurs in Handdrehen und Glasieren.")
            .locationText("Speicherstadt 8, 20457 Hamburg")
            .lat(new BigDecimal("53.543210"))
            .lng(new BigDecimal("9.991234"))
            .priceHint("€45.00 / Person")
            .status(RawDealStatus.PENDING)
            .build(),
        RawDeal.builder()
            .source(SOURCE_ID)
            .sourceRef("seed-deal-003")
            .rawTitle("Bodensee Segeltour & Weinverkostung")
            .rawDescription("Geführte Abend-Segeltour mit regionalen Qualitätsweinen.")
            .locationText("Hafenstraße 1, 78462 Konstanz")
            .lat(new BigDecimal("47.659123"))
            .lng(new BigDecimal("9.176456"))
            .priceHint("€69.00 / Person")
            .status(RawDealStatus.PENDING)
            .build());
  }

  @Override
  public void fetchAndIngest() {
    // Ingestion pipeline logic is handled by the IngestionService in Task #22.
  }
}
