
package com.nivocode.common.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentTenantProvider {

    public UUID getCompanyId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated user is required"
            );
        }

        String companyId = jwt.getClaimAsString("companyId");

        if (companyId == null || companyId.isBlank()) {
            throw new AccessDeniedException(
                    "Company identification is required"
            );
        }

        try {
            return UUID.fromString(companyId);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "Invalid company identification",
                    exception
            );
        }
    }
}
