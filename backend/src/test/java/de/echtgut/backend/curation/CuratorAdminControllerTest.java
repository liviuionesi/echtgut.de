package de.echtgut.backend.curation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * <p>Verifies {@code GET /api/admin/pending-deals} returns the next pending deal or 204 No Content,
 * and {@code POST /api/admin/deals/{id}/reject} updates deal status to REJECTED.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CuratorAdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RawDealRepository rawDealRepository;

  @Test
  @DisplayName("1. Given no pending deals, GET /api/admin/pending-deals returns HTTP 204 No Content")
  void testGetPendingDealsEmptyReturns204() throws Exception {
    rawDealRepository.deleteAllInBatch();

    mockMvc.perform(get("/api/admin/pending-deals")).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "2. Given pending deal, GET /api/admin/pending-deals returns HTTP 200 OK with deal details")
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
        .perform(get("/api/admin/pending-deals"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(deal.getId().toString()))
        .andExpect(jsonPath("$.rawTitle").value("Kaffeekultur Berlin"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName(
      "3. Given pending deal, POST /api/admin/deals/{id}/reject updates status to REJECTED")
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
}
