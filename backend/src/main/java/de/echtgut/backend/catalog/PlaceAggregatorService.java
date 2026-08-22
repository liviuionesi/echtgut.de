package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.PlaceDto;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Aggregates real-world place data live from external sources — Google Places API (New) when an API
 * key is configured, falling back to OpenStreetMap's Overpass API otherwise (ADR-003) — and
 * normalizes both into {@link PlaceDto}. There is no local persistence: every call re-fetches
 * (subject to Spring's cache abstraction, see {@link #getTrendingPlacesInStuttgart()}).
 *
 * <p>NFR-3 ("filter out poorly rated or incomplete places") is enforced here, not by the caller —
 * see {@link #passesQualityBar(PlaceDto)}.
 */
@Slf4j
@Service
public class PlaceAggregatorService {

  /** Minimum Google rating for a place to be considered trustworthy enough to show (NFR-3). */
  private static final double MIN_RATING = 4.0;

  /** Minimum review count so a single fluke 5-star review can't clear {@link #MIN_RATING}. */
  private static final int MIN_REVIEW_COUNT = 5;

  private final RestClient overpassClient;
  private final RestClient googlePlacesClient;
  private final ObjectMapper objectMapper;
  private final String googlePlacesApiKey;

  // Array of visually appealing placeholder images for OSM fallback
  private static final String[] PLACEHOLDER_IMAGES = {
    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1560200353-ce0a76b1d438?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1551632436-cbf8dd35adfa?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?auto=format&fit=crop&q=80&w=800",
    "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?auto=format&fit=crop&q=80&w=800"
  };

  /**
   * Constructs the service, building the two source-specific REST clients off the Spring-provided
   * {@link RestClient.Builder} — a shared builder rather than unconditionally-constructed clients
   * so tests can supply one bound to a mock server.
   *
   * @param restClientBuilder Spring-managed builder, cloned once per source so each keeps its own
   *     base URL.
   * @param objectMapper Jackson mapper for parsing both APIs' JSON responses.
   * @param googlePlacesApiKey Google Places API key; blank/absent triggers the OSM fallback.
   */
  public PlaceAggregatorService(
      RestClient.Builder restClientBuilder,
      ObjectMapper objectMapper,
      @Value("${google.places.api-key:}") String googlePlacesApiKey) {
    this.objectMapper = objectMapper;
    this.googlePlacesApiKey = googlePlacesApiKey;
    this.overpassClient = restClientBuilder.clone().baseUrl("https://overpass-api.de/api").build();
    this.googlePlacesClient =
        restClientBuilder.clone().baseUrl("https://places.googleapis.com/v1/places").build();
  }

  /**
   * Returns the current trending places in and around Stuttgart, from Google Places when a key is
   * configured, otherwise from OSM Overpass.
   *
   * @return Places passing the {@link #passesQualityBar(PlaceDto)} filter, or an empty list if the
   *     upstream fetch fails.
   */
  @Cacheable(value = "trendingPlaces")
  public List<PlaceDto> getTrendingPlacesInStuttgart() {
    List<PlaceDto> places;
    if (googlePlacesApiKey != null && !googlePlacesApiKey.isBlank()) {
      log.info("Google Places API Key found. Fetching real places from Google...");
      places = fetchFromGooglePlaces();
    } else {
      log.info("No Google Places API Key found. Falling back to Overpass API...");
      places = fetchFromOverpass();
    }
    return places.stream().filter(this::passesQualityBar).toList();
  }

  /**
   * NFR-3's data quality gate. Google-sourced places need a real rating that clears {@link
   * #MIN_RATING} on at least {@link #MIN_REVIEW_COUNT} reviews; OSM has no ratings at all, so it's
   * held to completeness only (a name and real coordinates) rather than a fabricated score.
   *
   * @param place Candidate place, already normalized.
   * @return {@code true} if the place is trustworthy/complete enough to show.
   */
  private boolean passesQualityBar(PlaceDto place) {
    if (place.getName() == null || place.getName().isBlank()) {
      return false;
    }
    if (place.getLat() == 0.0 && place.getLon() == 0.0) {
      return false;
    }
    // No rating on this place at all (OSM path) — completeness is all we can check.
    if (place.getRating() == null) {
      return true;
    }
    return place.getRating() >= MIN_RATING
        && place.getReviewCount() != null
        && place.getReviewCount() >= MIN_REVIEW_COUNT;
  }

  private List<PlaceDto> fetchFromGooglePlaces() {
    // 48.7758, 9.1829 is Stuttgart.
    String requestBody =
        """
        {
          "includedTypes": ["restaurant", "cafe", "museum", "historical_landmark"],
          "maxResultCount": 20,
          "locationRestriction": {
            "circle": {
              "center": {
                "latitude": 48.7758,
                "longitude": 9.1829
              },
              "radius": 50000.0
            }
          }
        }
        """;

    try {
      String response =
          googlePlacesClient
              .post()
              .uri(":searchNearby")
              .header("X-Goog-Api-Key", googlePlacesApiKey)
              .header(
                  "X-Goog-FieldMask",
                  "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.currentOpeningHours,places.photos,places.primaryTypeDisplayName")
              .header("Content-Type", "application/json")
              .body(requestBody)
              .retrieve()
              .body(String.class);

      JsonNode root = objectMapper.readTree(response);
      JsonNode placesNode = root.path("places");
      List<PlaceDto> places = new ArrayList<>();

      int fallbackImageIndex = 0;

      for (JsonNode node : placesNode) {
        String id = node.path("id").asText(null);
        if (id == null || id.isBlank()) {
          continue; // No stable id to key this place by — skip rather than mint a fake one.
        }
        String name = node.path("displayName").path("text").asText("Unnamed Place");
        double lat = node.path("location").path("latitude").asDouble();
        double lon = node.path("location").path("longitude").asDouble();

        Double rating = node.has("rating") ? node.path("rating").asDouble() : null;
        Integer reviewCount =
            node.has("userRatingCount") ? node.path("userRatingCount").asInt() : null;

        Boolean openNow = null;
        if (node.has("currentOpeningHours") && node.path("currentOpeningHours").has("openNow")) {
          openNow = node.path("currentOpeningHours").path("openNow").asBoolean();
        }

        String category = node.path("primaryTypeDisplayName").path("text").asText("Location");
        String address = node.path("formattedAddress").asText("Stuttgart Umgebung");

        String imageUrl = PLACEHOLDER_IMAGES[fallbackImageIndex % PLACEHOLDER_IMAGES.length];
        if (node.has("photos") && node.path("photos").isArray() && node.path("photos").size() > 0) {
          String photoReference = node.path("photos").get(0).path("name").asText();
          // The photoReference looks like "places/ChI.../photos/AUc..."
          // Construct Google Places Photo URL
          imageUrl =
              String.format(
                  "https://places.googleapis.com/v1/%s/media?maxHeightPx=800&maxWidthPx=800&key=%s",
                  photoReference, googlePlacesApiKey);
        } else {
          fallbackImageIndex++;
        }

        places.add(
            PlaceDto.builder()
                .id(id)
                .name(name)
                .description("Entdeckt via Google Places API: " + name)
                .category(category)
                .address(address)
                .lat(lat)
                .lon(lon)
                .rating(rating)
                .reviewCount(reviewCount)
                .openNow(openNow)
                .imageUrl(imageUrl)
                .build());
      }

      log.info("Successfully aggregated {} trending places from Google.", places.size());
      return places;

    } catch (Exception e) {
      log.error("Failed to fetch from Google Places API", e);
      return List.of();
    }
  }

  private List<PlaceDto> fetchFromOverpass() {
    String query =
        "[out:json][timeout:25];"
            + "("
            + "node[\"tourism\"=\"museum\"](around:50000,48.7758,9.1829);"
            + "node[\"amenity\"=\"restaurant\"][\"cuisine\"=\"regional\"](around:50000,48.7758,9.1829);"
            + "node[\"historic\"=\"castle\"](around:50000,48.7758,9.1829);"
            + ");"
            + "out 50;";

    try {
      String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
      String response =
          overpassClient
              .get()
              .uri("/interpreter?data=" + encodedQuery)
              .retrieve()
              .body(String.class);

      JsonNode root = objectMapper.readTree(response);
      JsonNode elements = root.path("elements");
      List<PlaceDto> places = new ArrayList<>();

      int imageIndex = 0;

      for (JsonNode node : elements) {
        JsonNode tags = node.path("tags");
        String name = tags.path("name").asText("Unnamed Place");

        if ("Unnamed Place".equals(name)) continue;
        if (!node.has("id")) continue; // No stable OSM node id — skip rather than mint a fake one.

        String id = "osm-" + node.path("id").asLong();
        double lat = node.path("lat").asDouble();
        double lon = node.path("lon").asDouble();

        // OSM has no star-rating concept at all — leave rating/reviewCount/openNow unset
        // (null) rather than fabricating one. A fabricated number is a false trust signal
        // this product's whole pitch depends on not showing (NFR-3).
        String category = "Location";
        if (tags.has("tourism")) category = "Kultur & Entdeckung";
        if (tags.has("amenity")) category = "Kulinarik";
        if (tags.has("historic")) category = "Historisch";

        places.add(
            PlaceDto.builder()
                .id(id)
                .name(name)
                .description(
                    tags.path("description")
                        .asText(
                            "Ein faszinierender Ort, der darauf wartet, entdeckt zu werden. (Fallback Daten)"))
                .category(category)
                .address(
                    tags.path("addr:street").asText("")
                        + " "
                        + tags.path("addr:city").asText("Stuttgart Umgebung"))
                .lat(lat)
                .lon(lon)
                .imageUrl(PLACEHOLDER_IMAGES[imageIndex % PLACEHOLDER_IMAGES.length])
                .build());

        imageIndex++;
      }

      log.info("Successfully aggregated {} trending places from Overpass.", places.size());
      return places;

    } catch (Exception e) {
      log.error("Failed to fetch from Overpass API", e);
      return List.of();
    }
  }
}
