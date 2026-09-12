package com.crosspay.transfer.controller;

import com.crosspay.transaction.entity.Transaction;
import com.crosspay.common.exception.ApiErrorResponse;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.transfer.dto.TransferResponse;
import com.crosspay.transfer.service.TransferService;
import com.crosspay.common.observability.FinancialOperationMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/transfers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Transfers", description = "Authenticated cross-currency transfers using a previously issued FX quote.")
public class TransferController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;
    private final FinancialOperationMetrics metrics;

    public TransferController(TransferService transferService, FinancialOperationMetrics metrics) {
        this.transferService = transferService;
        this.metrics = metrics;
    }

    @PostMapping
    @Operation(summary = "Create a transfer", description = "The authenticated user is the initiator. The quote amount must match the request; insufficient funds, expired/used quotes, self-transfer, and conflicting idempotency keys are rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed or existing idempotent transaction returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request, quote, funds, or idempotency conflict", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TransferResponse> transfer(
            Authentication authentication,
            @Parameter(description = "Required key (maximum 100 characters) for safely retrying the same financial operation. Reusing it with different transfer details is rejected.", required = true, example = "transfer-2026-001") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {

        UUID senderUserId =
                (UUID) authentication.getPrincipal();

        LOGGER.info("event=transfer_started senderUserId={} recipientUserId={}",
                senderUserId, request.recipientUserId());
        Transaction transaction;
        try {
            transaction = transferService.transfer(senderUserId, request, idempotencyKey);
            metrics.transferSuccess();
        } catch (RuntimeException exception) {
            metrics.transferFailure();
            LOGGER.warn("event=transfer_failed senderUserId={} recipientUserId={} reason=operation_rejected",
                    senderUserId, request.recipientUserId());
            throw exception;
        }
        LOGGER.info("event=transfer_completed transactionId={} senderUserId={} recipientUserId={} sourceCurrency={} destinationCurrency={}",
                transaction.getId(), senderUserId, transaction.getRecipientUserId(),
                transaction.getSourceCurrency(), transaction.getDestinationCurrency());

        TransferResponse response =
                new TransferResponse(
                        transaction.getId(),
                        transaction.getRecipientUserId(),
                        transaction.getSourceCurrency(),
                        transaction.getDestinationCurrency(),
                        transaction.getSourceAmount(),
                        transaction.getDestinationAmount(),
                        transaction.getFxQuoteId(),
                        transaction.getStatus(),
                        transaction.getCreatedAt(),
                        transaction.getCompletedAt()
                );

        return ResponseEntity.ok(response);
    }
}
