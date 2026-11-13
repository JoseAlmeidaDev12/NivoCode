
package com.nivocode.company;

import com.nivocode.common.exception.ResourceConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void shouldCreateCompanySuccessfully() {
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Company company = companyService.create(
                "  Minha Loja  ",
                "MINHA-LOJA"
        );

        assertEquals("Minha Loja", company.getName());
        assertEquals("minha-loja", company.getSlug());
        assertTrue(company.isActive());

        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void shouldRejectBlankCompanyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> companyService.create("  ", "minha-loja")
        );

        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateSlug() {
        when(companyRepository.existsBySlug("minha-loja"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> companyService.create("Minha Loja", "minha-loja")
        );

        verify(companyRepository, never()).save(any());
    }
}
