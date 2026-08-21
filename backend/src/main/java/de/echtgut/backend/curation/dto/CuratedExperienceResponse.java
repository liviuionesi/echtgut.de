package de.echtgut.backend.curation.dto;

import de.echtgut.backend.curation.CuratedExperience;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST API response representation of a {@link CuratedExperience}.
 *
 * @param id Unique identifier.
 * @param rawDealId Foreign key to original raw deal.
 * @param slug Unique URL routing slug.
 * @param editorialTitle Editorial title.
 * @param editorialDescription Editorial description.
 * @param heroImageUrl Hero image URL.
 * @param address Physical address.
 * @param lat Latitude coordinate.
 * @param lng Longitude coordinate.
 * @param affiliateUrl Optional affiliate link.
 * @param bookingContact Optional booking contact info.
 * @param curatorNotes Optional curator internal notes.
 * @param isPublished Published status flag.
 * @param createdAt Creation timestamp.
 * @param updatedAt Update timestamp.
 */
public record CuratedExperienceResponse(
    UUID id,
    UUID rawDealId,
    String slug,
    String editorialTitle,
    String editorialDescription,
    String heroImageUrl,
    String address,
    BigDecimal lat,
    BigDecimal lng,
    String affiliateUrl,
    String bookingContact,
    String curatorNotes,
    boolean isPublished,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  /**
   * Factory method mapping domain entity to REST response DTO.
   *
   * @param entity Domain entity.
   * @return Response DTO.
   */
  public static CuratedExperienceResponse fromEntity(CuratedExperience entity) {
    return new CuratedExperienceResponse(
        entity.getId(),
        entity.getRawDealId(),
        entity.getSlug(),
        entity.getEditorialTitle(),
        entity.getEditorialDescription(),
        entity.getHeroImageUrl(),
        entity.getAddress(),
        entity.getLat(),
        entity.getLng(),
        entity.getAffiliateUrl(),
        entity.getBookingContact(),
        entity.getCuratorNotes(),
        entity.isPublished(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
