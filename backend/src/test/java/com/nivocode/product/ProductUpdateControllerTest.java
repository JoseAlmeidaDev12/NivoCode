
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProductUpdateControllerTest {

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
                        "Original description",
                        new BigDecimal("100.00"),
                        10
                )
        );
    }

    @Test
    void shouldRejectUpdateWithoutJwt() throws Exception {
        mockMvc.perform(put("/api/products/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Product",
                                  "sku": "SKU-001",
                                  "price": 200.00,
                                  "stockQuantity": 20
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateOwnProduct() throws Exception {
        Company company = createCompany("Company Update");

        Product product = createProduct(
                company,
                "Original Product",
                "SKU-001"
        );

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Product",
                                  "sku": "SKU-001",
                                  "description": "Updated description",
                                  "price": 250.00,
                                  "stockQuantity": 25
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(product.getId().toString()))
                .andExpect(jsonPath("$.name")
                        .value("Updated Product"))
                .andExpect(jsonPath("$.sku")
                        .value("SKU-001"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.price")
                        .value(250.00))
                .andExpect(jsonPath("$.stockQuantity")
                        .value(25));
    }

    @Test
    void shouldRejectUpdateFromAnotherCompany() throws Exception {
        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        Product product = createProduct(
                companyA,
                "Private Product",
                "PRIVATE-001"
        );

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .with(jwtFor(companyB.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unauthorized Update",
                                  "sku": "HACK-001",
                                  "price": 1.00,
                                  "stockQuantity": 999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Product not found"));
    }

    @Test
    void shouldRejectDuplicatedSku() throws Exception {
        Company company = createCompany("Company Duplicate");

        Product firstProduct = createProduct(
                company,
                "First Product",
                "SKU-001"
        );

        createProduct(
                company,
                "Second Product",
                "SKU-002"
        );

        mockMvc.perform(put("/api/products/{id}", firstProduct.getId())
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Product",
                                  "sku": "SKU-002",
                                  "price": 300.00,
                                  "stockQuantity": 30
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidUpdateData() throws Exception {
        Company company = createCompany("Company Validation");

        Product product = createProduct(
                company,
                "Valid Product",
                "SKU-001"
        );

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "sku": "",
                                  "price": -100.00,
                                  "stockQuantity": -5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() throws Exception {
        Company company = createCompany("Company Missing");

        mockMvc.perform(put("/api/products/{id}", UUID.randomUUID())
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing Product",
                                  "sku": "MISSING-001",
                                  "price": 100.00,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Product not found"));
    }
}
