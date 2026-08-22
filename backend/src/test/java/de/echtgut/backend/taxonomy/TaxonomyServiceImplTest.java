package de.echtgut.backend.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.echtgut.backend.exception.InvalidDealOperationException;
import de.echtgut.backend.exception.ResourceNotFoundException;
import de.echtgut.backend.taxonomy.dto.CreateTagRequest;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

  @Test
  @DisplayName("4. Creates new valid tag")
  void testCreateTagSuccess() {
    CreateTagRequest req = new CreateTagRequest("Wellness & Spa", null, null);
    when(tagRepository.findBySlug("wellness-spa")).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class)))
        .thenAnswer(
            inv -> {
              Tag t = inv.getArgument(0);
              t.setId(UUID.randomUUID());
              return t;
            });

    TagDto created = service.createTag(req);

    assertThat(created.slug()).isEqualTo("wellness-spa");
    assertThat(created.name()).isEqualTo("Wellness & Spa");
  }

  @Test
  @DisplayName("5. Throws InvalidDealOperationException when creating duplicate tag slug")
  void testCreateTagDuplicateSlug() {
    CreateTagRequest req = new CreateTagRequest("Auszeit", "auszeit", null);
    when(tagRepository.findBySlug("auszeit")).thenReturn(Optional.of(new Tag()));

    assertThatThrownBy(() -> service.createTag(req))
        .isInstanceOf(InvalidDealOperationException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  @DisplayName("6. Retires existing tag by ID")
  void testRetireTagSuccess() {
    UUID tagId = UUID.randomUUID();
    Tag tag = Tag.builder().id(tagId).slug("auszeit").name("Auszeit").isRetired(false).build();
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
    when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

    TagDto retired = service.retireTag(tagId);

    assertThat(retired.isRetired()).isTrue();
    verify(tagRepository).save(tag);
  }

  @Test
  @DisplayName("7. Throws ResourceNotFoundException when retiring non-existent tag")
  void testRetireTagNotFound() {
    UUID tagId = UUID.randomUUID();
    when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.retireTag(tagId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
