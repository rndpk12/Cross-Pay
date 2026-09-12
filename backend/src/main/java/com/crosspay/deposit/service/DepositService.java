package com.crosspay.deposit.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.service.LedgerEntryService;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.service.TransactionService;
import com.crosspay.user.entity.User;
import com.crosspay.user.repository.UserRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepositService {

    private final WalletService walletService;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryService ledgerEntryService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public DepositService(
            WalletService walletService,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryService ledgerEntryService,
            TransactionService transactionService,
            UserRepository userRepository
    ) {
        this.walletService = walletService;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryService = ledgerEntryService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction deposit(
            UUID userId,
            String currency,
            BigDecimal amount,
            String idempotencyKey
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        /*
         * ---------------------------------------------------------
         * Validate idempotency key
         * ---------------------------------------------------------
         */

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        String normalizedIdempotencyKey =
                idempotencyKey.trim();

        if (normalizedIdempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 100 characters"
            );
        }

        /*
         * ---------------------------------------------------------
         * Validate currency
         * ---------------------------------------------------------
         */

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalizedCurrency =
                currency.trim().toUpperCase();

        /*
         * ---------------------------------------------------------
         * Validate amount
         * ---------------------------------------------------------
         */

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (amount.scale() > 4) {
            throw new IllegalArgumentException(
                    "Amount must not have more than 4 decimal places"
            );
        }

        /*
         * Lock and validate the initiating user only after all input
         * validation has passed. This serializes idempotent financial
         * requests for a user, including requests for different wallets.
         */
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "User account is not active"
            );
        }

        /*
         * ---------------------------------------------------------
         * Idempotency
         * ---------------------------------------------------------
         *
         * Check before touching the wallet or ledger.
         *
         * Same key + same deposit:
         * return the existing transaction.
         *
         * Same key + different deposit:
         * reject the request.
         * ---------------------------------------------------------
         */

        Optional<Transaction> existingTransaction =
                transactionService.findByInitiatorAndIdempotencyKey(
                        userId,
                        normalizedIdempotencyKey
                );

        if (existingTransaction.isPresent()) {

            Transaction existing =
                    existingTransaction.get();

            if (!"DEPOSIT".equals(existing.getTransactionType())
                    || !normalizedCurrency.equals(
                            existing.getCurrency())
                    || existing.getAmount().compareTo(amount) != 0) {

                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different transaction"
                );
            }

            return existing;
        }

        /*
         * ---------------------------------------------------------
         * Find active wallet
         * ---------------------------------------------------------
         */

        Wallet wallet =
                walletService.findByUserIdAndCurrency(
                        userId,
                        normalizedCurrency
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wallet not found for this currency"
                        )
                );

        /*
         * ---------------------------------------------------------
         * Find ledger account
         * ---------------------------------------------------------
         */

        LedgerAccount ledgerAccount =
                ledgerAccountRepository
                        .findByWalletId(wallet.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ledger account not found for wallet"
                                )
                        );

        /*
         * ---------------------------------------------------------
         * Lock ledger account
         * ---------------------------------------------------------
         *
         * The row remains locked until this transaction commits.
         * This prevents concurrent operations from modifying the
         * same ledger account simultaneously.
         * ---------------------------------------------------------
         */

        LedgerAccount lockedAccount =
                ledgerAccountRepository
                        .findByIdForUpdate(ledgerAccount.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ledger account not found"
                                )
                        );

        /*
         * ---------------------------------------------------------
         * Defensive currency check
         * ---------------------------------------------------------
         */

        if (!normalizedCurrency.equals(
                lockedAccount.getCurrency())) {

            throw new IllegalArgumentException(
                    "Ledger account currency does not match deposit currency"
            );
        }

        /*
         * ---------------------------------------------------------
         * Create PENDING deposit transaction
         * ---------------------------------------------------------
         */

        Transaction transaction =
                transactionService.createDepositTransaction(
                        userId,
                        normalizedCurrency,
                        amount,
                        normalizedIdempotencyKey
                );

        /*
         * ---------------------------------------------------------
         * Create CREDIT ledger entry
         * ---------------------------------------------------------
         *
         * The transaction ID links the accounting entry to the
         * business transaction.
         * ---------------------------------------------------------
         */

        LedgerEntry entry =
                ledgerEntryService.createEntry(
                        lockedAccount.getId(),
                        transaction.getId(),
                        amount,
                        "CREDIT",
                        "DEPOSIT",
                        transaction.getId()
                );

        /*
         * ---------------------------------------------------------
         * Complete transaction
         * ---------------------------------------------------------
         *
         * Transaction + ledger entry are part of the same
         * database transaction because this service is @Transactional.
         * ---------------------------------------------------------
         */

        return transactionService.completeTransaction(
                transaction.getId()
        );
    }
}
