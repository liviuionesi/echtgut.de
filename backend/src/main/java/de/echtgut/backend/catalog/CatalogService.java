package de.echtgut.backend.catalog;

/**
 * Service interface backing public visitor queries and SSG/ISR site generation.
 *
 * <p>Provides filterable listing queries and experience detail lookups.
 */
public interface CatalogService {

  /**
   * Health status ping for catalog queries.
   *
   * @return A status message indicating catalog readiness.
   */
  String getCatalogStatus();
}
