
package com.nivocode.company;

import com.nivocode.company.dto.CompanyRequest;
import com.nivocode.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest request) {
        Company company = companyService.create(
                request.name(),
                request.slug()
        );

        return CompanyResponse.fromEntity(company);
    }
}
