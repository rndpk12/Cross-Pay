package com.crosspay.ledger.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DepositResponse(
        UUID ledgerEntryId,
        String currency,
        BigDecimal amount,
        String entryType,
        String referenceType,
        OffsetDateTime createdAt
) {}