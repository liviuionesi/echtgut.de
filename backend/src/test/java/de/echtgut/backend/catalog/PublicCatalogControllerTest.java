package de.echtgut.backend.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.echtgut.backend.curation.CuratedExperience;
import de.echtgut.backend.curation.CuratedExperienceRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PublicCatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CuratedExperienceRepository curatedExperienceRepository;

  @BeforeEach
  void setUp() {
    curatedExperienceRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("1. Unauthenticated request to GET /api/experiences succeeds with 200 OK")
  void testGetExperiencesPublicAccess() throws Exception {
    mockMvc.perform(get("/api/experiences")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("2. Returns published experiences only")
  void testGetExperiencesPublishedOnly() throws Exception {
    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("published-spa")
            .editorialTitle("Published Spa")
            .editorialDescription("Pristine spa experience")
            .heroImageUrl("https://img.com/spa.jpg")
            .address("Spaweg 1, Berlin")
            .lat(BigDecimal.valueOf(52.52))
            .lng(BigDecimal.valueOf(13.40))
            .isPublished(true)
            .build());

    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("draft-spa")
            .editorialTitle("Draft Spa")
            .editorialDescription("Draft description")
            .heroImageUrl("https://img.com/draft.jpg")
            .address("Spaweg 2, Berlin")
            .lat(BigDecimal.valueOf(52.52))
            .lng(BigDecimal.valueOf(13.40))
            .isPublished(false)
            .build());

    mockMvc
        .perform(get("/api/experiences"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].slug").value("published-spa"));
  }

  @Test
  @DisplayName("3. GET /api/experiences/{slug} returns detail view for published experience")
  void testGetExperienceBySlugSuccess() throws Exception {
    curatedExperienceRepository.saveAndFlush(
        CuratedExperience.builder()
            .slug("bio-brotgarten-berlin")
            .editorialTitle("Bio Brotgarten")
            .editorialDescription("Fresh sourdough bread")
            .heroImageUrl("https://img.com/brot.jpg")
            .address("Kastanienallee 12, Berlin")
            .lat(BigDecimal.valueOf(52.53))
            .lng(BigDecimal.valueOf(13.40))
            .isPublished(true)
            .build());

    mockMvc
        .perform(get("/api/experiences/bio-brotgarten-berlin"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Bio Brotgarten"))
        .andExpect(jsonPath("$.address").value("Kastanienallee 12, Berlin"));
  }

  @Test
  @DisplayName("4. GET /api/experiences/{slug} returns 404 for non-existent slug")
  void testGetExperienceBySlugNotFound() throws Exception {
    mockMvc.perform(get("/api/experiences/non-existent-slug")).andExpect(status().isNotFound());
  }
}
