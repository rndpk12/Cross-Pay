package com.crosspay.withdrawal.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WithdrawalResponse(
        UUID transactionId,
        String currency,
        BigDecimal amount,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}
