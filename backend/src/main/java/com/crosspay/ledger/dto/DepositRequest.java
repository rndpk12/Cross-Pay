package com.crosspay.ledger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DepositRequest(

        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(
                min = 3,
                max = 3,
                message = "Currency must be a 3-letter ISO code"
        )
        String currency
) {}