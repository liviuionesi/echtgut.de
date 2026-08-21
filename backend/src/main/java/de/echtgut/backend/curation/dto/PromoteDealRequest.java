package de.echtgut.backend.curation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO request payload for promoting a raw candidate deal into a curated public experience.
 *
 * <p>Enforces FR-3.5 quality invariants: editorial title, description, hero image URL, address, and
 * coordinates are strictly required.
 *
 * @param slug Unique URL routing slug (generated if blank).
 * @param editorialTitle Pristine human-curated title.
 * @param editorialDescription Pristine human-curated description.
 * @param heroImageUrl Validated high-quality hero image URL.
 * @param address Validated physical address.
 * @param lat Validated latitude coordinate.
 * @param lng Validated longitude coordinate.
 * @param affiliateUrl Optional network affiliate link.
 * @param bookingContact Optional direct booking contact fallback.
 * @param curatorNotes Optional internal notes for curators.
 * @param isPublished Publication flag (defaults to true if null).
 */
public record PromoteDealRequest(
    String slug,
    @NotBlank(message = "Editorial title is required") String editorialTitle,
    @NotBlank(message = "Editorial description is required") String editorialDescription,
    @NotBlank(message = "Hero image URL is required") String heroImageUrl,
    @NotBlank(message = "Address is required") String address,
    @NotNull(message = "Latitude coordinate is required") BigDecimal lat,
    @NotNull(message = "Longitude coordinate is required") BigDecimal lng,
    String affiliateUrl,
    String bookingContact,
    String curatorNotes,
    Boolean isPublished) {}
