package com.pokergto.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the {@code app.keycloak.*} keys from application.yml. */
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(String baseUrl, String realm, String clientId, String clientSecret) {}
