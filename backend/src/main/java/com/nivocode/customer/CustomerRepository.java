
package com.nivocode.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByCompany_IdAndActiveTrue(
            UUID companyId
    );

    Optional<Customer> findByIdAndCompany_IdAndActiveTrue(
            UUID customerId,
            UUID companyId
    );

    boolean existsByCompany_IdAndDocument(
            UUID companyId,
            String document
    );
}
