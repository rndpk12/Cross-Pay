package com.crosspay.ledger.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class LedgerEntryService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public LedgerEntryService(
            LedgerEntryRepository ledgerEntryRepository,
            LedgerAccountRepository ledgerAccountRepository
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
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

        if (ledgerAccountId == null) {
            throw new IllegalArgumentException(
                    "Ledger account ID is required"
            );
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (entryType == null || entryType.isBlank()) {
            throw new IllegalArgumentException(
                    "Entry type is required"
            );
        }

        String normalizedEntryType =
                entryType.trim().toUpperCase();

        if (!normalizedEntryType.equals("CREDIT")
                && !normalizedEntryType.equals("DEBIT")) {
            throw new IllegalArgumentException(
                    "Entry type must be CREDIT or DEBIT"
            );
        }

        LedgerAccount account =
                ledgerAccountRepository
                        .findById(ledgerAccountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ledger account not found"
                                )
                        );

        LedgerEntry entry = new LedgerEntry();

        entry.setId(UUID.randomUUID());

        entry.setLedgerAccountId(
                ledgerAccountId
        );

        entry.setTransactionId(
                transactionId
        );

        entry.setAmount(amount);

        entry.setEntryType(
                normalizedEntryType
        );

        entry.setCurrency(
                account.getCurrency()
        );

        entry.setReferenceType(
                referenceType
        );

        entry.setReferenceId(
                referenceId
        );

        entry.setCreatedAt(
                OffsetDateTime.now()
        );

        return ledgerEntryRepository.save(entry);
    }
}