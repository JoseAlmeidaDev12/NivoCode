
package com.nivocode.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank(message = "Customer name is required")
        @Size(max = 160, message = "Customer name is too long")
        String name,

        @Email(message = "Invalid email address")
        @Size(max = 255, message = "Email is too long")
        String email,

        @Size(max = 30, message = "Phone is too long")
        String phone,

        @Size(max = 18, message = "Document is too long")
        String document,

        String address

) {
}
