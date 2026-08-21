package de.echtgut.backend.curation;

/**
 * Enumeration representing the state machine status of a raw deal candidate.
 *
 * <p>State transitions move from {@link #PENDING} to terminal states {@link #PROMOTED} or {@link
 * #REJECTED}.
 */
public enum RawDealStatus {
  /** Candidate deal is awaiting human curator inspection and decision. */
  PENDING,

  /** Deal has been rejected by a curator with a specific reason. */
  REJECTED,

  /** Deal has been approved and promoted into a public curated experience. */
  PROMOTED
}
