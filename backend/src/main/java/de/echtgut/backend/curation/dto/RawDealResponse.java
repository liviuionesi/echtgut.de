package de.echtgut.backend.curation.dto;

import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST API DTO response representation of a {@link RawDeal}.
 *
 * @param id Unique identifier.
 * @param source Deal source identifier.
 * @param sourceRef Source-native unique reference.
 * @param rawTitle Unformatted raw title.
 * @param rawDescription Unformatted raw description.
 * @param rawImageUrl Raw image URL.
 * @param locationText Unverified location text.
 * @param lat Latitude coordinate.
 * @param lng Longitude coordinate.
 * @param priceHint Free-form price string.
 * @param status State machine review status.
 * @param rejectionReason Optional rejection explanation.
 * @param submittedBy Submitter contact info.
 * @param promotedExperienceId Promoted experience ID if approved.
 * @param ingestedAt Timestamp when ingested.
 * @param reviewedAt Timestamp when reviewed.
 */
public record RawDealResponse(
    UUID id,
    String source,
    String sourceRef,
    String rawTitle,
    String rawDescription,
    String rawImageUrl,
    String locationText,
    BigDecimal lat,
    BigDecimal lng,
    String priceHint,
    RawDealStatus status,
    String rejectionReason,
    String submittedBy,
    UUID promotedExperienceId,
    OffsetDateTime ingestedAt,
    OffsetDateTime reviewedAt) {

  /**
   * Factory method mapping a domain {@link RawDeal} entity to a REST response DTO.
   *
   * @param entity Domain entity.
   * @return Mapped response DTO.
   */
  public static RawDealResponse fromEntity(RawDeal entity) {
    return new RawDealResponse(
        entity.getId(),
        entity.getSource(),
        entity.getSourceRef(),
        entity.getRawTitle(),
        entity.getRawDescription(),
        entity.getRawImageUrl(),
        entity.getLocationText(),
        entity.getLat(),
        entity.getLng(),
        entity.getPriceHint(),
        entity.getStatus(),
        entity.getRejectionReason(),
        entity.getSubmittedBy(),
        entity.getPromotedExperienceId(),
        entity.getIngestedAt(),
        entity.getReviewedAt());
  }
}
