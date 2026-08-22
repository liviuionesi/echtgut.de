package de.echtgut.backend.catalog;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.echtgut.backend.catalog.dto.PlaceDto;
import de.echtgut.backend.taxonomy.TaxonomyService;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests for {@link PublicCatalogController}. Both collaborators are mocked — this
 * controller has no persistence of its own (ADR-003: places are fetched live, never stored), so
 * there's nothing here for {@code @SpringBootTest}/a real database to add.
 */
@WebMvcTest(PublicCatalogController.class)
class PublicCatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private TaxonomyService taxonomyService;
  @MockitoBean private PlaceAggregatorService placeAggregatorService;

  @Test
  @DisplayName("1. GET /api/places/trending returns the aggregator's live places")
  void testGetTrendingPlaces() throws Exception {
    PlaceDto place =
        PlaceDto.builder()
            .id("ChIJ-real-google-place-id")
            .name("Bio Brotgarten")
            .description("Entdeckt via Google Places API: Bio Brotgarten")
            .category("Bakery")
            .address("Kastanienallee 12, Berlin")
            .lat(52.53)
            .lon(13.40)
            .rating(4.7)
            .reviewCount(823)
            .openNow(true)
            .imageUrl("https://example.com/brot.jpg")
            .build();
    when(placeAggregatorService.getTrendingPlacesInStuttgart()).thenReturn(List.of(place));

    mockMvc
        .perform(get("/api/places/trending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("ChIJ-real-google-place-id"))
        .andExpect(jsonPath("$[0].name").value("Bio Brotgarten"))
        .andExpect(jsonPath("$[0].rating").value(4.7));
  }

  @Test
  @DisplayName("2. GET /api/places/trending omits rating fields the aggregator left unset")
  void testGetTrendingPlacesWithoutRating() throws Exception {
    PlaceDto unrated =
        PlaceDto.builder()
            .id("osm-123456")
            .name("Schlossgarten")
            .description("Ein faszinierender Ort, der darauf wartet, entdeckt zu werden.")
            .category("Historisch")
            .address("Stuttgart")
            .lat(48.78)
            .lon(9.18)
            .imageUrl("https://example.com/placeholder.jpg")
            .build();
    when(placeAggregatorService.getTrendingPlacesInStuttgart()).thenReturn(List.of(unrated));

    mockMvc
        .perform(get("/api/places/trending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("osm-123456"))
        .andExpect(jsonPath("$[0].rating").doesNotExist());
  }

  @Test
  @DisplayName("3. GET /api/tags returns active tags from the taxonomy service")
  void testGetPublicTags() throws Exception {
    TagDto tag =
        new TagDto(
            java.util.UUID.randomUUID(), "auszeit", "Auszeit", "MOOD", false, "Zeit für dich");
    when(taxonomyService.getAllTags(anyBoolean())).thenReturn(List.of(tag));

    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].slug").value("auszeit"));
  }
}
