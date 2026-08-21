package de.echtgut.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for Spring Boot Actuator endpoints.
 *
 * <p>Uses {@link MockMvc} to perform web layer requests against the actuator health endpoint to
 * verify system observability requirements.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorHealthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  /**
   * Verifies that the actuator health endpoint returns HTTP 200 OK with status UP.
   *
   * <p>Ensures that container health checks and deployment probes can reliably inspect the backend.
   *
   * @throws Exception if mock MVC request execution fails.
   */
  @Test
  @DisplayName("GET /actuator/health returns HTTP 200 OK with status UP")
  void healthEndpointReturnsUp() throws Exception {
    // 1. Given the application is running
    // 2. When calling GET /actuator/health
    // 3. Then the response status should be 200 OK and status should be UP
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
