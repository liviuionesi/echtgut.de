package de.echtgut.backend.ingestion;

import de.echtgut.backend.curation.RawDeal;
import java.util.List;

/**
 * Common adapter interface for external raw deal sources.
 *
 * <p>Implementations fetch and map external partner feeds (e.g., OSM POIs, affiliate networks, or
 * scraped sources) into pending raw deals in the staging database table.
 */
public interface RawDealSource {

  /**
   * Gets the unique identifier for this deal source (e.g., "OSM", "AFFILIATE_NETWORK", "SEED").
   *
   * @return The string source identifier.
   */
  String getSourceId();

  /**
   * Fetches candidate raw deals from the source without persisting them directly.
   *
   * @return List of candidate raw deals.
   */
  List<RawDeal> fetchCandidateDeals();

  /** Triggers a scheduled ingestion cycle for this deal source. */
  void fetchAndIngest();
}
