package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.ExperienceDetailDto;
import de.echtgut.backend.catalog.dto.ExperienceSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public visitor REST controller serving pristine curated experience catalog listings and detail views.
 */
@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class PublicCatalogController {

  private final CatalogService catalogService;

  /**
   * Retrieves a paginated, filterable collection of published curated experiences.
   *
   * @param tag Optional tag slug filter.
   * @param query Optional search text query filter.
   * @param page Page index (0-based, default 0).
   * @param size Page size (default 20).
   * @return Page of {@link ExperienceSummaryDto}.
   */
  @GetMapping
  public ResponseEntity<Page<ExperienceSummaryDto>> getExperiences(
      @RequestParam(name = "tag", required = false) String tag,
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size) {
    Page<ExperienceSummaryDto> experiences =
        catalogService.getPublishedExperiences(tag, query, PageRequest.of(page, size));
    return ResponseEntity.ok(experiences);
  }

  /**
   * Retrieves a single published curated experience by slug.
   *
   * @param slug Unique URL routing slug.
   * @return {@link ExperienceDetailDto} payload.
   */
  @GetMapping("/{slug}")
  public ResponseEntity<ExperienceDetailDto> getExperienceBySlug(@PathVariable("slug") String slug) {
    ExperienceDetailDto detail = catalogService.getExperienceBySlug(slug);
    return ResponseEntity.ok(detail);
  }
}
