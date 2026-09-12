package com.crosspay.transaction.service;

import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction createTransferTransaction(
            UUID senderUserId,
            UUID recipientUserId,
            String sourceCurrency,
            BigDecimal sourceAmount,
            String destinationCurrency,
            BigDecimal destinationAmount,
            UUID fxQuoteId,
            String idempotencyKey
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

        if (sourceCurrency == null || sourceCurrency.isBlank()) {
            throw new IllegalArgumentException(
                    "Source currency is required"
            );
        }

        if (destinationCurrency == null
                || destinationCurrency.isBlank()) {
            throw new IllegalArgumentException(
                    "Destination currency is required"
            );
        }

        if (sourceAmount == null
                || sourceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Source amount must be greater than zero"
            );
        }

        if (destinationAmount == null
                || destinationAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Destination amount must be greater than zero"
            );
        }

        if (fxQuoteId == null) {
            throw new IllegalArgumentException(
                    "FX quote ID is required"
            );
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        String normalizedSourceCurrency =
                sourceCurrency.trim().toUpperCase();

        String normalizedDestinationCurrency =
                destinationCurrency.trim().toUpperCase();

        String normalizedIdempotencyKey =
                idempotencyKey.trim();

        /*
         * Return the existing transaction when the same
         * idempotency key has already been processed.
         */
        Optional<Transaction> existingTransaction =
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                normalizedIdempotencyKey
                        );

        if (existingTransaction.isPresent()) {
            return existingTransaction.get();
        }

        OffsetDateTime now = OffsetDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setId(UUID.randomUUID());

        transaction.setSenderUserId(senderUserId);

        transaction.setRecipientUserId(recipientUserId);

        transaction.setTransactionType("TRANSFER");

        transaction.setStatus("PENDING");

        /*
         * Legacy transaction fields.
         */
        transaction.setCurrency(
                normalizedSourceCurrency
        );

        transaction.setAmount(sourceAmount);

        /*
         * Cross-currency transaction fields.
         */
        transaction.setSourceCurrency(
                normalizedSourceCurrency
        );

        transaction.setDestinationCurrency(
                normalizedDestinationCurrency
        );

        transaction.setSourceAmount(sourceAmount);

        transaction.setDestinationAmount(destinationAmount);

        transaction.setFxQuoteId(fxQuoteId);

        transaction.setIdempotencyKey(
                normalizedIdempotencyKey
        );

        transaction.setCreatedAt(now);

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findBySenderAndIdempotencyKey(
            UUID senderUserId,
            String idempotencyKey
    ) {

        if (senderUserId == null) {
            throw new IllegalArgumentException(
                    "Sender user ID is required"
            );
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        return transactionRepository
                .findBySenderUserIdAndIdempotencyKey(
                        senderUserId,
                        idempotencyKey.trim()
                );
    }

    @Transactional
    public Transaction completeTransaction(
            UUID transactionId
    ) {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "Transaction ID is required"
            );
        }

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found"
                                )
                        );

        if (!"PENDING".equals(transaction.getStatus())) {
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
    public Transaction failTransaction(
            UUID transactionId
    ) {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "Transaction ID is required"
            );
        }

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found"
                                )
                        );

        if (!"PENDING".equals(transaction.getStatus())) {
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