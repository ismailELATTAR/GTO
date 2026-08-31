package com.pokergto.auth.service;

import com.pokergto.auth.dto.AuthResponse;
import com.pokergto.auth.dto.LoginRequest;
import com.pokergto.auth.dto.RegisterRequest;
import com.pokergto.auth.dto.UserSummary;
import com.pokergto.common.exception.ApiException;
import com.pokergto.common.security.KeycloakClient;
import com.pokergto.common.security.KeycloakTokenResponse;
import com.pokergto.user.model.User;
import com.pokergto.user.model.UserProfile;
import com.pokergto.user.model.UserRole;
import com.pokergto.user.repository.UserProfileRepository;
import com.pokergto.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates registration/login/refresh/logout against Keycloak, keeping
 * the local `users`/`user_profiles` rows in sync. Keycloak owns identity;
 * this service's job is translating between "what Keycloak just told us"
 * and "what our local User row says" — see the class-level note on
 * {@code User} for why that split exists.
 */
@Service
public class AuthService {

  private final KeycloakClient keycloakClient;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final JwtDecoder jwtDecoder;

  public AuthService(
      KeycloakClient keycloakClient,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository,
      JwtDecoder jwtDecoder) {
    this.keycloakClient = keycloakClient;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.jwtDecoder = jwtDecoder;
  }

  @Transactional
  public AuthResult register(RegisterRequest request) {
    UUID keycloakUserId = keycloakClient.createUser(request.email(), request.displayName(), request.password());

    User user =
        User.builder()
            .id(keycloakUserId)
            .email(request.email())
            .displayName(request.displayName())
            .role(UserRole.USER)
            .lastLoginAt(Instant.now())
            .build();
    // save() on an entity with a manually-assigned @Id goes through
    // EntityManager.merge() (not persist()) — it returns a *new* managed
    // copy rather than attaching `user` itself, so the managed copy is
    // what downstream code (and the UserProfile association below) must use.
    user = userRepository.save(user);
    userProfileRepository.save(UserProfile.builder().user(user).build());

    KeycloakTokenResponse tokens = keycloakClient.passwordLogin(request.email(), request.password());
    return toResult(tokens, user);
  }

  @Transactional
  public AuthResult login(LoginRequest request) {
    KeycloakTokenResponse tokens = keycloakClient.passwordLogin(request.email(), request.password());

    // The "sub" claim is the same Keycloak user id we'd have stored locally
    // on registration — decoding the token we just received (rather than
    // trusting the submitted email alone) is what ties the two together.
    UUID userId = extractUserId(tokens.accessToken());
    User user = findOrProvisionLocalUser(userId, request.email());
    user.setLastLoginAt(Instant.now());
    user = userRepository.save(user);

    return toResult(tokens, user);
  }

  @Transactional(readOnly = true)
  public AuthResult refresh(String refreshToken) {
    KeycloakTokenResponse tokens = keycloakClient.refresh(refreshToken);
    UUID userId = extractUserId(tokens.accessToken());
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.unauthorized("Unknown user"));
    return toResult(tokens, user);
  }

  public void logout(String refreshToken) {
    if (refreshToken != null) {
      keycloakClient.logout(refreshToken);
    }
  }

  /** Handles a user that exists in Keycloak but was never created through our /register (e.g. added via the Keycloak admin console). */
  private User findOrProvisionLocalUser(UUID userId, String email) {
    return userRepository
        .findById(userId)
        .orElseGet(
            () -> {
              User provisioned =
                  User.builder().id(userId).email(email).displayName(email).role(UserRole.USER).build();
              provisioned = userRepository.save(provisioned);
              userProfileRepository.save(UserProfile.builder().user(provisioned).build());
              return provisioned;
            });
  }

  private UUID extractUserId(String accessToken) {
    Jwt jwt = jwtDecoder.decode(accessToken);
    return UUID.fromString(jwt.getSubject());
  }

  private AuthResult toResult(KeycloakTokenResponse tokens, User user) {
    var summary = new UserSummary(user.getId(), user.getEmail(), user.getDisplayName());
    var response = new AuthResponse(tokens.accessToken(), summary);
    return new AuthResult(response, tokens.refreshToken(), tokens.refreshExpiresInSeconds());
  }
}
