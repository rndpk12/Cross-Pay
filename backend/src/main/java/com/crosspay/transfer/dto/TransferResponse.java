package com.crosspay.transfer.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferResponse(

        UUID transactionId,

        UUID recipientUserId,

        String sourceCurrency,

        String destinationCurrency,

        BigDecimal sourceAmount,

        BigDecimal destinationAmount,

        UUID fxQuoteId,

        String status,

        OffsetDateTime createdAt,

        OffsetDateTime completedAt

) {}