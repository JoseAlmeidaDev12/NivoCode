
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
class CustomerCrudControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;

    private static final String UPDATE_JSON = """
            {
              "name": "Updated Customer",
              "email": "updated@example.com",
              "phone": "19999999999",
              "document": "98765432100",
              "address": "New Address, 100"
            }
            """;

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
                        "Original Address"
                )
        );
    }

    @Test
    void shouldRejectFindByIdWithoutJwt() throws Exception {
        mockMvc.perform(
                        get("/api/customers/{id}", UUID.randomUUID())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFindOwnCustomerById() throws Exception {
        Company company = createCompany("Company Search");

        Customer customer = createCustomer(
                company,
                "Customer A",
                "11111111111"
        );

        mockMvc.perform(get("/api/customers/{id}", customer.getId())
                        .with(jwtFor(company.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(customer.getId().toString()))
                .andExpect(jsonPath("$.name")
                        .value("Customer A"))
                .andExpect(jsonPath("$.document")
                        .value("11111111111"))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }

    @Test
    void shouldHideOtherCompanyAndMissingCustomers()
            throws Exception {

        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        Customer customer = createCustomer(
                companyA,
                "Private Customer",
                "11111111111"
        );

        mockMvc.perform(get("/api/customers/{id}", customer.getId())
                        .with(jwtFor(companyB.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Customer not found"));

        mockMvc.perform(get(
                        "/api/customers/{id}",
                        UUID.randomUUID()
                )
                        .with(jwtFor(companyA.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Customer not found"));
    }

    @Test
    void shouldRejectUpdateWithoutJwt() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateOwnCustomer() throws Exception {
        Company company = createCompany("Company Update");

        Customer customer = createCustomer(
                company,
                "Original Customer",
                "11111111111"
        );

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(customer.getId().toString()))
                .andExpect(jsonPath("$.name")
                        .value("Updated Customer"))
                .andExpect(jsonPath("$.email")
                        .value("updated@example.com"))
                .andExpect(jsonPath("$.phone")
                        .value("19999999999"))
                .andExpect(jsonPath("$.document")
                        .value("98765432100"))
                .andExpect(jsonPath("$.address")
                        .value("New Address, 100"));

        Customer savedCustomer = customerRepository
                .findById(customer.getId())
                .orElseThrow();

        assertTrue(savedCustomer.isActive());
    }

    @Test
    void shouldRejectUpdateFromAnotherCompany()
            throws Exception {

        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        Customer customer = createCustomer(
                companyA,
                "Private Customer",
                "11111111111"
        );

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                        .with(jwtFor(companyB.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Customer not found"));
    }

    @Test
    void shouldRejectDuplicateDocumentOnUpdate()
            throws Exception {

        Company company = createCompany("Company Duplicate");

        Customer firstCustomer = createCustomer(
                company,
                "First Customer",
                "11111111111"
        );

        createCustomer(
                company,
                "Second Customer",
                "98765432100"
        );

        mockMvc.perform(put(
                        "/api/customers/{id}",
                        firstCustomer.getId()
                )
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value(
                                "Customer document already exists in this company"
                        ));
    }

    @Test
    void shouldRejectInvalidUpdateData() throws Exception {
        Company company = createCompany("Company Validation");

        Customer customer = createCustomer(
                company,
                "Valid Customer",
                "11111111111"
        );

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
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

    @Test
    void shouldRejectDeleteWithoutJwt() throws Exception {
        mockMvc.perform(
                        delete("/api/customers/{id}", UUID.randomUUID())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSoftDeleteOwnCustomer() throws Exception {
        Company company = createCompany("Company Soft Delete");

        Customer customer = createCustomer(
                company,
                "Customer To Delete",
                "11111111111"
        );

        UUID customerId = customer.getId();

        mockMvc.perform(delete("/api/customers/{id}", customerId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNoContent());

        Customer savedCustomer = customerRepository
                .findById(customerId)
                .orElseThrow();

        assertFalse(savedCustomer.isActive());

        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/customers")
                        .with(jwtFor(company.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectDeleteFromAnotherCompany()
            throws Exception {

        Company companyA = createCompany("Company A");
        Company companyB = createCompany("Company B");

        Customer customer = createCustomer(
                companyA,
                "Private Customer",
                "11111111111"
        );

        mockMvc.perform(delete("/api/customers/{id}", customer.getId())
                        .with(jwtFor(companyB.getId())))
                .andExpect(status().isNotFound());

        assertTrue(
                customerRepository.findById(customer.getId())
                        .orElseThrow()
                        .isActive()
        );
    }

    @Test
    void shouldRejectSecondDeleteAndUpdateAfterDeactivation()
            throws Exception {

        Company company = createCompany("Company Inactive");

        Customer customer = createCustomer(
                company,
                "Inactive Customer",
                "11111111111"
        );

        UUID customerId = customer.getId();

        mockMvc.perform(delete("/api/customers/{id}", customerId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/customers/{id}", customerId)
                        .with(jwtFor(company.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .with(jwtFor(company.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Customer not found"));
    }
}
