package com.crosspay.transfer.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID transactionId,
        UUID recipientUserId,
        String currency,
        BigDecimal amount,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}