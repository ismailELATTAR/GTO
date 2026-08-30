package com.pokergto.config;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal liveness endpoint used to verify the Docker Compose wiring
 * (frontend → backend → Postgres) during Phase 1 scaffolding.
 */
@RestController
public class HealthController {

  @GetMapping("/api/health")
  public Map<String, Object> health() {
    return Map.of("status", "UP", "timestamp", Instant.now().toString());
  }
}
