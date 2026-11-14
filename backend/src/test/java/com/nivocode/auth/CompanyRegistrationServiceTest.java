
package com.nivocode.auth;

import com.nivocode.company.Company;
import com.nivocode.company.CompanyService;
import com.nivocode.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyRegistrationServiceTest {

    @Mock
    private CompanyService companyService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompanyRegistrationService registrationService;

    @Test
    void shouldRegisterCompanyAndInitialAdmin() {
        Company company = mock(Company.class);

        when(companyService.create("NivoCode", "nivocode"))
                .thenReturn(company);

        Company result = registrationService.register(
                "NivoCode",
                "nivocode",
                "Admin Test",
                "admin@example.com",
                "StrongPassword123!"
        );

        assertSame(company, result);

        InOrder inOrder = inOrder(companyService, userService);

        inOrder.verify(companyService)
                .create("NivoCode", "nivocode");

        inOrder.verify(userService).createInitialAdmin(
                company,
                "Admin Test",
                "admin@example.com",
                "StrongPassword123!"
        );
    }

    @Test
    void shouldPropagateAdminCreationFailure() {
        Company company = mock(Company.class);

        when(companyService.create("NivoCode", "nivocode"))
                .thenReturn(company);

        when(userService.createInitialAdmin(
                company,
                "Admin Test",
                "admin@example.com",
                "123"
        )).thenThrow(new IllegalArgumentException("Invalid password length"));

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.register(
                        "NivoCode",
                        "nivocode",
                        "Admin Test",
                        "admin@example.com",
                        "123"
                )
        );
    }
}
