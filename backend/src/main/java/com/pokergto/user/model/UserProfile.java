package com.pokergto.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional profile fields split out from {@link User} (§4 of ARCHITECTURE.md)
 * so the auth-critical table stays lean. Created empty alongside the user
 * on registration.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

  // Present because @Id requires a plain field, but @MapsId derives its
  // value from `user` below at save time — setting THIS field directly
  // (e.g. via the builder) does nothing; Hibernate ignores it unless
  // `user` is also set. Always construct via `.user(someUser)`, never
  // `.userId(...)`.
  @Id
  @Column(name = "user_id")
  private UUID userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "avatar_url")
  private String avatarUrl;

  private String bio;

  private String timezone;
}
