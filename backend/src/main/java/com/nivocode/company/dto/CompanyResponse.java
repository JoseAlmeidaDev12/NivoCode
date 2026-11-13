
package com.nivocode.company.dto;

import com.nivocode.company.Company;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String slug,
        boolean active,
        OffsetDateTime createdAt
) {
    public static CompanyResponse fromEntity(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getSlug(),
                company.isActive(),
                company.getCreatedAt()
        );
    }
}
