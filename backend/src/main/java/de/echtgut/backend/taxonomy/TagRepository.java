package de.echtgut.backend.taxonomy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for managing {@link Tag} persistence operations. */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

  /**
   * Finds a tag by its unique slug identifier.
   *
   * @param slug Tag slug string.
   * @return Optional containing tag if found.
   */
  Optional<Tag> findBySlug(String slug);

  /**
   * Returns list of tags filtered by retirement status, ordered by name.
   *
   * @param isRetired Retirement flag.
   * @return List of matching tags.
   */
  List<Tag> findByIsRetiredOrderByNameAsc(boolean isRetired);

  /**
   * Returns all tags ordered by name.
   *
   * @return List of all tags.
   */
  List<Tag> findAllByOrderByNameAsc();

  /**
   * Finds all tags matching given collection of slugs.
   *
   * @param slugs Collection of tag slugs.
   * @return List of matching tags.
   */
  List<Tag> findBySlugIn(List<String> slugs);
}
