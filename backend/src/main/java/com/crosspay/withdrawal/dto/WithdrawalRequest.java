package com.crosspay.withdrawal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record WithdrawalRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 15, fraction = 4, message = "Amount must fit NUMERIC(19,4)")
        @Schema(description = "Positive monetary amount; up to 15 integer and 4 fractional digits", example = "75.0000")
        BigDecimal amount,
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must contain only letters")
        @Schema(description = "Exactly three alphabetic characters. Supported currencies are enforced by business validation.", example = "USD")
        String currency
) {
}
