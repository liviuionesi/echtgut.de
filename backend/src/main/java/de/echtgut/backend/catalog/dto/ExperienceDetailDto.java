package de.echtgut.backend.catalog.dto;

import de.echtgut.backend.curation.CuratedExperience;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO record representing full detail payload for a public curated experience.
 *
 * @param id Unique experience UUID.
 * @param slug URL routing slug.
 * @param title Editorial title.
 * @param description Full editorial narrative.
 * @param heroImageUrl Hero image URL.
 * @param address Location address.
 * @param lat Latitude coordinate.
 * @param lng Longitude coordinate.
 * @param affiliateUrl Optional network affiliate booking link.
 * @param bookingContact Optional direct booking contact fallback.
 * @param publishedAt Publication timestamp.
 * @param tags List of assigned tag slugs.
 */
public record ExperienceDetailDto(
    UUID id,
    String slug,
    String title,
    String description,
    String heroImageUrl,
    String address,
    BigDecimal lat,
    BigDecimal lng,
    String affiliateUrl,
    String bookingContact,
    OffsetDateTime publishedAt,
    List<String> tags) {

  /**
   * Factory method mapping entity and associated tag slugs to detail DTO.
   *
   * @param entity CuratedExperience entity.
   * @param tagSlugs List of assigned tag slugs.
   * @return ExperienceDetailDto record.
   */
  public static ExperienceDetailDto fromEntity(CuratedExperience entity, List<String> tagSlugs) {
    return new ExperienceDetailDto(
        entity.getId(),
        entity.getSlug(),
        entity.getEditorialTitle(),
        entity.getEditorialDescription(),
        entity.getHeroImageUrl(),
        entity.getAddress(),
        entity.getLat(),
        entity.getLng(),
        entity.getAffiliateUrl(),
        entity.getBookingContact(),
        entity.getCreatedAt(),
        tagSlugs != null ? tagSlugs : List.of());
  }
}
