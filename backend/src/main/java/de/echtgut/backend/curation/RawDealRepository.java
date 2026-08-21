package de.echtgut.backend.curation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for managing {@link RawDeal} entities in staging. */
@Repository
public interface RawDealRepository extends JpaRepository<RawDeal, UUID> {

  /**
   * Finds a raw deal by its source-native reference for deduplication.
   *
   * @param sourceRef Unique reference from external source.
   * @return Optional containing matching deal if found.
   */
  Optional<RawDeal> findBySourceRef(String sourceRef);

  /**
   * Finds all raw deals matching a specific review status.
   *
   * @param status Review status filter.
   * @return List of matching raw deals.
   */
  List<RawDeal> findByStatus(RawDealStatus status);

  /**
   * Counts the number of raw deals in a specific review status.
   *
   * @param status Review status filter.
   * @return Total count.
   */
  long countByStatus(RawDealStatus status);
}
