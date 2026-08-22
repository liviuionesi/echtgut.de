package de.echtgut.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter executed once per HTTP request to inspect the {@code Authorization} header for a valid
 * Bearer JWT.
 *
 * <p>When a valid JWT token is present, populates the {@link SecurityContextHolder} with an
 * authenticated {@link UsernamePasswordAuthenticationToken}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 1. Extract Bearer token from Authorization header
    String token = extractBearerToken(request);

    // 2. Validate token and populate SecurityContext
    if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
      String username = jwtTokenProvider.getUsernameFromToken(token);
      Collection<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(username, null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 3. Continue filter chain execution
    filterChain.doFilter(request, response);
  }

  /**
   * Helper method to extract token string from {@code Authorization: Bearer <token>} header.
   *
   * @param request Incoming HTTP request.
   * @return Extracted JWT token string or {@code null} if missing/invalid format.
   */
  private String extractBearerToken(HttpServletRequest request) {
    String bearerHeader = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerHeader) && bearerHeader.startsWith("Bearer ")) {
      return bearerHeader.substring(7);
    }
    return null;
  }
}
