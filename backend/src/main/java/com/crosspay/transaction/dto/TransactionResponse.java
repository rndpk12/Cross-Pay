package com.crosspay.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        UUID senderUserId,
        UUID recipientUserId,
        String transactionType,
        String status,
        String currency,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}