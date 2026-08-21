package de.echtgut.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the echtgut.de Spring Boot backend service.
 *
 * <p>This application serves both the internal Curator Admin Portal and the public visitor site
 * through REST APIs backed by PostgreSQL and Flyway database migrations.
 */
@SpringBootApplication
public class EchtgutBackendApplication {

  /**
   * Application main method.
   *
   * @param args Command line arguments passed to the application.
   */
  public static void main(String[] args) {
    SpringApplication.run(EchtgutBackendApplication.class, args);
  }
}
