package com.nivocode.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class CurrentUserController {

    @GetMapping("/me")
    public Map<String, String> currentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Map.of(
                "userId", jwt.getSubject(),
                "companyId", jwt.getClaimAsString("companyId"),
                "role", jwt.getClaimAsString("role")
        );
    }
}