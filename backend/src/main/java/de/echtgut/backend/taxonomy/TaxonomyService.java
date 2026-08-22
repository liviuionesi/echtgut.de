package de.echtgut.backend.taxonomy;

import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;

/**
 * Service interface for the public taxonomy tag catalog. Tags are seeded via Flyway (see {@code
 * V4__create_taxonomy_tags.sql}) — there is no curator-facing create/retire workflow (Non-Goals,
 * REQUIREMENTS.md §6).
 */
public interface TaxonomyService {

  /**
   * Health status ping for taxonomy.
   *
   * @return A status message indicating taxonomy readiness.
   */
  String getTaxonomyStatus();

  /**
   * Retrieves all taxonomy tags.
   *
   * @param includeRetired Whether to include retired tags in result.
   * @return List of {@link TagDto}.
   */
  List<TagDto> getAllTags(boolean includeRetired);
}
