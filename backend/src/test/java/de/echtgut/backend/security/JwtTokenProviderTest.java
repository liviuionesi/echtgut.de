package de.echtgut.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit test suite for {@link JwtTokenProvider}.
 *
 * <p>Verifies token creation, validation, claim parsing, authority extraction, and expiration handling.
 */
class JwtTokenProviderTest {

  private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!";
  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(SECRET, 3600000L);
  }

  @Test
  @DisplayName("1. Given valid parameters, generateToken produces verifiable JWT")
  void testGenerateAndValidateToken() {
    // 1. Given username and roles
    String username = "curator@echtgut.de";
    List<String> roles = List.of("CURATOR");

    // 2. When generating token
    String token = jwtTokenProvider.generateToken(username, roles);

    // 3. Then token is valid and subject is correct
    assertThat(token).isNotBlank();
    assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
  }

  @Test
  @DisplayName("2. Given token with roles, getAuthoritiesFromToken returns prefixed GrantedAuthorities")
  void testGetAuthoritiesFromToken() {
    // 1. Given token with CURATOR and ADMIN roles
    String token = jwtTokenProvider.generateToken("admin@echtgut.de", List.of("CURATOR", "ADMIN"));

    // 2. When parsing authorities
    Collection<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

    // 3. Then authorities contain ROLE_CURATOR and ROLE_ADMIN
    List<String> authorityStrings = authorities.stream().map(GrantedAuthority::getAuthority).toList();
    assertThat(authorityStrings).containsExactlyInAnyOrder("ROLE_CURATOR", "ROLE_ADMIN");
  }

  @Test
  @DisplayName("3. Given invalid or malformed token string, validateToken returns false")
  void testValidateTokenInvalid() {
    // 1. Given invalid token string
    String invalidToken = "invalid.jwt.token";

    // 2. When validating
    boolean isValid = jwtTokenProvider.validateToken(invalidToken);

    // 3. Then validation fails
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("4. Given expired token, validateToken returns false")
  void testExpiredTokenValidationFails() {
    // 1. Given provider with 0ms expiration
    JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000L);
    String expiredToken = expiredProvider.generateToken("user", List.of("CURATOR"));

    // 2. When validating expired token
    boolean isValid = jwtTokenProvider.validateToken(expiredToken);

    // 3. Then validation returns false
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("5. Given short secret key string, constructor auto-pads key without error")
  void testShortSecretPadding() {
    // 1. Given short secret under 32 bytes
    JwtTokenProvider shortSecretProvider = new JwtTokenProvider("short", 3600000L);

    // 2. When generating and validating token
    String token = shortSecretProvider.generateToken("curator", List.of("CURATOR"));

    // 3. Then token is valid
    assertThat(shortSecretProvider.validateToken(token)).isTrue();
  }
}
