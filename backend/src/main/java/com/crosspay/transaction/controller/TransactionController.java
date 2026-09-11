package com.crosspay.transaction.controller;

import com.crosspay.transaction.dto.TransactionResponse;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            Authentication authentication
    ) {

        UUID userId = (UUID) authentication.getPrincipal();

        List<Transaction> transactions =
                transactionRepository
                        .findBySenderUserIdOrRecipientUserIdOrderByCreatedAtDesc(
                                userId,
                                userId
                        );

        List<TransactionResponse> response =
                transactions.stream()
                        .map(transaction -> new TransactionResponse(
                                transaction.getId(),
                                transaction.getSenderUserId(),
                                transaction.getRecipientUserId(),
                                transaction.getTransactionType(),
                                transaction.getStatus(),
                                transaction.getCurrency(),
                                transaction.getCreatedAt(),
                                transaction.getCompletedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(response);
    }
}