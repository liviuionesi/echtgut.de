package de.echtgut.backend.config;

import de.echtgut.backend.exception.SecurityConfigurationException;
import de.echtgut.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration bean defining authentication rules, stateless session management,
 * and endpoint authorization policies.
 *
 * <p>Gates all {@code /api/admin/**} endpoints to authenticated users possessing either the {@code
 * CURATOR} or {@code ADMIN} role, while keeping public endpoints accessible.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Configures the primary {@link SecurityFilterChain} for the application.
   *
   * @param http {@link HttpSecurity} builder.
   * @return Built security filter chain.
   * @throws SecurityConfigurationException If authorization filter chain configuration fails to initialize.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    try {
      /*
       * CSRF is intentionally disabled. This API is stateless (JWT bearer tokens, no session cookies),
       * so CSRF attacks are not applicable. Disabling CSRF is safe for REST APIs that do not use
       * cookie-based authentication. See OWASP REST Security Cheat Sheet §CSRF.
       */
      http.csrf(csrf -> csrf.disable()) // NOSONAR java:S4502 — stateless JWT API, no CSRF risk
          // 2. Configure stateless session management
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          // 3. Configure authentication entry point for unauthorized requests
          .exceptionHandling(
              ex ->
                  ex.authenticationEntryPoint(
                      (request, response, authException) ->
                          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
          // 4. Gate /api/admin/** routes behind CURATOR or ADMIN roles
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers("/api/admin/**")
                      .hasAnyRole("CURATOR", "ADMIN")
                      .anyRequest()
                      .permitAll())
          // 5. Add custom JWT authentication filter before standard username/password filter
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
    } catch (Exception e) {
      throw new SecurityConfigurationException("Failed to configure SecurityFilterChain", e);
    }
  }
}
