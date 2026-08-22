package de.echtgut.backend.catalog.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDto {
  private UUID id;
  private String name;
  private String description;
  private String category;
  private String address;
  private double lat;
  private double lon;
  private double rating;
  private int reviewCount;
  private boolean openNow;
  private String imageUrl;
}
