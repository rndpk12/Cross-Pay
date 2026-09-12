package com.crosspay.withdrawal.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
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
public class WithdrawalService {

    private final WalletService walletService;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerEntryService ledgerEntryService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public WithdrawalService(
            WalletService walletService,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            LedgerEntryService ledgerEntryService,
            TransactionService transactionService,
            UserRepository userRepository
    ) {
        this.walletService = walletService;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerEntryService = ledgerEntryService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction withdraw(
            UUID userId,
            String currency,
            BigDecimal amount,
            String idempotencyKey
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        String normalizedIdempotencyKey = idempotencyKey.trim();
        if (normalizedIdempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 100 characters"
            );
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        String normalizedCurrency = currency.trim().toUpperCase();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount.scale() > 4) {
            throw new IllegalArgumentException(
                    "Amount must not have more than 4 decimal places"
            );
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }

        Optional<Transaction> existingTransaction =
                transactionService.findByInitiatorAndIdempotencyKey(
                        userId,
                        normalizedIdempotencyKey
                );
        if (existingTransaction.isPresent()) {
            Transaction existing = existingTransaction.get();
            if (!"WITHDRAWAL".equals(existing.getTransactionType())
                    || !normalizedCurrency.equals(existing.getCurrency())
                    || existing.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different transaction"
                );
            }
            return existing;
        }

        Wallet wallet = walletService.findByUserIdAndCurrency(userId, normalizedCurrency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wallet not found for this currency"
                ));
        LedgerAccount ledgerAccount = ledgerAccountRepository.findByWalletId(wallet.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ledger account not found for wallet"
                ));
        LedgerAccount lockedAccount = ledgerAccountRepository
                .findByIdForUpdate(ledgerAccount.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ledger account not found"));

        if (!normalizedCurrency.equals(lockedAccount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Ledger account currency does not match withdrawal currency"
            );
        }

        BigDecimal balance = ledgerEntryRepository.calculateBalance(lockedAccount.getId());
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        Transaction transaction = transactionService.createWithdrawalTransaction(
                userId,
                normalizedCurrency,
                amount,
                normalizedIdempotencyKey
        );
        ledgerEntryService.createEntry(
                lockedAccount.getId(),
                transaction.getId(),
                amount,
                "DEBIT",
                "WITHDRAWAL",
                transaction.getId()
        );
        return transactionService.completeTransaction(transaction.getId());
    }
}
