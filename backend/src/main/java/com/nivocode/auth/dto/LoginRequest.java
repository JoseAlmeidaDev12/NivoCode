
package com.nivocode.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        String companySlug,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        String password

) {
}
