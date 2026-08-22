package de.echtgut.backend.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicSubmissionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("1. Submitting valid local gem returns 201 Created")
  void testSubmitGemSuccess() throws Exception {
    String json = "{\"name\":\"Test Gem\",\"address\":\"123 Test St\",\"description\":\"A great hidden gem\"}";

    mockMvc
        .perform(
            post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("X-Forwarded-For", "192.168.1.100"))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("2. Rate limiting kicks in after 3 requests from same IP")
  void testSubmitGemRateLimit() throws Exception {
    String json = "{\"name\":\"Test Gem 2\",\"address\":\"456 Test St\",\"description\":\"Another hidden gem\"}";

    String ip = "10.0.0.5";

    // First 3 should succeed
    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post("/api/submissions")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json)
                  .header("X-Forwarded-For", ip))
          .andExpect(status().isCreated());
    }

    // 4th should fail with 429
    mockMvc
        .perform(
            post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("X-Forwarded-For", ip))
        .andExpect(status().isTooManyRequests());
  }
}
