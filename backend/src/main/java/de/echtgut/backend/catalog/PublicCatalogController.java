package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.PlaceDto;
import de.echtgut.backend.taxonomy.TaxonomyService;
import de.echtgut.backend.taxonomy.dto.TagDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public visitor REST controller serving the live-aggregated place catalog (ADR-003) and public
 * taxonomy tags.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PublicCatalogController {

  private final TaxonomyService taxonomyService;
  private final PlaceAggregatorService placeAggregatorService;

  /**
   * Retrieves public taxonomy tags (excluding retired ones).
   *
   * @return List of {@link TagDto}.
   */
  @GetMapping("/api/tags")
  public ResponseEntity<List<TagDto>> getPublicTags() {
    return ResponseEntity.ok(taxonomyService.getAllTags(false));
  }

  /**
   * Retrieves trending places automatically aggregated from real-world data (Google Places API,
   * falling back to OSM Overpass API).
   *
   * @return List of {@link PlaceDto}.
   */
  @GetMapping("/api/places/trending")
  public ResponseEntity<List<PlaceDto>> getTrendingPlaces() {
    return ResponseEntity.ok(placeAggregatorService.getTrendingPlacesInStuttgart());
  }
}
