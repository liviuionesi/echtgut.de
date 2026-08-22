package de.echtgut.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Service component responsible for JWT token generation, parsing, validation, and authority
 * extraction.
 *
 * <p>Uses HS256 signature algorithm with a secret key derived from application properties or
 * environment variables.
 */
@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final long expirationMs;

  /**
   * Constructs {@link JwtTokenProvider} with configured secret and expiration duration.
   *
   * @param secret Plain text secret key (minimum 32 bytes required for HS256).
   * @param expirationMs Token validity duration in milliseconds.
   */
  public JwtTokenProvider(
      @Value(
              "${echtgut.jwt.secret:${JWT_SECRET:devsecret-change-in-production-must-be-at-least-32-bytes}}")
          String secret,
      @Value("${echtgut.jwt.expiration-ms:86400000}") long expirationMs) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      byte[] padded = new byte[32];
      System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
      keyBytes = padded;
    }
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.expirationMs = expirationMs;
  }

  /**
   * Generates a signed HS256 JWT token for a given subject and granted roles.
   *
   * @param username Subject/username associated with token.
   * @param roles Collection of roles (e.g. {@code List.of("CURATOR")}).
   * @return Signed JWT string.
   */
  public String generateToken(String username, Collection<String> roles) {
    Instant now = Instant.now();
    Instant expiry = now.plusMillis(expirationMs);

    return Jwts.builder()
        .subject(username)
        .claim("roles", roles)
        .issuedAt(java.util.Date.from(now))
        .expiration(java.util.Date.from(expiry))
        .signWith(key)
        .compact();
  }

  /**
   * Validates whether the given JWT token string is structurally sound, properly signed, and
   * unexpired.
   *
   * @param token JWT string to validate.
   * @return {@code true} if valid; {@code false} otherwise.
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Extracts the subject (username/id) from a valid JWT token.
   *
   * @param token Valid JWT string.
   * @return Subject string.
   */
  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }

  /**
   * Extracts GrantedAuthorities from the token's {@code roles} or {@code role} claim.
   *
   * @param token Valid JWT string.
   * @return Collection of {@link GrantedAuthority}.
   */
  public Collection<GrantedAuthority> getAuthoritiesFromToken(String token) {
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

    Object rolesObject = claims.get("roles");
    if (rolesObject == null) {
      rolesObject = claims.get("role");
    }

    if (rolesObject == null) {
      return Collections.emptyList();
    }

    List<String> rawRoles =
        switch (rolesObject) {
          case List<?> list -> list.stream().filter(Objects::nonNull).map(String::valueOf).toList();
          case String strRole ->
              Arrays.stream(strRole.split(","))
                  .filter(entry -> !entry.isBlank())
                  .map(entry -> entry.trim())
                  .toList();
          case null, default -> Collections.emptyList();
        };

    return rawRoles.stream()
        .map(
            role -> {
              String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
              return new SimpleGrantedAuthority(authorityName);
            })
        .collect(Collectors.toList());
  }
}
