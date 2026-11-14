
package com.nivocode.auth;

import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.user.User;
import com.nivocode.user.UserRepository;
import com.nivocode.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompanyRegistrationIntegrationTest {

    @Autowired
    private CompanyRegistrationService registrationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRollbackCompanyWhenAdminRegistrationFails() {
        String slug = "rollback-" + UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.register(
                        "Rollback Test",
                        slug,
                        "Admin Test",
                        "admin@example.com",
                        "123"
                )
        );

        assertFalse(companyRepository.existsBySlug(slug));
    }

    @Test
    void shouldPersistCompanyAndInitialAdmin() {
        String slug = "company-" + UUID.randomUUID();
        String email = "admin-" + UUID.randomUUID() + "@example.com";
        Company company = null;

        try {
            company = registrationService.register(
                    "Test Company",
                    slug,
                    "Admin Test",
                    email,
                    "StrongPassword123!"
            );

            Company savedCompany = companyRepository
                    .findBySlug(slug)
                    .orElseThrow();

            assertEquals(company.getId(), savedCompany.getId());

            User admin = userRepository
                    .findByCompany_IdAndEmailIgnoreCase(
                            company.getId(),
                            email
                    )
                    .orElseThrow();

            assertEquals(UserRole.ADMIN, admin.getRole());
            assertEquals("Admin Test", admin.getName());
            assertEquals(company.getId(), admin.getCompany().getId());

            assertNotEquals(
                    "StrongPassword123!",
                    admin.getPasswordHash()
            );

            assertTrue(passwordEncoder.matches(
                    "StrongPassword123!",
                    admin.getPasswordHash()
            ));

        } finally {
            if (company != null) {
                userRepository
                        .findByCompany_IdAndEmailIgnoreCase(
                                company.getId(),
                                email
                        )
                        .ifPresent(userRepository::delete);

                companyRepository.deleteById(company.getId());
            }
        }
    }
}
