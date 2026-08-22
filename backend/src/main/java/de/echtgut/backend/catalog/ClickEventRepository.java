package de.echtgut.backend.catalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for managing {@link ClickEvent} persistence. */
public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {

  /**
   * Finds all click events associated with a given experience.
   *
   * @param experienceId UUID experience identifier.
   * @return List of matching ClickEvents.
   */
  List<ClickEvent> findByExperienceId(UUID experienceId);
}
