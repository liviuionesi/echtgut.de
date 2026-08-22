package de.echtgut.backend.taxonomy;

import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
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
}
