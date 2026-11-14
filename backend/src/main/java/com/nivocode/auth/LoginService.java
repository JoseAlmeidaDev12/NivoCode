
package com.nivocode.auth;

import com.nivocode.auth.dto.LoginRequest;
import com.nivocode.auth.dto.LoginResponse;
import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.user.User;
import com.nivocode.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class LoginService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public LoginService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        String companySlug = request.companySlug()
                .trim()
                .toLowerCase(Locale.ROOT);

        Company company = companyRepository.findBySlug(companySlug)
                .orElseThrow(this::invalidCredentials);

        if (!company.isActive()) {
            throw invalidCredentials();
        }

        User user = userRepository
                .findByCompany_IdAndEmailIgnoreCase(
                        company.getId(),
                        request.email().trim()
                )
                .orElseThrow(this::invalidCredentials);

        if (!user.isActive()
                || !passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw invalidCredentials();
        }

        String token = jwtTokenService.generateToken(
                user.getId(),
                company.getId(),
                user.getRole()
        );

        return new LoginResponse(token, "Bearer", 3600);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
    }
}
