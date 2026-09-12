package com.crosspay.withdrawal.controller;

import com.crosspay.transaction.entity.Transaction;
import com.crosspay.common.exception.ApiErrorResponse;
import com.crosspay.withdrawal.dto.WithdrawalRequest;
import com.crosspay.withdrawal.dto.WithdrawalResponse;
import com.crosspay.withdrawal.service.WithdrawalService;
import com.crosspay.common.observability.FinancialOperationMetrics;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/withdrawals", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Withdrawals", description = "Authenticated wallet debits backed by ledger entries.")
public class WithdrawalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawalController.class);

    private final WithdrawalService withdrawalService;
    private final FinancialOperationMetrics metrics;

    public WithdrawalController(WithdrawalService withdrawalService, FinancialOperationMetrics metrics) {
        this.withdrawalService = withdrawalService;
        this.metrics = metrics;
    }

    @PostMapping
    @Operation(summary = "Create a withdrawal", description = "Debits the authenticated user's wallet when sufficient ledger-derived funds are available. The Idempotency-Key header is required.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request, insufficient funds, or idempotency conflict", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<WithdrawalResponse> withdraw(
            Authentication authentication,
            @Parameter(description = "Required key (maximum 100 characters) for safely retrying the same withdrawal. Reuse with different details is rejected.", required = true, example = "withdrawal-2026-001") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        LOGGER.info("event=withdrawal_started userId={} currency={}", userId, request.currency());
        Transaction transaction;
        try {
            transaction = withdrawalService.withdraw(userId, request.currency(), request.amount(), idempotencyKey);
            metrics.withdrawalSuccess();
        } catch (RuntimeException exception) {
            metrics.withdrawalFailure();
            LOGGER.warn("event=withdrawal_failed userId={} currency={} reason=operation_rejected", userId, request.currency());
            throw exception;
        }
        LOGGER.info("event=withdrawal_completed transactionId={} userId={} currency={}",
                transaction.getId(), userId, transaction.getCurrency());
        WithdrawalResponse response = new WithdrawalResponse(
                transaction.getId(),
                transaction.getCurrency(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
