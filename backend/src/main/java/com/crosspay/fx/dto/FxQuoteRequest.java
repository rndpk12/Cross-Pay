package com.crosspay.fx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record FxQuoteRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01")
        @Digits(integer = 15, fraction = 4, message = "Amount must fit NUMERIC(19,4)")
        @Schema(description = "Positive source amount; up to 15 integer and 4 fractional digits", example = "100.0000")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        @Schema(description = "Exactly three alphabetic characters", example = "USD")
        String fromCurrency,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        @Schema(description = "Exactly three alphabetic characters and different from source currency", example = "EUR")
        String toCurrency
) {
}
