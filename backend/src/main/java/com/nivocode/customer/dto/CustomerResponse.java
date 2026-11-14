
package com.nivocode.customer.dto;

import com.nivocode.customer.Customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String document,
        String address,
        boolean active,
        OffsetDateTime createdAt
) {

    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDocument(),
                customer.getAddress(),
                customer.isActive(),
                customer.getCreatedAt()
        );
    }
}
