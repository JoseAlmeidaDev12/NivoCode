
package com.nivocode.auth;

import com.nivocode.auth.dto.RegistrationRequest;
import com.nivocode.company.Company;
import com.nivocode.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final CompanyRegistrationService registrationService;

    public RegistrationController(
            CompanyRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse register(
            @Valid @RequestBody RegistrationRequest request) {

        Company company = registrationService.register(
                request.companyName(),
                request.companySlug(),
                request.adminName(),
                request.adminEmail(),
                request.adminPassword()
        );

        return CompanyResponse.fromEntity(company);
    }
}