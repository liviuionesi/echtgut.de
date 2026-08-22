package de.echtgut.backend.taxonomy;

import de.echtgut.backend.taxonomy.dto.CreateTagRequest;
import de.echtgut.backend.taxonomy.dto.TagDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing curator admin management endpoints for experience taxonomy tags.
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class TaxonomyAdminController {

  private final TaxonomyService taxonomyService;

  /**
   * Retrieves taxonomy tags.
   *
   * @param includeRetired Optional flag to include retired tags (default {@code false}).
   * @return List of {@link TagDto}.
   */
  @GetMapping
  public ResponseEntity<List<TagDto>> getTags(
      @RequestParam(name = "includeRetired", defaultValue = "false") boolean includeRetired) {
    List<TagDto> tags = taxonomyService.getAllTags(includeRetired);
    return ResponseEntity.ok(tags);
  }

  /**
   * Creates a new taxonomy tag.
   *
   * @param request Validated {@link CreateTagRequest}.
   * @return Created {@link TagDto} with HTTP 201 Created.
   */
  @PostMapping
  public ResponseEntity<TagDto> createTag(@Valid @RequestBody CreateTagRequest request) {
    TagDto created = taxonomyService.createTag(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * Retires an existing tag.
   *
   * @param id UUID tag identifier.
   * @return Updated {@link TagDto} with HTTP 200 OK.
   */
  @PostMapping("/{id}/retire")
  public ResponseEntity<TagDto> retireTag(@PathVariable("id") UUID id) {
    TagDto retired = taxonomyService.retireTag(id);
    return ResponseEntity.ok(retired);
  }
}
