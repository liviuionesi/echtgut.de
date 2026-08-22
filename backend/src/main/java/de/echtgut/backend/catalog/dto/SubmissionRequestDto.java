package de.echtgut.backend.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for submitting a new local gem (community submission).
 *
 * @param name Name of the place/experience.
 * @param address Location text or address.
 * @param description Why it's a hidden gem.
 */
public record SubmissionRequestDto(
    @NotBlank(message = "Name is required") 
    @Size(max = 500, message = "Name must be under 500 characters") 
    String name,

    @NotBlank(message = "Address is required") 
    @Size(max = 500, message = "Address must be under 500 characters") 
    String address,

    @NotBlank(message = "Description is required") 
    String description
) {}
