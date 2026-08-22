package de.echtgut.backend.catalog;

import de.echtgut.backend.catalog.dto.SubmissionRequestDto;
import de.echtgut.backend.curation.RawDeal;
import de.echtgut.backend.curation.RawDealRepository;
import de.echtgut.backend.curation.RawDealStatus;
import de.echtgut.backend.exception.RateLimitExceededException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling community local gem submissions.
 * Includes in-memory rate limiting to prevent spam.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

  private final RawDealRepository rawDealRepository;
  
  // In-memory rate limiter: IP address -> (Timestamp of last submissions, count)
  // Simplified for MVP. We store an array of submission timestamps or just count.
  // Actually, a simple map of IP -> [Last Submission Time, Count in last hour]
  // To keep it very simple: map of IP -> timestamp of last submission. 
  // Wait, requirement says "more than rate limit allows", let's say 3 per hour.
  private final Map<String, UserRateLimit> rateLimitCache = new ConcurrentHashMap<>();
  private static final int MAX_SUBMISSIONS_PER_HOUR = 3;

  @Transactional
  public void submitLocalGem(SubmissionRequestDto request, String ipAddress) {
    checkRateLimit(ipAddress);

    RawDeal rawDeal = RawDeal.builder()
        .source("COMMUNITY")
        .sourceRef("community-" + UUID.randomUUID())
        .rawTitle(request.name())
        .locationText(request.address())
        .rawDescription(request.description())
        .status(RawDealStatus.PENDING)
        .build();

    rawDealRepository.save(rawDeal);
  }

  private void checkRateLimit(String ipAddress) {
    Instant now = Instant.now();
    rateLimitCache.compute(ipAddress, (ip, limit) -> {
      if (limit == null) {
        return new UserRateLimit(now, 1);
      }
      
      // Reset limit if an hour has passed since the window started
      if (ChronoUnit.HOURS.between(limit.windowStart, now) >= 1) {
        return new UserRateLimit(now, 1);
      }
      
      if (limit.count >= MAX_SUBMISSIONS_PER_HOUR) {
        throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
      }
      
      return new UserRateLimit(limit.windowStart, limit.count + 1);
    });
  }

  private record UserRateLimit(Instant windowStart, int count) {}
}
