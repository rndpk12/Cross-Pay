package com.crosspay.transaction.controller;

import com.crosspay.transaction.dto.TransactionHistoryResponse;
import com.crosspay.transaction.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Transactions", description = "Authenticated transaction history for operations a user initiated, sent, or received.")
public class TransactionController {

    private final TransactionHistoryService transactionHistoryService;

    public TransactionController(
            TransactionHistoryService transactionHistoryService
    ) {
        this.transactionHistoryService = transactionHistoryService;
    }

    @GetMapping
    @Operation(summary = "Get transaction history", description = "Returns newest transactions first. Page is zero-based; size defaults to 20 and is limited to 100.")
    public ResponseEntity<TransactionHistoryResponse> getTransactions(
            Authentication authentication,
            @Parameter(description = "Zero-based page number; must be at least 0", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100; default 20", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return ResponseEntity.ok(
                transactionHistoryService.getTransactionHistory(userId, page, size)
        );
    }
}
