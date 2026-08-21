package de.echtgut.backend.curation;

import java.util.UUID;

/**
 * Service interface managing curator review workflows.
 *
 * <p>Handles inspection of pending raw deals, rejection with feedback, and promotion into the
 * curated_experiences public table.
 */
public interface CurationService {

  /**
   * Rejects a pending raw deal.
   *
   * @param rawDealId Unique identifier of the deal to reject.
   * @param reason Explanation for rejection.
   */
  void rejectDeal(UUID rawDealId, String reason);
}
