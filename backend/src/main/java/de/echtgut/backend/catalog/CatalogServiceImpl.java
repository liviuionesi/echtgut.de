package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.ExperienceDetailDto;
import de.echtgut.backend.catalog.dto.ExperienceSummaryDto;
import de.echtgut.backend.curation.CuratedExperience;
import de.echtgut.backend.curation.CuratedExperienceRepository;
import de.echtgut.backend.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Production implementation of {@link CatalogService}. */
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

  private final CuratedExperienceRepository curatedExperienceRepository;
  private final ClickEventRepository clickEventRepository;

  @Override
  public String getCatalogStatus() {
    return "Catalog service operational";
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ExperienceSummaryDto> getPublishedExperiences(
      String tag, String query, Pageable pageable) {
    // 1. Query published experience entities from repository
    List<CuratedExperience> allPublished = curatedExperienceRepository.findByIsPublishedTrue();

    // 2. Filter by search query if provided
    List<CuratedExperience> filtered =
        allPublished.stream()
            .filter(
                exp -> {
                  if (query == null || query.isBlank()) return true;
                  String q = query.toLowerCase();
                  return exp.getEditorialTitle().toLowerCase().contains(q)
                      || exp.getEditorialDescription().toLowerCase().contains(q)
                      || exp.getAddress().toLowerCase().contains(q);
                })
            .toList();

    // 3. Map filtered entities to summary DTOs
    List<ExperienceSummaryDto> dtoList =
        filtered.stream()
            .map(exp -> ExperienceSummaryDto.fromEntity(exp, List.of()))
            .toList();

    // 4. Handle pagination slicing
    int start = (int) pageable.getOffset();
    if (start >= dtoList.size()) {
      return new PageImpl<>(List.of(), pageable, dtoList.size());
    }
    int end = Math.min(start + pageable.getPageSize(), dtoList.size());
    List<ExperienceSummaryDto> pageContent = dtoList.subList(start, end);

    return new PageImpl<>(pageContent, pageable, dtoList.size());
  }

  @Override
  @Transactional(readOnly = true)
  public ExperienceDetailDto getExperienceBySlug(String slug) {
    // 1. Fetch published experience entity by slug
    CuratedExperience experience =
        curatedExperienceRepository
            .findBySlug(slug)
            .filter(exp -> exp != null && exp.isPublished())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Published experience not found with slug: " + slug));

    // 2. Map entity to detail DTO record
    return ExperienceDetailDto.fromEntity(experience, List.of());
  }

  @Override
  @Transactional
  public String recordClickAndGetRedirectUrl(
      UUID experienceId, String referrer, String userAgent) {
    // 1. Retrieve target curated experience entity
    CuratedExperience experience =
        curatedExperienceRepository
            .findById(experienceId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Experience not found with ID: " + experienceId));

    // 2. Record click event details
    ClickEvent click =
        ClickEvent.builder()
            .experienceId(experienceId)
            .referrerUrl(referrer)
            .userAgent(userAgent)
            .build();
    clickEventRepository.save(click);

    // 3. Determine fallback redirect URL hierarchy (affiliateUrl -> bookingContact fallback -> detail slug)
    if (experience.getAffiliateUrl() != null && !experience.getAffiliateUrl().isBlank()) {
      return experience.getAffiliateUrl();
    }
    if (experience.getBookingContact() != null
        && (experience.getBookingContact().startsWith("http://")
            || experience.getBookingContact().startsWith("https://"))) {
      return experience.getBookingContact();
    }
    return "/experience/" + experience.getSlug();
  }
}
