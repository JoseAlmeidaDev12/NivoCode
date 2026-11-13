
package com.nivocode.company;

import com.nivocode.common.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company create(String name, String slug) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 160) {
            throw new IllegalArgumentException("Company name is too long");
        }

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Company slug is required");
        }

        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);

        if (normalizedSlug.length() > 80 ||
                !normalizedSlug.matches("[a-z0-9]+(-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("Invalid company slug");
        }

        if (companyRepository.existsBySlug(normalizedSlug)) {
            throw new ResourceConflictException("Company slug already exists");
        }

        Company company = new Company(normalizedName, normalizedSlug);

        return companyRepository.save(company);
    }
}
