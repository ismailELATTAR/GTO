package com.pokergto.common.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Shape of Keycloak's token endpoint response (snake_case on the wire). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") long expiresInSeconds,
    @JsonProperty("refresh_expires_in") long refreshExpiresInSeconds) {}
