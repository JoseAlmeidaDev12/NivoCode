
package com.nivocode.product;

import com.nivocode.common.exception.ResourceConflictException;
import com.nivocode.common.security.CurrentTenantProvider;
import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.product.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CurrentTenantProvider currentTenantProvider;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldListProductsForAuthenticatedCompany() {
        UUID companyId = UUID.randomUUID();

        when(currentTenantProvider.getCompanyId())
                .thenReturn(companyId);

        when(productRepository.findAllByCompany_IdAndActiveTrue(companyId))
                .thenReturn(List.of());

        List<Product> products = productService.findAll();

        assertEquals(0, products.size());

        verify(currentTenantProvider).getCompanyId();

        verify(productRepository)
                .findAllByCompany_IdAndActiveTrue(companyId);
    }

    @Test
    void shouldCreateProductForAuthenticatedCompany() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company("Company A", "company-a");

        ProductRequest request = new ProductRequest(
                "Keyboard",
                "KEY-001",
                "Mechanical keyboard",
                new BigDecimal("150.00"),
                10
        );

        when(currentTenantProvider.getCompanyId())
                .thenReturn(companyId);

        when(companyRepository.findById(companyId))
                .thenReturn(Optional.of(company));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.create(request);

        assertSame(company, product.getCompany());
        assertEquals("Keyboard", product.getName());
        assertEquals("KEY-001", product.getSku());
        assertEquals(new BigDecimal("150.00"), product.getPrice());
        assertEquals(10, product.getStockQuantity());

        verify(productRepository)
                .existsByCompany_IdAndSku(companyId, "KEY-001");

        verify(companyRepository).findById(companyId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldRejectDuplicateSkuWithinSameCompany() {
        UUID companyId = UUID.randomUUID();

        ProductRequest request = new ProductRequest(
                "Mouse",
                "MOUSE-001",
                null,
                new BigDecimal("80.00"),
                5
        );

        when(currentTenantProvider.getCompanyId())
                .thenReturn(companyId);

        when(productRepository.existsByCompany_IdAndSku(
                companyId, "MOUSE-001"
        )).thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }
}
