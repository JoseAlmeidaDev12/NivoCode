
package com.nivocode.customer;

import com.nivocode.common.exception.ResourceConflictException;
import com.nivocode.common.exception.ResourceNotFoundException;
import com.nivocode.common.security.CurrentTenantProvider;
import com.nivocode.company.Company;
import com.nivocode.company.CompanyRepository;
import com.nivocode.customer.dto.CustomerRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final CurrentTenantProvider currentTenantProvider;

    public CustomerService(
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            CurrentTenantProvider currentTenantProvider
    ) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        UUID companyId = currentTenantProvider.getCompanyId();

        return customerRepository
                .findAllByCompany_IdAndActiveTrue(companyId);
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID customerId) {
        UUID companyId = currentTenantProvider.getCompanyId();

        return customerRepository
                .findByIdAndCompany_IdAndActiveTrue(
                        customerId, companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found"
                ));
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        UUID companyId = currentTenantProvider.getCompanyId();

        String name = request.name().trim();
        String email = normalizeOptional(request.email());
        String phone = normalizeOptional(request.phone());
        String document = normalizeOptional(request.document());
        String address = normalizeOptional(request.address());

        if (document != null &&
                customerRepository.existsByCompany_IdAndDocument(
                        companyId, document
                )) {
            throw new ResourceConflictException(
                    "Customer document already exists in this company"
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated company not found"
                ));

        Customer customer = new Customer(
                company, name, email, phone, document, address
        );

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(UUID customerId, CustomerRequest request) {
        UUID companyId = currentTenantProvider.getCompanyId();

        Customer customer = customerRepository
                .findByIdAndCompany_IdAndActiveTrue(
                        customerId, companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found"
                ));

        String name = request.name().trim();
        String email = normalizeOptional(request.email());
        String phone = normalizeOptional(request.phone());
        String document = normalizeOptional(request.document());
        String address = normalizeOptional(request.address());

        if (document != null
                && !document.equals(customer.getDocument())
                && customerRepository.existsByCompany_IdAndDocument(
                companyId, document
        )) {
            throw new ResourceConflictException(
                    "Customer document already exists in this company"
            );
        }

        customer.updateDetails(
                name, email, phone, document, address
        );

        return customerRepository.save(customer);
    }

    @Transactional
    public void deactivate(UUID customerId) {
        UUID companyId = currentTenantProvider.getCompanyId();

        Customer customer = customerRepository
                .findByIdAndCompany_IdAndActiveTrue(
                        customerId, companyId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found"
                ));

        customer.deactivate();
        customerRepository.save(customer);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
