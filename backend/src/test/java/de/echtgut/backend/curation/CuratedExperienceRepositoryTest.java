package de.echtgut.backend.curation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test suite for {@link CuratedExperienceRepository}.
 *
 * <p>Verifies persistence, slug lookup, unique constraint enforcement, and published status
 * filtering.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CuratedExperienceRepositoryTest {

  @Autowired private CuratedExperienceRepository curatedExperienceRepository;
  @Autowired private RawDealRepository rawDealRepository;

  /** Tests saving and retrieving a curated experience by slug. */
  @Test
  @DisplayName("1. Given valid experience, when saved, then retrieves entity by slug")
  void testSaveAndFindBySlug() {
    // 1. Given a saved raw deal reference
    RawDeal rawDeal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("MANUAL")
                .sourceRef("raw-ref-201")
                .rawTitle("Raw Title")
                .build());

    // 2. Given curated experience linked to raw deal
    CuratedExperience experience =
        CuratedExperience.builder()
            .rawDealId(rawDeal.getId())
            .slug("bio-baecker-kreuzberg")
            .editorialTitle("Handwerklicher Bio-Bäcker in Kreuzberg")
            .editorialDescription("Traditionelles Sauerteigbrot aus regionalem Bio-Mehl.")
            .heroImageUrl("https://images.echtgut.de/bio-baecker.jpg")
            .address("Oranienstraße 45, 10999 Berlin")
            .lat(new BigDecimal("52.501234"))
            .lng(new BigDecimal("13.417890"))
            .curatorNotes("Direct contact verified")
            .isPublished(true)
            .build();

    // 3. When saving entity
    CuratedExperience saved = curatedExperienceRepository.saveAndFlush(experience);

    // 4. Then entity is persisted and findable by slug
    assertThat(saved.getId()).isNotNull();
    Optional<CuratedExperience> found =
        curatedExperienceRepository.findBySlug("bio-baecker-kreuzberg");
    assertThat(found).isPresent();
    assertThat(found.get().getEditorialTitle())
        .isEqualTo("Handwerklicher Bio-Bäcker in Kreuzberg");
    assertThat(found.get().getRawDealId()).isEqualTo(rawDeal.getId());
  }

  /** Tests slug uniqueness constraint violation. */
  @Test
  @DisplayName("2. Given duplicate slug, when saved, then throws DataIntegrityViolationException")
  void testDuplicateSlugThrowsException() {
    // 1. Given an existing experience with slug
    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("unique-slug")
            .editorialTitle("First Deal")
            .editorialDescription("Description")
            .heroImageUrl("https://img.com/1.jpg")
            .address("Address 1")
            .lat(new BigDecimal("52.500000"))
            .lng(new BigDecimal("13.400000"))
            .build());

    // 2. Given a second experience with duplicate slug
    CuratedExperience duplicate =
        CuratedExperience.builder()
            .slug("unique-slug")
            .editorialTitle("Second Deal")
            .editorialDescription("Description")
            .heroImageUrl("https://img.com/2.jpg")
            .address("Address 2")
            .lat(new BigDecimal("52.510000"))
            .lng(new BigDecimal("13.410000"))
            .build();

    // 3. When saving duplicate, then exception is thrown
    assertThatThrownBy(() -> curatedExperienceRepository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  /** Tests filtering published experiences. */
  @Test
  @DisplayName("3. Given published and unpublished experiences, when queried, returns active only")
  void testFindByIsPublishedTrue() {
    // 1. Given published and draft experiences
    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("published-exp")
            .editorialTitle("Published")
            .editorialDescription("Desc")
            .heroImageUrl("https://img.com/p.jpg")
            .address("Address P")
            .lat(new BigDecimal("52.500000"))
            .lng(new BigDecimal("13.400000"))
            .isPublished(true)
            .build());

    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("unpublished-exp")
            .editorialTitle("Unpublished")
            .editorialDescription("Desc")
            .heroImageUrl("https://img.com/u.jpg")
            .address("Address U")
            .lat(new BigDecimal("52.500000"))
            .lng(new BigDecimal("13.400000"))
            .isPublished(false)
            .build());

    // 2. When querying published experiences
    List<CuratedExperience> active = curatedExperienceRepository.findByIsPublishedTrue();

    // 3. Then only published entity is returned
    assertThat(active).hasSize(1);
    assertThat(active.get(0).getSlug()).isEqualTo("published-exp");
  }
}
