package de.echtgut.backend.catalog.dto;

import de.echtgut.backend.curation.CuratedExperience;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO record representing a summary view of a published curated experience for marketplace catalog listings.
 *
 * @param id Unique experience UUID.
 * @param slug URL routing slug.
 * @param title Editorial title.
 * @param description Editorial description summary.
 * @param heroImageUrl Hero image URL.
 * @param address Location address.
 * @param lat Latitude coordinate.
 * @param lng Longitude coordinate.
 * @param publishedAt Publication timestamp.
 * @param tags List of assigned tag slugs.
 */
public record ExperienceSummaryDto(
    UUID id,
    String slug,
    String title,
    String description,
    String heroImageUrl,
    String address,
    BigDecimal lat,
    BigDecimal lng,
    OffsetDateTime publishedAt,
    List<String> tags) {

  /**
   * Factory method mapping entity and associated tag slugs to summary DTO.
   *
   * @param entity CuratedExperience entity.
   * @param tagSlugs List of assigned tag slugs.
   * @return ExperienceSummaryDto record.
   */
  public static ExperienceSummaryDto fromEntity(CuratedExperience entity, List<String> tagSlugs) {
    return new ExperienceSummaryDto(
        entity.getId(),
        entity.getSlug(),
        entity.getEditorialTitle(),
        entity.getEditorialDescription(),
        entity.getHeroImageUrl(),
        entity.getAddress(),
        entity.getLat(),
        entity.getLng(),
        entity.getCreatedAt(),
        tagSlugs != null ? tagSlugs : List.of());
  }
}
