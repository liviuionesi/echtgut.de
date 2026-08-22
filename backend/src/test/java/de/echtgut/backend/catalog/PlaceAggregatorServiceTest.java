package de.echtgut.backend.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import de.echtgut.backend.catalog.dto.PlaceDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for {@link PlaceAggregatorService}, exercising both source paths against a {@link
 * MockRestServiceServer} rather than the real Google/Overpass APIs. Covers the two behaviors that
 * regressed silently before this pivot's cleanup: a fabricated OSM rating (NFR-3) and a non-stable
 * place id.
 */
class PlaceAggregatorServiceTest {

  private final JsonMapper objectMapper = JsonMapper.builder().build();

  @Test
  @DisplayName("1. OSM fallback path never fabricates a rating/review-count/open-status")
  void testOverpassFallbackLeavesRatingUnset() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(containsString("/interpreter")))
        .andRespond(
            withSuccess(
                """
                {"elements": [
                  {"id": 123456, "lat": 48.78, "lon": 9.18,
                   "tags": {"name": "Schlossgarten", "historic": "castle"}}
                ]}
                """,
                MediaType.APPLICATION_JSON));

    PlaceAggregatorService service = new PlaceAggregatorService(builder, objectMapper, "");

    List<PlaceDto> places = service.getTrendingPlacesInStuttgart();

    assertThat(places).hasSize(1);
    PlaceDto place = places.get(0);
    assertThat(place.getId()).isEqualTo("osm-123456");
    assertThat(place.getName()).isEqualTo("Schlossgarten");
    assertThat(place.getRating()).isNull();
    assertThat(place.getReviewCount()).isNull();
    assertThat(place.getOpenNow()).isNull();
  }

  @Test
  @DisplayName("2. Google path keys places by the real, stable Google Place ID")
  void testGooglePlacesUsesStableId() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(containsString(":searchNearby")))
        .andRespond(
            withSuccess(
                """
                {"places": [
                  {"id": "ChIJ-real-google-place-id", "displayName": {"text": "Bio Brotgarten"},
                   "location": {"latitude": 52.53, "longitude": 13.40},
                   "rating": 4.7, "userRatingCount": 823}
                ]}
                """,
                MediaType.APPLICATION_JSON));

    PlaceAggregatorService service =
        new PlaceAggregatorService(builder, objectMapper, "test-api-key");

    List<PlaceDto> places = service.getTrendingPlacesInStuttgart();

    assertThat(places).hasSize(1);
    assertThat(places.get(0).getId()).isEqualTo("ChIJ-real-google-place-id");
  }

  @Test
  @DisplayName("3. Filters out a Google result that fails the NFR-3 quality bar")
  void testGoogleResultBelowQualityBarIsFiltered() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(containsString(":searchNearby")))
        .andRespond(
            withSuccess(
                """
                {"places": [
                  {"id": "low-rated-place", "displayName": {"text": "Meh Cafe"},
                   "location": {"latitude": 48.78, "longitude": 9.18},
                   "rating": 2.5, "userRatingCount": 40}
                ]}
                """,
                MediaType.APPLICATION_JSON));

    PlaceAggregatorService service =
        new PlaceAggregatorService(builder, objectMapper, "test-api-key");

    List<PlaceDto> places = service.getTrendingPlacesInStuttgart();

    assertThat(places).isEmpty();
  }
}
