package de.echtgut.backend.curation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity mapping the public pristine table {@code curated_experiences}.
 *
 * <p>Stores human-reviewed, hand-curated experiences published to the visitor site.
 */
@Entity
@Table(name = "curated_experiences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuratedExperience {

  /** Unique primary key identifier. */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** Traceability foreign key to original raw deal in staging, if applicable. */
  @Column(name = "raw_deal_id")
  private UUID rawDealId;

  /** Unique URL-friendly slug used for frontend SSG/ISR routing. */
  @Column(name = "slug", nullable = false, unique = true)
  private String slug;

  /** Pristine human-written editorial title. */
  @Column(name = "editorial_title", nullable = false)
  private String editorialTitle;

  /** Pristine human-written editorial description. */
  @Column(name = "editorial_description", nullable = false, columnDefinition = "TEXT")
  private String editorialDescription;

  /** Validated hero image URL. */
  @Column(name = "hero_image_url", nullable = false, columnDefinition = "TEXT")
  private String heroImageUrl;

  /** Validated physical address. */
  @Column(name = "address", nullable = false)
  private String address;

  /** Validated latitude coordinate. */
  @Column(name = "lat", nullable = false, precision = 9, scale = 6)
  private BigDecimal lat;

  /** Validated longitude coordinate. */
  @Column(name = "lng", nullable = false, precision = 9, scale = 6)
  private BigDecimal lng;

  /** Affiliate link URL, if monetized via network. */
  @Column(name = "affiliate_url", columnDefinition = "TEXT")
  private String affiliateUrl;

  /** Direct booking phone/contact info fallback when no affiliate link exists. */
  @Column(name = "booking_contact")
  private String bookingContact;

  /** Internal notes for curators (not rendered on public site). */
  @Column(name = "curator_notes", columnDefinition = "TEXT")
  private String curatorNotes;

  /** Publication flag enabling unpublishing without record deletion. */
  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean isPublished = true;

  /** Timestamp when experience was created/promoted. */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  /** Timestamp when experience was last edited. */
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
