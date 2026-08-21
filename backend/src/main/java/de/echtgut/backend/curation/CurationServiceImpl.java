package de.echtgut.backend.curation;

import de.echtgut.backend.curation.dto.PromoteDealRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Production implementation of {@link CurationService}. */
@Service
@RequiredArgsConstructor
public class CurationServiceImpl implements CurationService {

  private final RawDealRepository rawDealRepository;
  private final CuratedExperienceRepository curatedExperienceRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<RawDeal> getNextPendingDeal() {
    return rawDealRepository.findFirstByStatusOrderByIngestedAtAsc(RawDealStatus.PENDING);
  }

  @Override
  @Transactional
  public RawDeal rejectDeal(UUID rawDealId, String reason) {
    RawDeal deal =
        rawDealRepository
            .findById(rawDealId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Raw deal not found with ID: " + rawDealId));

    deal.setStatus(RawDealStatus.REJECTED);
    deal.setRejectionReason(reason);
    deal.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
    return rawDealRepository.save(deal);
  }

  @Override
  @Transactional
  public CuratedExperience promoteDeal(UUID rawDealId, PromoteDealRequest request) {
    RawDeal rawDeal =
        rawDealRepository
            .findById(rawDealId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Raw deal not found with ID: " + rawDealId));

    // FR-3.5 validation invariants enforced by @Valid on DTO, double check non-blank
    if (request.editorialTitle().isBlank()
        || request.editorialDescription().isBlank()
        || request.heroImageUrl().isBlank()
        || request.address().isBlank()
        || request.lat() == null
        || request.lng() == null) {
      throw new IllegalArgumentException(
          "Quality invariant violation: title, description, hero image URL, address, and coordinates are required");
    }

    String slug =
        (request.slug() != null && !request.slug().isBlank())
            ? request.slug().toLowerCase(Locale.ROOT)
            : generateSlug(request.editorialTitle());

    // FR-3.4 Upsert keyed by raw_deal_id
    Optional<CuratedExperience> existingOpt =
        curatedExperienceRepository.findByRawDealId(rawDealId);

    CuratedExperience experience;
    if (existingOpt.isPresent()) {
      experience = existingOpt.get();
      experience.setSlug(slug);
      experience.setEditorialTitle(request.editorialTitle());
      experience.setEditorialDescription(request.editorialDescription());
      experience.setHeroImageUrl(request.heroImageUrl());
      experience.setAddress(request.address());
      experience.setLat(request.lat());
      experience.setLng(request.lng());
      experience.setAffiliateUrl(request.affiliateUrl());
      experience.setBookingContact(request.bookingContact());
      experience.setCuratorNotes(request.curatorNotes());
      if (request.isPublished() != null) {
        experience.setPublished(request.isPublished());
      }
    } else {
      experience =
          CuratedExperience.builder()
              .rawDealId(rawDealId)
              .slug(slug)
              .editorialTitle(request.editorialTitle())
              .editorialDescription(request.editorialDescription())
              .heroImageUrl(request.heroImageUrl())
              .address(request.address())
              .lat(request.lat())
              .lng(request.lng())
              .affiliateUrl(request.affiliateUrl())
              .bookingContact(request.bookingContact())
              .curatorNotes(request.curatorNotes())
              .isPublished(request.isPublished() == null || request.isPublished())
              .build();
    }

    CuratedExperience saved = curatedExperienceRepository.save(experience);

    rawDeal.setStatus(RawDealStatus.PROMOTED);
    rawDeal.setPromotedExperienceId(saved.getId());
    rawDeal.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
    rawDealRepository.save(rawDeal);

    return saved;
  }

  private String generateSlug(String title) {
    return title
        .toLowerCase(Locale.GERMAN)
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("(^-)|(-$)", "");
  }
}
