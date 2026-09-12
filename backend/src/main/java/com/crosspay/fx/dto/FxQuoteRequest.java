package com.crosspay.fx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record FxQuoteRequest(

        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String fromCurrency,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String toCurrency
) {
}