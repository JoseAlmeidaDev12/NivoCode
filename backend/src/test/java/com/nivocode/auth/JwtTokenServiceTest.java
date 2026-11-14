
package com.nivocode.auth;

import com.nivocode.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    @Test
    void shouldGenerateAndValidateJwtToken() {
        JwtConfig config = new JwtConfig();

        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        String encodedSecret = Base64.getEncoder()
                .encodeToString(keyBytes);

        SecretKey secretKey = config.jwtSecretKey(encodedSecret);

        JwtTokenService tokenService = new JwtTokenService(
                config.jwtEncoder(secretKey)
        );

        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = tokenService.generateToken(
                userId,
                companyId,
                UserRole.ADMIN
        );

        Jwt jwt = config.jwtDecoder(secretKey).decode(token);

        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals(companyId.toString(), jwt.getClaimAsString("companyId"));
        assertEquals("ADMIN", jwt.getClaimAsString("role"));
        assertEquals("https://auth.nivocode.example", jwt.getIssuer().toString());
        assertEquals(
                Duration.ofHours(1),
                Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())
        );
    }
}
