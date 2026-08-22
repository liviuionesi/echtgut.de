package de.echtgut.backend.taxonomy.dto;

import de.echtgut.backend.taxonomy.Tag;
import java.util.UUID;

/**
 * DTO representing a taxonomy tag.
 *
 * @param id Unique UUID identifier.
 * @param slug URL-friendly slug.
 * @param name Display label.
 * @param category Tag category (e.g. MOOD).
 * @param isRetired Whether tag is retired from selection.
 */
public record TagDto(UUID id, String slug, String name, String category, boolean isRetired) {

  /**
   * Static factory creating DTO from entity.
   *
   * @param entity Tag JPA entity.
   * @return TagDto record.
   */
  public static TagDto fromEntity(Tag entity) {
    return new TagDto(
        entity.getId(),
        entity.getSlug(),
        entity.getName(),
        entity.getCategory(),
        entity.isRetired());
  }
}
