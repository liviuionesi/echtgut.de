package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.PlaceDto;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class PlaceAggregatorService {

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

  public PlaceAggregatorService(
      ObjectMapper objectMapper,
      @Value("${google.places.api-key:}") String googlePlacesApiKey) {
    this.objectMapper = objectMapper;
    this.googlePlacesApiKey = googlePlacesApiKey;
    this.overpassClient = RestClient.builder()
        .baseUrl("https://overpass-api.de/api")
        .build();
    this.googlePlacesClient = RestClient.builder()
        .baseUrl("https://places.googleapis.com/v1/places")
        .build();
  }

  @Cacheable(value = "trendingPlaces")
  public List<PlaceDto> getTrendingPlacesInStuttgart() {
    if (googlePlacesApiKey != null && !googlePlacesApiKey.isBlank()) {
      log.info("Google Places API Key found. Fetching real places from Google...");
      return fetchFromGooglePlaces();
    } else {
      log.info("No Google Places API Key found. Falling back to Overpass API...");
      return fetchFromOverpass();
    }
  }

  private List<PlaceDto> fetchFromGooglePlaces() {
    // 48.7758, 9.1829 is Stuttgart.
    String requestBody = """
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
      String response = googlePlacesClient.post()
          .uri(":searchNearby")
          .header("X-Goog-Api-Key", googlePlacesApiKey)
          .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.currentOpeningHours,places.photos,places.primaryTypeDisplayName")
          .header("Content-Type", "application/json")
          .body(requestBody)
          .retrieve()
          .body(String.class);

      JsonNode root = objectMapper.readTree(response);
      JsonNode placesNode = root.path("places");
      List<PlaceDto> places = new ArrayList<>();
      
      int fallbackImageIndex = 0;

      for (JsonNode node : placesNode) {
        String name = node.path("displayName").path("text").asText("Unnamed Place");
        double lat = node.path("location").path("latitude").asDouble();
        double lon = node.path("location").path("longitude").asDouble();
        
        double rating = node.path("rating").asDouble(0.0);
        int reviewCount = node.path("userRatingCount").asInt(0);
        
        boolean openNow = false;
        if (node.has("currentOpeningHours") && node.path("currentOpeningHours").has("openNow")) {
            openNow = node.path("currentOpeningHours").path("openNow").asBoolean(false);
        }
        
        String category = node.path("primaryTypeDisplayName").path("text").asText("Location");
        String address = node.path("formattedAddress").asText("Stuttgart Umgebung");
        
        String imageUrl = PLACEHOLDER_IMAGES[fallbackImageIndex % PLACEHOLDER_IMAGES.length];
        if (node.has("photos") && node.path("photos").isArray() && node.path("photos").size() > 0) {
            String photoReference = node.path("photos").get(0).path("name").asText();
            // The photoReference looks like "places/ChI.../photos/AUc..."
            // Construct Google Places Photo URL
            imageUrl = String.format("https://places.googleapis.com/v1/%s/media?maxHeightPx=800&maxWidthPx=800&key=%s", 
                photoReference, googlePlacesApiKey);
        } else {
            fallbackImageIndex++;
        }

        places.add(PlaceDto.builder()
            .id(UUID.randomUUID())
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
    String query = "[out:json][timeout:25];" +
        "(" +
        "node[\"tourism\"=\"museum\"](around:50000,48.7758,9.1829);" +
        "node[\"amenity\"=\"restaurant\"][\"cuisine\"=\"regional\"](around:50000,48.7758,9.1829);" +
        "node[\"historic\"=\"castle\"](around:50000,48.7758,9.1829);" +
        ");" +
        "out 50;";
        
    try {
      String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
      String response = overpassClient.get()
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

        double lat = node.path("lat").asDouble();
        double lon = node.path("lon").asDouble();
        
        double rating = 4.5 + (Math.random() * 0.5); 
        int reviewCount = 150 + (int)(Math.random() * 1500);
        boolean openNow = Math.random() > 0.2; 
        
        String category = "Location";
        if (tags.has("tourism")) category = "Kultur & Entdeckung";
        if (tags.has("amenity")) category = "Kulinarik";
        if (tags.has("historic")) category = "Historisch";

        places.add(PlaceDto.builder()
            .id(UUID.randomUUID())
            .name(name)
            .description(tags.path("description").asText("Ein faszinierender Ort, der darauf wartet, entdeckt zu werden. (Fallback Daten)"))
            .category(category)
            .address(tags.path("addr:street").asText("") + " " + tags.path("addr:city").asText("Stuttgart Umgebung"))
            .lat(lat)
            .lon(lon)
            .rating(Math.round(rating * 10.0) / 10.0)
            .reviewCount(reviewCount)
            .openNow(openNow)
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
