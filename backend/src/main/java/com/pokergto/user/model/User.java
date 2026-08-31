package com.pokergto.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The app's local record of a user. Keycloak is the source of truth for
 * identity and credentials (no password field here at all) — this table
 * exists so app data (progress, XP, attempts, …) has a local row to FK
 * against, and so display data doesn't require a Keycloak lookup on every
 * read.
 *
 * <p>{@code id} is NOT generated here — it's always set to the Keycloak
 * user's {@code sub} claim (their Keycloak user id), so this row and the
 * Keycloak account are the same identity, just mirrored.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserRole role = UserRole.USER;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
