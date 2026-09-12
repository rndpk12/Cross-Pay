package com.crosspay.ledger.controller;

import com.crosspay.deposit.service.DepositService;
import com.crosspay.common.observability.FinancialOperationMetrics;
import com.crosspay.common.exception.ApiErrorResponse;
import com.crosspay.ledger.dto.DepositRequest;
import com.crosspay.ledger.dto.DepositResponse;
import com.crosspay.transaction.entity.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/deposits", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Deposits", description = "Authenticated wallet credits backed by ledger entries.")
public class DepositController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepositController.class);

    private final DepositService depositService;
    private final FinancialOperationMetrics metrics;

    public DepositController(DepositService depositService, FinancialOperationMetrics metrics) {
        this.depositService = depositService;
        this.metrics = metrics;
    }

    @PostMapping
    @Operation(summary = "Create a deposit", description = "Credits the authenticated user's wallet. The Idempotency-Key header is required; a retry with the same details returns the existing transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or idempotency conflict", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DepositResponse> deposit(
            Authentication authentication,
            @Parameter(description = "Required key (maximum 100 characters) for safely retrying the same deposit. Reuse with different details is rejected.", required = true, example = "deposit-2026-001") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request
    ) {

        UUID userId =
                (UUID) authentication.getPrincipal();

        LOGGER.info("event=deposit_started userId={} currency={}", userId, request.currency());
        Transaction transaction;
        try {
            transaction = depositService.deposit(userId, request.currency(), request.amount(), idempotencyKey);
            metrics.depositSuccess();
        } catch (RuntimeException exception) {
            metrics.depositFailure();
            LOGGER.warn("event=deposit_failed userId={} currency={} reason=operation_rejected", userId, request.currency());
            throw exception;
        }
        LOGGER.info("event=deposit_completed transactionId={} userId={} currency={}",
                transaction.getId(), userId, transaction.getCurrency());

        DepositResponse response =
                new DepositResponse(
                        transaction.getId(),
                        transaction.getCurrency(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreatedAt(),
                        transaction.getCompletedAt()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
