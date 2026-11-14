
package com.nivocode.user;

import com.nivocode.common.exception.ResourceConflictException;
import com.nivocode.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                new BCryptPasswordEncoder(12)
        );
    }

    @Test
    void shouldCreateAdminWithHashedPassword() {
        Company company = createActiveCompany();

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.createInitialAdmin(
                company,
                "Admin Test",
                " ADMIN@EXAMPLE.COM ",
                "StrongPassword123!"
        );

        assertEquals("Admin Test", user.getName());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertSame(company, user.getCompany());
        assertNotEquals("StrongPassword123!", user.getPasswordHash());

        assertTrue(new BCryptPasswordEncoder().matches(
                "StrongPassword123!",
                user.getPasswordHash()
        ));
    }

    @Test
    void shouldRejectDuplicateEmailInSameCompany() {
        Company company = createActiveCompany();

        when(userRepository.existsByCompany_IdAndEmailIgnoreCase(
                company.getId(),
                "admin@example.com"
        )).thenReturn(true);

        assertThrows(ResourceConflictException.class, () ->
                userService.createInitialAdmin(
                        company,
                        "Admin Test",
                        "admin@example.com",
                        "StrongPassword123!"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectWeakPassword() {
        Company company = createActiveCompany();

        assertThrows(IllegalArgumentException.class, () ->
                userService.createInitialAdmin(
                        company,
                        "Admin Test",
                        "admin@example.com",
                        "123"
                )
        );

        verifyNoInteractions(userRepository);
    }

    private Company createActiveCompany() {
        Company company = mock(Company.class);

        when(company.getId()).thenReturn(UUID.randomUUID());
        when(company.isActive()).thenReturn(true);

        return company;
    }
}
