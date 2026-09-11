package com.crosspay.transaction.service;

import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction createTransferTransaction(
            UUID senderUserId,
            UUID recipientUserId,
            String currency
    ) {
        if (senderUserId == null) {
            throw new IllegalArgumentException(
                    "Sender user ID is required"
            );
        }

        if (recipientUserId == null) {
            throw new IllegalArgumentException(
                    "Recipient user ID is required"
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalizedCurrency =
                currency.trim().toUpperCase();

        OffsetDateTime now = OffsetDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setId(UUID.randomUUID());
        transaction.setSenderUserId(senderUserId);
        transaction.setRecipientUserId(recipientUserId);
        transaction.setTransactionType("TRANSFER");
        transaction.setStatus("PENDING");
        transaction.setCurrency(normalizedCurrency);
        transaction.setCreatedAt(now);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction completeTransaction(UUID transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found"
                                )
                        );

        if (!transaction.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException(
                    "Only pending transactions can be completed"
            );
        }

        transaction.setStatus("COMPLETED");
        transaction.setCompletedAt(
                OffsetDateTime.now()
        );

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction failTransaction(UUID transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found"
                                )
                        );

        if (!transaction.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException(
                    "Only pending transactions can be failed"
            );
        }

        transaction.setStatus("FAILED");
        transaction.setCompletedAt(
                OffsetDateTime.now()
        );

        return transactionRepository.save(transaction);
    }
}