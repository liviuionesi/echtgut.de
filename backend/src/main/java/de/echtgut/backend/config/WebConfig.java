package de.echtgut.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Web MVC and CORS configuration component. */
@Configuration
public class WebConfig {

  /**
   * Provides a fresh {@link RestClient.Builder} for beans (e.g. {@link
   * de.echtgut.backend.catalog.PlaceAggregatorService}) that build their own named clients off it —
   * a shared builder bean rather than each bean calling {@code RestClient.builder()} directly, so
   * tests can supply a builder bound to a mock server instead.
   *
   * @return A new, unconfigured {@link RestClient.Builder}.
   */
  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}
