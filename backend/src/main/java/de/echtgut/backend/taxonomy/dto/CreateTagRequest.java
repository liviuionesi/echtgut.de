package de.echtgut.backend.taxonomy.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload DTO for creating a new taxonomy tag.
 *
 * @param name Tag display name.
 * @param slug Optional custom slug.
 * @param category Optional tag category.
 */
public record CreateTagRequest(
    @NotBlank(message = "Tag name is required") String name,
    String slug,
    String category) {}
