package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.ExperienceDetailDto;
import de.echtgut.backend.catalog.dto.ExperienceSummaryDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public visitor REST controller serving pristine curated experience catalog listings, detail views,
 * and affiliate click conversion tracking.
 */
@RestController
@RequestMapping
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
  @GetMapping("/api/experiences")
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
  @GetMapping("/api/experiences/{slug}")
  public ResponseEntity<ExperienceDetailDto> getExperienceBySlug(@PathVariable("slug") String slug) {
    ExperienceDetailDto detail = catalogService.getExperienceBySlug(slug);
    return ResponseEntity.ok(detail);
  }

  /**
   * Records a visitor click event and returns destination redirect URL for conversion tracking (FR-6.1).
   *
   * @param id UUID experience identifier.
   * @param request HttpServletRequest for Referer and User-Agent extraction.
   * @return Map containing redirect URL payload.
   */
  @PostMapping("/api/track/click/{id}")
  public ResponseEntity<Map<String, String>> trackClick(
      @PathVariable("id") UUID id, HttpServletRequest request) {
    String referrer = request.getHeader("Referer");
    String userAgent = request.getHeader("User-Agent");

    String redirectUrl = catalogService.recordClickAndGetRedirectUrl(id, referrer, userAgent);
    return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
  }
}
