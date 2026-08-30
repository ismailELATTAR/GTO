package com.pokergto.common.security;

import com.pokergto.common.exception.ApiException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Everything the backend needs to say to Keycloak, in one place. Two kinds
 * of calls happen here, both server-side only — the browser never talks to
 * Keycloak directly (that's the "custom in-app form" choice: the frontend
 * only ever calls our own /api/auth/* endpoints, which proxy to Keycloak):
 *
 *  1. Admin REST API  (bearer = this backend's own service-account token)
 *     — used once, to create the Keycloak account on registration.
 *  2. Token endpoint  (OAuth2 "grant" calls, no bearer token needed)
 *     — used for login (password grant), refresh, and logout.
 */
@Component
public class KeycloakClient {

  private final RestClient restClient;
  private final KeycloakProperties properties;

  public KeycloakClient(KeycloakProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
  }

  /** Creates the Keycloak account and returns its user id (the future "sub" claim). */
  public UUID createUser(String email, String displayName, String password) {
    String adminToken = fetchAdminAccessToken();

    // Keycloak's default User Profile config (24+) requires BOTH firstName
    // and lastName to consider an account "fully set up" — omitting either
    // makes every subsequent password-grant login fail with a vague
    // "Account is not fully set up" error, discovered the hard way.
    // We only collect a single displayName, so split it heuristically.
    String[] nameParts = displayName.trim().split("\\s+", 2);
    String firstName = nameParts[0];
    String lastName = nameParts.length > 1 ? nameParts[1] : nameParts[0];

    Map<String, Object> body =
        Map.of(
            "username", email,
            "email", email,
            "firstName", firstName,
            "lastName", lastName,
            "enabled", true,
            "emailVerified", true,
            "credentials",
                List.of(Map.of("type", "password", "value", password, "temporary", false)));

    try {
      URI location =
          restClient
              .post()
              .uri("/admin/realms/{realm}/users", properties.realm())
              .header("Authorization", "Bearer " + adminToken)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .toBodilessEntity()
              .getHeaders()
              .getLocation();

      if (location == null) {
        throw new ApiException(
            HttpStatus.INTERNAL_SERVER_ERROR, "KEYCLOAK_ERROR", "Keycloak did not return a user location");
      }
      // Location is ".../admin/realms/pokergto/users/{id}" — the id is the last segment.
      String path = location.getPath();
      return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.CONFLICT) {
        throw ApiException.conflict("An account with this email already exists");
      }
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "KEYCLOAK_ERROR", "Failed to create account");
    }
  }

  /** Resource Owner Password Credentials grant — exchanges email+password for tokens. */
  public KeycloakTokenResponse passwordLogin(String email, String password) {
    MultiValueMap<String, String> form = baseForm();
    form.add("grant_type", "password");
    form.add("username", email);
    form.add("password", password);
    form.add("scope", "openid");
    return requestToken(form, "Invalid email or password");
  }

  /** Refresh-token grant — exchanges a still-valid refresh token for a new token pair. */
  public KeycloakTokenResponse refresh(String refreshToken) {
    MultiValueMap<String, String> form = baseForm();
    form.add("grant_type", "refresh_token");
    form.add("refresh_token", refreshToken);
    return requestToken(form, "Session expired, please log in again");
  }

  /** Revokes the refresh token's session in Keycloak (best-effort — failures are ignored). */
  public void logout(String refreshToken) {
    MultiValueMap<String, String> form = baseForm();
    form.add("refresh_token", refreshToken);
    try {
      restClient
          .post()
          .uri("/realms/{realm}/protocol/openid-connect/logout", properties.realm())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientResponseException ignored) {
      // Logout is best-effort: an already-expired/invalid refresh token is not an error for the caller.
    }
  }

  /** Client-credentials grant using our own service account — used only for Admin API calls. */
  private String fetchAdminAccessToken() {
    MultiValueMap<String, String> form = baseForm();
    form.add("grant_type", "client_credentials");
    return requestToken(form, "Failed to authenticate with Keycloak").accessToken();
  }

  private KeycloakTokenResponse requestToken(MultiValueMap<String, String> form, String unauthorizedMessage) {
    try {
      return restClient
          .post()
          .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(KeycloakTokenResponse.class);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw ApiException.unauthorized(unauthorizedMessage);
      }
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "KEYCLOAK_ERROR", "Keycloak request failed");
    }
  }

  private MultiValueMap<String, String> baseForm() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", properties.clientId());
    form.add("client_secret", properties.clientSecret());
    return form;
  }
}
