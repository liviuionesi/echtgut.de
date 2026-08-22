package de.echtgut.backend.taxonomy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.echtgut.backend.security.JwtTokenProvider;
import de.echtgut.backend.taxonomy.dto.CreateTagRequest;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaxonomyAdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private TaxonomyService taxonomyService;

  private String curatorToken;

  @BeforeEach
  void setUp() {
    curatorToken = jwtTokenProvider.generateToken("test-curator", List.of("CURATOR"));
  }

  @Test
  @DisplayName("1. Given unauthenticated request to GET /api/admin/tags, returns 401")
  void testGetTagsUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/admin/tags")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("2. Given valid CURATOR JWT, GET /api/admin/tags returns 200 OK")
  void testGetTagsSuccess() throws Exception {
    TagDto dto = new TagDto(UUID.randomUUID(), "auszeit", "Auszeit", "MOOD", false);
    when(taxonomyService.getAllTags(false)).thenReturn(List.of(dto));

    mockMvc
        .perform(
            get("/api/admin/tags")
                .header("Authorization", "Bearer " + curatorToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].slug").value("auszeit"))
        .andExpect(jsonPath("$[0].name").value("Auszeit"));
  }

  @Test
  @DisplayName("3. Given valid payload and CURATOR JWT, POST /api/admin/tags creates tag")
  void testCreateTagSuccess() throws Exception {
    TagDto created = new TagDto(UUID.randomUUID(), "wellness-spa", "Wellness & Spa", "MOOD", false);
    when(taxonomyService.createTag(any(CreateTagRequest.class))).thenReturn(created);

    String payloadJson = "{\"name\":\"Wellness & Spa\",\"slug\":\"wellness-spa\",\"category\":\"MOOD\"}";

    mockMvc
        .perform(
            post("/api/admin/tags")
                .header("Authorization", "Bearer " + curatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.slug").value("wellness-spa"));
  }

  @Test
  @DisplayName("4. Given valid tag ID and CURATOR JWT, POST /api/admin/tags/{id}/retire retires tag")
  void testRetireTagSuccess() throws Exception {
    UUID tagId = UUID.randomUUID();
    TagDto retired = new TagDto(tagId, "old", "Old", "MOOD", true);

    when(taxonomyService.retireTag(tagId)).thenReturn(retired);

    mockMvc
        .perform(
            post("/api/admin/tags/" + tagId + "/retire")
                .header("Authorization", "Bearer " + curatorToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isRetired").value(true));
  }
}
