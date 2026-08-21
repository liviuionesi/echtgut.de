package de.echtgut.backend.curation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA Entity mapping the staging database table {@code raw_deals}.
 *
 * <p>Stores unverified deal candidates ingested from external feeds, scrapes, or community
 * submissions prior to human curator review.
 */
@Entity
@Table(name = "raw_deals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawDeal {

  /** Unique primary key identifier for the raw deal. */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** Source identifier (e.g., OSM, AFFILIATE_NETWORK, SCRAPE_SITE, COMMUNITY, MANUAL). */
  @Column(name = "source", nullable = false)
  private String source;

  /** Source-native unique identifier or URL used for deduplication. */
  @Column(name = "source_ref", nullable = false, unique = true)
  private String sourceRef;

  /** Raw unformatted title as ingested from source. */
  @Column(name = "raw_title", nullable = false)
  private String rawTitle;

  /** Raw description text as ingested. */
  @Column(name = "raw_description", columnDefinition = "TEXT")
  private String rawDescription;

  /** Image URL as provided by source (may be null or replaced by curator). */
  @Column(name = "raw_image_url", columnDefinition = "TEXT")
  private String rawImageUrl;

  /** Unverified address or location text. */
  @Column(name = "location_text")
  private String locationText;

  /** Latitude coordinate. */
  @Column(name = "lat", precision = 9, scale = 6)
  private BigDecimal lat;

  /** Longitude coordinate. */
  @Column(name = "lng", precision = 9, scale = 6)
  private BigDecimal lng;

  /** Free-form price hint or string as provided by feed. */
  @Column(name = "price_hint")
  private String priceHint;

  /** Current state machine review status. */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private RawDealStatus status = RawDealStatus.PENDING;

  /** Explanation note if the deal was rejected. */
  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  /** Submitter contact info for community submissions. */
  @Column(name = "submitted_by")
  private String submittedBy;

  /** Foreign key pointing to promoted curated experience, if approved. */
  @Column(name = "promoted_experience_id")
  private UUID promotedExperienceId;

  /** Timestamp when the record was ingested into staging. */
  @CreationTimestamp
  @Column(name = "ingested_at", nullable = false, updatable = false)
  private OffsetDateTime ingestedAt;

  /** Timestamp when curator decision was finalized. */
  @Column(name = "reviewed_at")
  private OffsetDateTime reviewedAt;
}
