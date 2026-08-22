package de.echtgut.backend.curation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.echtgut.backend.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test suite for {@link CuratorAdminController}.
 *
 * <p>Verifies security enforcement (HTTP 401 Unauthorized for unauthenticated access) as well as
 * {@code GET /api/admin/pending-deals}, {@code POST /api/admin/deals/{id}/reject}, and {@code POST
 * /api/admin/deals/{id}/promote} with valid JWT authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CuratorAdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RawDealRepository rawDealRepository;
  @Autowired private CuratedExperienceRepository curatedExperienceRepository;
  @Autowired private JwtTokenProvider jwtTokenProvider;

  private String curatorToken;

  @BeforeEach
  void setUp() {
    curatorToken = jwtTokenProvider.generateToken("curator@echtgut.de", List.of("CURATOR"));
  }

  @Test
  @DisplayName("1. Given unauthenticated request to /api/admin/pending-deals, returns HTTP 401 Unauthorized")
  void testGetPendingDealsUnauthenticatedReturns401() throws Exception {
    mockMvc.perform(get("/api/admin/pending-deals")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("2. Given unauthenticated request to /api/admin/deals/{id}/reject, returns HTTP 401 Unauthorized")
  void testRejectDealUnauthenticatedReturns401() throws Exception {
    mockMvc
        .perform(post("/api/admin/deals/00000000-0000-0000-0000-000000000001/reject"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("3. Given unauthenticated request to /api/admin/deals/{id}/promote, returns HTTP 401 Unauthorized")
  void testPromoteDealUnauthenticatedReturns401() throws Exception {
    mockMvc
        .perform(post("/api/admin/deals/00000000-0000-0000-0000-000000000001/promote"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("4. Given no pending deals and valid CURATOR JWT, GET /api/admin/pending-deals returns HTTP 204 No Content")
  void testGetPendingDealsEmptyReturns204() throws Exception {
    rawDealRepository.deleteAllInBatch();

    mockMvc
        .perform(get("/api/admin/pending-deals").header("Authorization", "Bearer " + curatorToken))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "5. Given pending deal and valid CURATOR JWT, GET /api/admin/pending-deals returns HTTP 200 OK with deal details")
  void testGetPendingDealsReturns200() throws Exception {
    rawDealRepository.deleteAllInBatch();

    RawDeal deal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("MANUAL")
                .sourceRef("test-queue-001")
                .rawTitle("Kaffeekultur Berlin")
                .build());

    mockMvc
        .perform(get("/api/admin/pending-deals").header("Authorization", "Bearer " + curatorToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(deal.getId().toString()))
        .andExpect(jsonPath("$.rawTitle").value("Kaffeekultur Berlin"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName(
      "6. Given pending deal and valid CURATOR JWT, POST /api/admin/deals/{id}/reject updates status to REJECTED")
  void testRejectDealReturns200() throws Exception {
    rawDealRepository.deleteAllInBatch();

    RawDeal deal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("MANUAL")
                .sourceRef("test-queue-002")
                .rawTitle("Invalid Deal")
                .build());

    String payloadJson = "{\"reason\":\"Not a valid local deal\"}";

    mockMvc
        .perform(
            post("/api/admin/deals/" + deal.getId() + "/reject")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(deal.getId().toString()))
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.rejectionReason").value("Not a valid local deal"));

    RawDeal updated = rawDealRepository.findById(deal.getId()).orElseThrow();
    assertThat(updated)
        .extracting("status", "rejectionReason")
        .containsExactly(RawDealStatus.REJECTED, "Not a valid local deal");
  }

  @Test
  @DisplayName("7. Given valid payload and CURATOR JWT, POST /api/admin/deals/{id}/promote creates experience & marks PROMOTED")
  void testPromoteDealValidReturns200() throws Exception {
    rawDealRepository.deleteAllInBatch();
    curatedExperienceRepository.deleteAllInBatch();

    RawDeal deal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("SEED")
                .sourceRef("test-promote-001")
                .rawTitle("Raw Title")
                .build());

    String promoteJson =
        """
        {
          "slug": "bio-brotgarten-berlin",
          "editorialTitle": "Bio Brotgarten Berlin",
          "editorialDescription": "Frisches Sauerteigbrot aus der Holzofen-Bäckerei.",
          "heroImageUrl": "https://images.echtgut.de/brotgarten.jpg",
          "address": "Kastanienallee 12, 10435 Berlin",
          "lat": 52.535123,
          "lng": 13.408456,
          "priceHint": "ab €4.80"
        }
        """;

    mockMvc
        .perform(
            post("/api/admin/deals/" + deal.getId() + "/promote")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(promoteJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rawDealId").value(deal.getId().toString()))
        .andExpect(jsonPath("$.slug").value("bio-brotgarten-berlin"))
        .andExpect(jsonPath("$.editorialTitle").value("Bio Brotgarten Berlin"))
        .andExpect(jsonPath("$.isPublished").value(true));

    RawDeal updatedRaw = rawDealRepository.findById(deal.getId()).orElseThrow();
    assertThat(updatedRaw.getStatus()).isEqualTo(RawDealStatus.PROMOTED);
    assertThat(updatedRaw.getPromotedExperienceId()).isNotNull();
  }

  @Test
  @DisplayName("8. Given invalid payload (missing hero image), POST /api/admin/deals/{id}/promote returns 400")
  void testPromoteDealMissingHeroImageReturns400() throws Exception {
    rawDealRepository.deleteAllInBatch();

    RawDeal deal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("SEED")
                .sourceRef("test-promote-002")
                .rawTitle("Raw Title")
                .build());

    String invalidJson =
        """
        {
          "editorialTitle": "Bio Brotgarten Berlin",
          "editorialDescription": "Frisches Sauerteigbrot",
          "heroImageUrl": "",
          "address": "Kastanienallee 12",
          "lat": 52.535123,
          "lng": 13.408456
        }
        """;

    mockMvc
        .perform(
            post("/api/admin/deals/" + deal.getId() + "/promote")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("9. Given re-promoted raw deal, updates existing curated_experiences row without duplicate")
  void testRePromoteDealUpsertsExperience() throws Exception {
    rawDealRepository.deleteAllInBatch();
    curatedExperienceRepository.deleteAllInBatch();

    RawDeal deal =
        rawDealRepository.saveAndFlush(
            RawDeal.builder()
                .source("SEED")
                .sourceRef("test-promote-003")
                .rawTitle("Raw Title")
                .build());

    String promoteJson1 =
        """
        {
          "slug": "bio-brotgarten",
          "editorialTitle": "Original Title",
          "editorialDescription": "Original Description",
          "heroImageUrl": "https://img.com/1.jpg",
          "address": "Address 1",
          "lat": 52.500000,
          "lng": 13.400000
        }
        """;

    mockMvc
        .perform(
            post("/api/admin/deals/" + deal.getId() + "/promote")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(promoteJson1))
        .andExpect(status().isOk());

    assertThat(curatedExperienceRepository.count()).isEqualTo(1);

    String promoteJson2 =
        """
        {
          "slug": "bio-brotgarten",
          "editorialTitle": "Updated Editorial Title",
          "editorialDescription": "Updated Description",
          "heroImageUrl": "https://img.com/2.jpg",
          "address": "Address 1",
          "lat": 52.500000,
          "lng": 13.400000
        }
        """;

    mockMvc
        .perform(
            post("/api/admin/deals/" + deal.getId() + "/promote")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(promoteJson2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.editorialTitle").value("Updated Editorial Title"));

    assertThat(curatedExperienceRepository.count()).isEqualTo(1);
  }
}
