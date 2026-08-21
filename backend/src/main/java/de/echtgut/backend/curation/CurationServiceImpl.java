package de.echtgut.backend.curation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
}
