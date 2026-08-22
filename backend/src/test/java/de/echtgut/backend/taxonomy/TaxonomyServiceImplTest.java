package de.echtgut.backend.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TaxonomyServiceImpl} covering the public read-only tag catalog. There is no
 * create/retire coverage here — that curator-facing workflow was removed with the automated
 * aggregation pivot (ADR-003); tags are seeded once via Flyway.
 */
class TaxonomyServiceImplTest {

  private TagRepository tagRepository;
  private TaxonomyServiceImpl service;

  @BeforeEach
  void setUp() {
    tagRepository = mock(TagRepository.class);
    service = new TaxonomyServiceImpl(tagRepository);
  }

  @Test
  @DisplayName("1. Returns status message")
  void testGetTaxonomyStatus() {
    assertThat(service.getTaxonomyStatus()).isEqualTo("Taxonomy service operational");
  }

  @Test
  @DisplayName("2. Fetches active tags ordered by name")
  void testGetAllTagsActiveOnly() {
    Tag tag =
        Tag.builder()
            .id(UUID.randomUUID())
            .slug("auszeit")
            .name("Auszeit")
            .isRetired(false)
            .build();
    when(tagRepository.findByIsRetiredOrderByNameAsc(false)).thenReturn(List.of(tag));

    List<TagDto> dtos = service.getAllTags(false);

    assertThat(dtos).hasSize(1);
    assertThat(dtos.get(0).slug()).isEqualTo("auszeit");
  }

  @Test
  @DisplayName("3. Fetches all tags including retired when requested")
  void testGetAllTagsIncludeRetired() {
    Tag active =
        Tag.builder()
            .id(UUID.randomUUID())
            .slug("auszeit")
            .name("Auszeit")
            .isRetired(false)
            .build();
    Tag retired =
        Tag.builder().id(UUID.randomUUID()).slug("old").name("Old").isRetired(true).build();
    when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of(active, retired));

    List<TagDto> dtos = service.getAllTags(true);

    assertThat(dtos).hasSize(2);
  }
}
