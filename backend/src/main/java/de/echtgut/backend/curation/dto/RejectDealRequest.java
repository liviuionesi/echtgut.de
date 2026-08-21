package de.echtgut.backend.curation.dto;

/**
 * DTO request payload for rejecting a raw deal candidate.
 *
 * @param reason Explanation for why the candidate deal was rejected by the curator.
 */
public record RejectDealRequest(String reason) {}
