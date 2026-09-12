package com.crosspay.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull(message = "Recipient user ID is required")
        @Schema(description = "Recipient user identifier", example = "11111111-1111-1111-1111-111111111111")
        UUID recipientUserId,

        @NotNull(message = "Quote ID is required")
        @Schema(description = "Active FX quote identifier owned by the authenticated sender", example = "22222222-2222-2222-2222-222222222222")
        UUID quoteId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        @Digits(integer = 15, fraction = 4, message = "Amount must fit NUMERIC(19,4)")
        @Schema(description = "Must exactly match the quote source amount; up to 15 integer and 4 fractional digits", example = "100.0000")
        BigDecimal amount

) {}
