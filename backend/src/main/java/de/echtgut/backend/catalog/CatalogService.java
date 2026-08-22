package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.ExperienceDetailDto;
import de.echtgut.backend.catalog.dto.ExperienceSummaryDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface backing public visitor queries and SSG/ISR site generation.
 *
 * <p>Provides filterable listing queries, experience detail lookups, and click tracking.
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

  /**
   * Records a visitor click event and determines destination redirect URL.
   *
   * @param experienceId UUID experience identifier.
   * @param referrer HTTP Referer header.
   * @param userAgent HTTP User-Agent header.
   * @return Destination redirect URL.
   */
  String recordClickAndGetRedirectUrl(UUID experienceId, String referrer, String userAgent);
}
