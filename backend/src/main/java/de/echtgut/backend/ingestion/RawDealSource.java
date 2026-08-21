package de.echtgut.backend.ingestion;

/**
 * Common adapter interface for external raw deal sources.
 *
 * <p>Implementations fetch and map external partner feeds (e.g., OSM POIs, affiliate networks, or
 * scraped sources) into pending raw deals in the staging database table.
 */
public interface RawDealSource {

  /**
   * Gets the unique identifier for this deal source (e.g., "OSM", "AFFILIATE_NETWORK").
   *
   * @return The string source identifier.
   */
  String getSourceId();

  /** Triggers a scheduled ingestion cycle for this deal source. */
  void fetchAndIngest();
}
