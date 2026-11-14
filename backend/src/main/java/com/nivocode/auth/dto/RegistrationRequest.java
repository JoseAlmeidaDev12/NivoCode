
package com.nivocode.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @NotBlank
        @Size(max = 160)
        String companyName,

        @NotBlank
        @Size(max = 80)
        String companySlug,

        @NotBlank
        @Size(max = 160)
        String adminName,

        @NotBlank
        @Email
        @Size(max = 254)
        String adminEmail,

        @NotBlank
        @Size(min = 12)
        String adminPassword

) {
}
