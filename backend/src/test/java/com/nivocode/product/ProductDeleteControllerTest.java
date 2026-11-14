
package com.nivocode.product;

import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProductDeleteControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    private RequestPostProcessor jwtFor(UUID companyId) {
        return jwt().jwt(token -> token
                .subject(UUID.randomUUID().toString())
                .claim("companyId", companyId.toString())
                .claim("role", "ADMIN")
        );
    }

    private Company createCompany(String name) {
        return companyRepository.save(
                new Company(
                        name,
                        "company-" + UUID.randomUUID()
                )
        );
    }

    private Product createProduct(
            Company company,
            String name,
            String sku
    ) {
        return productRepository.save(
                new Product(
                        company,
                        name,
                        sku,
                        "Test product",
                        new BigDecimal("150.00"),
                        10
                )
        );
    }

    @Test
    void shouldRejectDeleteWithoutJwt() throws Exception {
        mockMvc.perform(
                        delete("/api/products/{id}", UUID.randomUUID())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeactivateProductWithoutDeletingFromDatabase()
            throws Exception {

        Company company = createCompany("Company Soft Delete");

        Product product = createProduct(
                company,
                "Mechanical Keyboard",
                "KEY-001"
        );

        UUID productId = product.getId();

        assertTrue(product.isActive());

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNoContent());

        Product savedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertFalse(savedProduct.isActive());

        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/products")
                        .with(jwtFor(company.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectDeleteFromAnotherCompany() throws Exception {
        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        Product product = createProduct(
                companyA,
                "Private Notebook",
                "PRIVATE-001"
        );

        UUID productId = product.getId();

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(jwtFor(companyB.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Product not found"));

        Product savedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertTrue(savedProduct.isActive());

        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(jwtFor(companyA.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() throws Exception {
        Company company = createCompany("Company Missing");

        mockMvc.perform(delete(
                        "/api/products/{id}",
                        UUID.randomUUID()
                )
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Product not found"));
    }

    @Test
    void shouldRejectDeletingProductTwice() throws Exception {
        Company company = createCompany("Company Double Delete");

        Product product = createProduct(
                company,
                "Monitor",
                "MON-001"
        );

        UUID productId = product.getId();

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUpdatingInactiveProduct() throws Exception {
        Company company = createCompany("Company Inactive");

        Product product = createProduct(
                company,
                "Inactive Mouse",
                "MOUSE-001"
        );

        UUID productId = product.getId();

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/products/{id}", productId)
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Mouse",
                                  "sku": "MOUSE-001",
                                  "description": "Updated description",
                                  "price": 200.00,
                                  "stockQuantity": 20
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Product not found"));
    }
}
