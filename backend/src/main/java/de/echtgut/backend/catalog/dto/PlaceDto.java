package de.echtgut.backend.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public payload for one aggregated place, sourced live from either the Google Places API or the
 * OpenStreetMap Overpass API (see {@link de.echtgut.backend.catalog.PlaceAggregatorService}).
 *
 * <p>{@code id} is the source's own stable identifier (a Google Place ID string, or {@code
 * "osm-<node id>"}) — never minted locally — so the same place round-trips to the same id across
 * requests. {@code rating}/{@code reviewCount}/{@code openNow} are {@code null} whenever the source
 * genuinely doesn't provide that field (OSM has no star ratings); a value here is always a real
 * one, never a placeholder standing in for "unknown."
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDto {
  private String id;
  private String name;
  private String description;
  private String category;
  private String address;
  private double lat;
  private double lon;
  private Double rating;
  private Integer reviewCount;
  private Boolean openNow;
  private String imageUrl;
}
