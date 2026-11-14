
package com.nivocode.auth;

import com.nivocode.company.Company;
import com.nivocode.company.CompanyService;
import com.nivocode.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyRegistrationService {

    private final CompanyService companyService;
    private final UserService userService;

    public CompanyRegistrationService(
            CompanyService companyService,
            UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @Transactional
    public Company register(
            String companyName,
            String companySlug,
            String adminName,
            String adminEmail,
            String adminPassword) {

        Company company = companyService.create(
                companyName,
                companySlug
        );

        userService.createInitialAdmin(
                company,
                adminName,
                adminEmail,
                adminPassword
        );

        return company;
    }
}
