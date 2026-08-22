package de.echtgut.backend.taxonomy;

import de.echtgut.backend.taxonomy.dto.CreateTagRequest;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import java.util.UUID;

/** Service interface managing experience taxonomy tags and categories. */
public interface TaxonomyService {

  /**
   * Health status ping for taxonomy.
   *
   * @return A status message indicating taxonomy readiness.
   */
  String getTaxonomyStatus();

  /**
   * Retrieves all taxonomy tags.
   *
   * @param includeRetired Whether to include retired tags in result.
   * @return List of {@link TagDto}.
   */
  List<TagDto> getAllTags(boolean includeRetired);

  /**
   * Creates a new taxonomy tag.
   *
   * @param request {@link CreateTagRequest} payload.
   * @return Created {@link TagDto}.
   */
  TagDto createTag(CreateTagRequest request);

  /**
   * Retires an existing taxonomy tag.
   *
   * @param tagId UUID identifier of tag to retire.
   * @return Retired {@link TagDto}.
   */
  TagDto retireTag(UUID tagId);
}
