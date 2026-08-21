package de.echtgut.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic Spring Boot context verification test.
 *
 * <p>Uses Spring Boot's {@link SpringBootTest} environment to verify that the application context
 * boots successfully without dependency injection failures.
 */
@SpringBootTest
@ActiveProfiles("test")
class EchtgutBackendApplicationTests {

  @Autowired private ApplicationContext applicationContext;

  /**
   * Verifies that the Spring Boot ApplicationContext initializes properly.
   *
   * <p>Ensures that essential core beans are wired cleanly upon application startup.
   */
  @Test
  @DisplayName("Spring Application Context loads successfully")
  void contextLoads() {
    // 1. Given the Spring container is bootstrapped
    // 2. When the application context is checked
    // 3. Then the context must not be null
    assertThat(applicationContext).isNotNull();
  }

  /**
   * Verifies that main method executes without throwing exceptions.
   */
  @Test
  @DisplayName("Main method runs successfully")
  void mainMethodRuns() {
    // 1. Given main method parameters
    // 2. When main method is executed
    // 3. Then no exception is thrown
    EchtgutBackendApplication.main(
        new String[] {"--spring.profiles.active=test", "--server.port=0"});
  }
}
