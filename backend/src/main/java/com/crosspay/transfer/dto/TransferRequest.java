package com.crosspay.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull(message = "Recipient user ID is required")
        UUID recipientUserId,

        @NotBlank(message = "Currency is required")
        @Size(
                min = 3,
                max = 3,
                message = "Currency must be a 3-letter ISO code"
        )
        String currency,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount
) {}