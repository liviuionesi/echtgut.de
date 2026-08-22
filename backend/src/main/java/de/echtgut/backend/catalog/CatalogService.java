package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.ExperienceDetailDto;
import de.echtgut.backend.catalog.dto.ExperienceSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  /**
   * Retrieves a paginated list of published curated experiences, optionally filtered by tag or query.
   *
   * @param tag Optional tag slug filter.
   * @param query Optional search text query filter.
   * @param pageable Pagination configuration.
   * @return Page of {@link ExperienceSummaryDto}.
   */
  Page<ExperienceSummaryDto> getPublishedExperiences(String tag, String query, Pageable pageable);

  /**
   * Retrieves a published experience by its unique URL slug.
   *
   * @param slug Experience routing slug.
   * @return {@link ExperienceDetailDto} payload.
   */
  ExperienceDetailDto getExperienceBySlug(String slug);
}
