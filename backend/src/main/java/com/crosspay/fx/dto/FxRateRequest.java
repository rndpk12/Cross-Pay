package com.crosspay.fx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record FxRateRequest(

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String baseCurrency,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String quoteCurrency,

        @DecimalMin(value = "0.00000001")
        BigDecimal rate
) {
}