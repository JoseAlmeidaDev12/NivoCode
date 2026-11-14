
package com.nivocode.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 160)
        String name,

        @NotBlank(message = "Product SKU is required")
        @Size(max = 80)
        String sku,

        String description,

        @NotNull(message = "Product price is required")
        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(0)
        Integer stockQuantity

) {
}
