package com.crosspay.ledger.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final WalletRepository walletRepository;

    public LedgerEntryService(
            LedgerEntryRepository ledgerEntryRepository,
            LedgerAccountRepository ledgerAccountRepository,
            WalletRepository walletRepository
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public LedgerEntry createEntry(
            UUID ledgerAccountId,
            UUID transactionId,
            BigDecimal amount,
            String entryType,
            String referenceType,
            UUID referenceId
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (entryType == null || entryType.isBlank()) {
            throw new IllegalArgumentException(
                    "Entry type is required"
            );
        }

        String normalizedEntryType = entryType.trim().toUpperCase();

        if (!normalizedEntryType.equals("CREDIT")
                && !normalizedEntryType.equals("DEBIT")) {
            throw new IllegalArgumentException(
                    "Entry type must be CREDIT or DEBIT"
            );
        }

        LedgerAccount account = ledgerAccountRepository
                .findById(ledgerAccountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ledger account not found"
                        )
                );

        LedgerEntry entry = new LedgerEntry();

        entry.setId(UUID.randomUUID());
        entry.setLedgerAccountId(ledgerAccountId);
        entry.setTransactionId(transactionId);
        entry.setAmount(amount);
        entry.setEntryType(normalizedEntryType);
        entry.setCurrency(account.getCurrency());
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);
        entry.setCreatedAt(OffsetDateTime.now());

        return ledgerEntryRepository.save(entry);
    }

    @Transactional
    public LedgerEntry deposit(
            UUID userId,
            String currency,
            BigDecimal amount
    ) {

        String normalizedCurrency = currency.trim().toUpperCase();

        Wallet wallet = walletRepository
                .findByUserIdAndCurrency(
                        userId,
                        normalizedCurrency
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wallet not found for this currency"
                        )
                );

        LedgerAccount account = ledgerAccountRepository
                .findByWalletId(wallet.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ledger account not found for wallet"
                        )
                );

        return createEntry(
                account.getId(),
                null,
                amount,
                "CREDIT",
                "DEPOSIT",
                null
        );
    }
}