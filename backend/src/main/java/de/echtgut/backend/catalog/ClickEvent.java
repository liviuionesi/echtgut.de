package de.echtgut.backend.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity recording visitor click events on experience booking links for conversion tracking.
 */
@Entity
@Table(name = "click_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "experience_id", nullable = false)
  private UUID experienceId;

  @Column(name = "clicked_at", nullable = false, updatable = false)
  private OffsetDateTime clickedAt;

  @Column(name = "referrer_url")
  private String referrerUrl;

  @Column(name = "user_agent")
  private String userAgent;

  /** Lifecycle callback setting UTC timestamp prior to persistence. */
  @PrePersist
  protected void onCreate() {
    if (clickedAt == null) {
      clickedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
  }
}
