
package com.nivocode.auth;

import com.nivocode.common.exception.GlobalExceptionHandler;
import com.nivocode.company.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private CompanyRegistrationService registrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RegistrationController controller =
                new RegistrationController(registrationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterCompanyAndReturnCreated() throws Exception {
        UUID companyId = UUID.randomUUID();
        Company company = mock(Company.class);

        when(company.getId()).thenReturn(companyId);
        when(company.getName()).thenReturn("Test Company");
        when(company.getSlug()).thenReturn("test-company");
        when(company.isActive()).thenReturn(true);

        when(registrationService.register(
                "Test Company",
                "test-company",
                "Admin Test",
                "admin@example.com",
                "StrongPassword123!"
        )).thenReturn(company);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Test Company",
                                  "companySlug": "test-company",
                                  "adminName": "Admin Test",
                                  "adminEmail": "admin@example.com",
                                  "adminPassword": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(companyId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Test Company"))
                .andExpect(jsonPath("$.slug")
                        .value("test-company"))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.passwordHash")
                        .doesNotExist());
    }

    @Test
    void shouldRejectInvalidRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Test Company",
                                  "companySlug": "test-company",
                                  "adminName": "Admin Test",
                                  "adminEmail": "invalid-email",
                                  "adminPassword": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrationService);
    }
}