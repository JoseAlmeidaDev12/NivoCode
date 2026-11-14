
package com.nivocode.customer;

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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CustomerControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

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

    private Customer createCustomer(
            Company company,
            String name,
            String document
    ) {
        return customerRepository.save(
                new Customer(
                        company,
                        name,
                        "customer@example.com",
                        "11999999999",
                        document,
                        "Test address"
                )
        );
    }

    @Test
    void shouldRejectCustomerListWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCustomerCreationWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao Silva",
                                  "email": "joao@example.com"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateCustomerForAuthenticatedCompany() throws Exception {
        Company company = createCompany("Company Create");

        mockMvc.perform(post("/api/customers")
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao Silva",
                                  "email": "joao@example.com",
                                  "phone": "11999999999",
                                  "document": "12345678900",
                                  "address": "Rua das Flores, 100"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Joao Silva"))
                .andExpect(jsonPath("$.email")
                        .value("joao@example.com"))
                .andExpect(jsonPath("$.phone")
                        .value("11999999999"))
                .andExpect(jsonPath("$.document")
                        .value("12345678900"))
                .andExpect(jsonPath("$.active").value(true));

        List<Customer> customers = customerRepository
                .findAllByCompany_IdAndActiveTrue(company.getId());

        assertEquals(1, customers.size());
        assertEquals("Joao Silva", customers.get(0).getName());
        assertEquals(
                company.getId(),
                customers.get(0).getCompany().getId()
        );
    }

    @Test
    void shouldListOnlyCustomersFromAuthenticatedCompany()
            throws Exception {

        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        createCustomer(
                companyA,
                "Customer A",
                "11111111111"
        );

        createCustomer(
                companyB,
                "Customer B",
                "22222222222"
        );

        mockMvc.perform(get("/api/customers")
                        .with(jwtFor(companyA.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Customer A"));

        mockMvc.perform(get("/api/customers")
                        .with(jwtFor(companyB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name")
                        .value("Customer B"));
    }

    @Test
    void shouldRejectDuplicateDocumentWithinSameCompany()
            throws Exception {

        Company company = createCompany("Company Duplicate");

        createCustomer(
                company,
                "First Customer",
                "12345678900"
        );

        mockMvc.perform(post("/api/customers")
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Second Customer",
                                  "email": "second@example.com",
                                  "document": "12345678900"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value(
                                "Customer document already exists in this company"
                        ));
    }

    @Test
    void shouldAllowSameDocumentInDifferentCompanies()
            throws Exception {

        Company companyA = createCompany("Company A Document");
        Company companyB = createCompany("Company B Document");

        createCustomer(
                companyA,
                "Customer Company A",
                "12345678900"
        );

        mockMvc.perform(post("/api/customers")
                        .with(jwtFor(companyB.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Customer Company B",
                                  "email": "companyb@example.com",
                                  "document": "12345678900"
                                }
                                """))
                .andExpect(status().isCreated());

        assertEquals(
                1,
                customerRepository
                        .findAllByCompany_IdAndActiveTrue(companyA.getId())
                        .size()
        );

        assertEquals(
                1,
                customerRepository
                        .findAllByCompany_IdAndActiveTrue(companyB.getId())
                        .size()
        );
    }

    @Test
    void shouldRejectInvalidCustomerData() throws Exception {
        Company company = createCompany("Company Validation");

        mockMvc.perform(post("/api/customers")
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "phone": "11999999999"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}