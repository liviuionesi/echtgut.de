package de.echtgut.backend.curation;

import de.echtgut.backend.curation.dto.RawDealResponse;
import de.echtgut.backend.curation.dto.RejectDealRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for the Curator Admin Portal.
 *
 * <p>Provides pending review queue inspection and rejection actions.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CuratorAdminController {

  private final CurationService curationService;

  /**
   * Retrieves the next pending raw deal candidate for curator review.
   *
   * @return HTTP 200 OK with {@link RawDealResponse} if deal exists, or HTTP 204 No Content if
   *     queue is empty.
   */
  @GetMapping("/pending-deals")
  public ResponseEntity<RawDealResponse> getPendingDeal() {
    Optional<RawDeal> pendingOpt = curationService.getNextPendingDeal();

    return pendingOpt
        .map(rawDeal -> ResponseEntity.ok(RawDealResponse.fromEntity(rawDeal)))
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  /**
   * Rejects a pending raw deal candidate with an optional reason.
   *
   * @param id Unique raw deal identifier.
   * @param request Reject payload containing optional reason.
   * @return HTTP 200 OK with updated {@link RawDealResponse}.
   */
  @PostMapping("/deals/{id}/reject")
  public ResponseEntity<RawDealResponse> rejectDeal(
      @PathVariable UUID id, @RequestBody(required = false) RejectDealRequest request) {
    String reason = request != null ? request.reason() : null;
    RawDeal rejected = curationService.rejectDeal(id, reason);
    return ResponseEntity.ok(RawDealResponse.fromEntity(rejected));
  }
}
