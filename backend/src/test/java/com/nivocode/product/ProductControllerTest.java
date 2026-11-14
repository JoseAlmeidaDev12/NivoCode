
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private CompanyRepository companyRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void shouldRejectProductsRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListProductsForAuthenticatedCompany() throws Exception {
        UUID companyId = UUID.randomUUID();

        mockMvc.perform(get("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyId.toString())
                                .claim("role", "ADMIN")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldRejectProductCreationWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Keyboard",
                                  "sku": "KEY-001",
                                  "price": 150.00,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidProductData() throws Exception {
        UUID companyId = UUID.randomUUID();

        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyId.toString())
                                .claim("role", "ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "sku": "",
                                  "price": -50.00,
                                  "stockQuantity": -5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldCreateProductForAuthenticatedCompany() throws Exception {
        Company company = companyRepository.save(
                new Company(
                        "Test Company",
                        "test-company-" + UUID.randomUUID()
                )
        );

        UUID companyId = company.getId();

        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyId.toString())
                                .claim("role", "ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Mechanical Keyboard",
                                  "sku": "KEY-001",
                                  "description": "RGB keyboard",
                                  "price": 150.00,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.sku").value("KEY-001"))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @Transactional
    void shouldIsolateProductsBetweenCompanies() throws Exception {

        Company companyA = companyRepository.save(
                new Company(
                        "Company A",
                        "company-a-" + UUID.randomUUID()
                )
        );

        Company companyB = companyRepository.save(
                new Company(
                        "Company B",
                        "company-b-" + UUID.randomUUID()
                )
        );

        UUID companyAId = companyA.getId();
        UUID companyBId = companyB.getId();

        // Create a product belonging to Company A
        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyAId.toString())
                                .claim("role", "ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Notebook Company A",
                                  "sku": "PROD-001",
                                  "price": 3500.00,
                                  "stockQuantity": 5
                                }
                                """))
                .andExpect(status().isCreated());

        // Create a product belonging to Company B
        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyBId.toString())
                                .claim("role", "ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Monitor Company B",
                                  "sku": "PROD-001",
                                  "price": 1200.00,
                                  "stockQuantity": 8
                                }
                                """))
                .andExpect(status().isCreated());

        // Company A must see only its own product
        mockMvc.perform(get("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyAId.toString())
                                .claim("role", "ADMIN")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Notebook Company A"));

        // Company B must see only its own product
        mockMvc.perform(get("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("companyId", companyBId.toString())
                                .claim("role", "ADMIN")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Monitor Company B"));
    }
}
