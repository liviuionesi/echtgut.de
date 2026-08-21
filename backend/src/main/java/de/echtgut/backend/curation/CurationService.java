package de.echtgut.backend.curation;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface managing curator review workflows.
 *
 * <p>Handles inspection of pending raw deals, rejection with feedback, and promotion into the
 * curated_experiences public table.
 */
public interface CurationService {

  /**
   * Retrieves the next unreviewed raw deal in the pending queue (FIFO order).
   *
   * @return Optional containing the next pending raw deal, or empty if queue is empty.
   */
  Optional<RawDeal> getNextPendingDeal();

  /**
   * Rejects a pending raw deal with an optional curator reason.
   *
   * @param rawDealId Unique identifier of the deal to reject.
   * @param reason Explanation for rejection.
   * @return Updated RawDeal entity.
   */
  RawDeal rejectDeal(UUID rawDealId, String reason);

  /**
   * Promotes a raw candidate deal into a curated public experience with validation and upserting.
   *
   * @param rawDealId Raw deal candidate UUID.
   * @param request Validated curator promotion request details.
   * @return Saved or updated CuratedExperience entity.
   */
  CuratedExperience promoteDeal(UUID rawDealId, de.echtgut.backend.curation.dto.PromoteDealRequest request);
}
