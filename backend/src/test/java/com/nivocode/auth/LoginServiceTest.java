
package com.nivocode.auth;

import com.nivocode.auth.dto.LoginRequest;
import com.nivocode.auth.dto.LoginResponse;
import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.user.User;
import com.nivocode.user.UserRepository;
import com.nivocode.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldLoginWithValidCredentials() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Company company = mock(Company.class);
        User user = mock(User.class);

        when(companyRepository.findBySlug("loja-demo"))
                .thenReturn(Optional.of(company));
        when(company.isActive()).thenReturn(true);
        when(company.getId()).thenReturn(companyId);

        when(userRepository.findByCompany_IdAndEmailIgnoreCase(
                companyId, "admin@empresa.com"))
                .thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getPasswordHash()).thenReturn("encoded-password");
        when(user.getId()).thenReturn(userId);
        when(user.getRole()).thenReturn(UserRole.ADMIN);

        when(passwordEncoder.matches(
                "SenhaSegura123!", "encoded-password"))
                .thenReturn(true);

        when(jwtTokenService.generateToken(
                userId, companyId, UserRole.ADMIN))
                .thenReturn("test.jwt.token");

        LoginRequest request = new LoginRequest(
                "loja-demo",
                "admin@empresa.com",
                "SenhaSegura123!"
        );

        LoginResponse response = loginService.login(request);

        assertNotNull(response);
        verify(jwtTokenService).generateToken(
                userId, companyId, UserRole.ADMIN
        );
    }

    @Test
    void shouldRejectInvalidPassword() {
        UUID companyId = UUID.randomUUID();

        Company company = mock(Company.class);
        User user = mock(User.class);

        when(companyRepository.findBySlug("loja-demo"))
                .thenReturn(Optional.of(company));
        when(company.isActive()).thenReturn(true);
        when(company.getId()).thenReturn(companyId);

        when(userRepository.findByCompany_IdAndEmailIgnoreCase(
                companyId, "admin@empresa.com"))
                .thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getPasswordHash()).thenReturn("encoded-password");

        when(passwordEncoder.matches(
                "SenhaErrada", "encoded-password"))
                .thenReturn(false);

        LoginRequest request = new LoginRequest(
                "loja-demo",
                "admin@empresa.com",
                "SenhaErrada"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldRejectUnknownCompany() {
        when(companyRepository.findBySlug("empresa-inexistente"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(
                "empresa-inexistente",
                "admin@empresa.com",
                "SenhaSegura123!"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtTokenService);
    }
}
