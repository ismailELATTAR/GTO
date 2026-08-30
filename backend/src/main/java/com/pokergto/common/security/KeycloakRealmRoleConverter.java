package com.pokergto.common.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Spring Security's default JWT-to-authorities mapping expects a flat
 * {@code scope} claim. Keycloak instead nests realm roles as:
 *
 * <pre>{@code "realm_access": { "roles": ["user", "offline_access", ...] } }</pre>
 *
 * This reads that shape and turns each role into a {@code ROLE_*} authority
 * (Spring Security's convention — {@code hasRole("ADMIN")} checks for the
 * authority {@code "ROLE_ADMIN"}), so {@code .hasRole("ADMIN")} in
 * SecurityConfig works against Keycloak-issued tokens.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
      return List.of();
    }
    return roles.stream()
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
        .toList();
  }
}
