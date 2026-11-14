
package com.nivocode.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentTenantProviderTest {

    private final CurrentTenantProvider provider =
            new CurrentTenantProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCompanyIdFromAuthenticatedJwt() {
        UUID companyId = UUID.randomUUID();

        authenticateWithCompanyId(companyId.toString());

        assertEquals(companyId, provider.getCompanyId());
    }

    @Test
    void shouldRejectUnauthenticatedUser() {
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                provider::getCompanyId
        );
    }

    @Test
    void shouldRejectJwtWithoutCompanyId() {
        authenticateWithCompanyId(null);

        assertThrows(
                AccessDeniedException.class,
                provider::getCompanyId
        );
    }

    @Test
    void shouldRejectInvalidCompanyId() {
        authenticateWithCompanyId("invalid-company-id");

        assertThrows(
                AccessDeniedException.class,
                provider::getCompanyId
        );
    }

    private void authenticateWithCompanyId(String companyId) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("test-user");

        if (companyId != null) {
            builder.claim("companyId", companyId);
        }

        Jwt jwt = builder.build();

        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
