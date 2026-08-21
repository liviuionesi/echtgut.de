package de.echtgut.backend.curation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for managing {@link CuratedExperience} entities. */
@Repository
public interface CuratedExperienceRepository extends JpaRepository<CuratedExperience, UUID> {

  /**
   * Finds a curated experience by its unique URL slug.
   *
   * @param slug Unique routing slug.
   * @return Optional containing matching experience.
   */
  Optional<CuratedExperience> findBySlug(String slug);

  /**
   * Finds all published curated experiences for public catalog rendering.
   *
   * @return List of active published experiences.
   */
  List<CuratedExperience> findByIsPublishedTrue();

  /**
   * Finds a curated experience by its original raw deal staging ID.
   *
   * @param rawDealId Raw deal staging UUID.
   * @return Optional containing matching experience.
   */
  Optional<CuratedExperience> findByRawDealId(UUID rawDealId);
}
