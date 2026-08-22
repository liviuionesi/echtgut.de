package de.echtgut.backend.taxonomy;

import de.echtgut.backend.exception.InvalidDealOperationException;
import de.echtgut.backend.exception.ResourceNotFoundException;
import de.echtgut.backend.taxonomy.dto.CreateTagRequest;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Production implementation of {@link TaxonomyService}. */
@Service
@RequiredArgsConstructor
public class TaxonomyServiceImpl implements TaxonomyService {

  private final TagRepository tagRepository;

  @Override
  public String getTaxonomyStatus() {
    return "Taxonomy service operational";
  }

  @Override
  @Transactional(readOnly = true)
  public List<TagDto> getAllTags(boolean includeRetired) {
    // 1. Fetch tags ordered by name, optionally including retired tags
    List<Tag> tags =
        includeRetired
            ? tagRepository.findAllByOrderByNameAsc()
            : tagRepository.findByIsRetiredOrderByNameAsc(false);

    // 2. Map entity instances to DTO records
    return tags.stream().map(TagDto::fromEntity).toList();
  }

  @Override
  @Transactional
  public TagDto createTag(CreateTagRequest request) {
    // 1. Validate non-blank name requirement
    if (request.name() == null || request.name().isBlank()) {
      throw new InvalidDealOperationException("Tag name must not be blank");
    }

    // 2. Derive URL slug if not explicitly provided
    String slug =
        (request.slug() != null && !request.slug().isBlank())
            ? request.slug().toLowerCase(Locale.ROOT)
            : generateSlug(request.name());

    // 3. Ensure slug uniqueness
    if (tagRepository.findBySlug(slug).isPresent()) {
      throw new InvalidDealOperationException("Tag with slug '" + slug + "' already exists");
    }

    // 4. Build and persist new Tag entity
    Tag tag =
        Tag.builder()
            .name(request.name().trim())
            .slug(slug)
            .category(
                (request.category() != null && !request.category().isBlank())
                    ? request.category().toUpperCase(Locale.ROOT)
                    : "MOOD")
            .isRetired(false)
            .build();

    Tag saved = tagRepository.save(tag);
    return TagDto.fromEntity(saved);
  }

  @Override
  @Transactional
  public TagDto retireTag(UUID tagId) {
    // 1. Retrieve target tag entity
    Tag tag =
        tagRepository
            .findById(tagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found with ID: " + tagId));

    // 2. Update retired status flag
    tag.setRetired(true);
    Tag saved = tagRepository.save(tag);
    return TagDto.fromEntity(saved);
  }

  private String generateSlug(String name) {
    return name.toLowerCase(Locale.GERMAN)
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("ß", "ss")
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("(^-)|(-$)", "");
  }
}
