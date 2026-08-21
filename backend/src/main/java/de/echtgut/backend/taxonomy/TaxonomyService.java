package de.echtgut.backend.taxonomy;

/** Service interface managing experience taxonomy tags and categories. */
public interface TaxonomyService {

  /**
   * Health status ping for taxonomy.
   *
   * @return A status message indicating taxonomy readiness.
   */
  String getTaxonomyStatus();
}
