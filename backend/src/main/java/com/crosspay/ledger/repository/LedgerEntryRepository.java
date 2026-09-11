package com.crosspay.ledger.repository;

import com.crosspay.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByLedgerAccountIdOrderByCreatedAtDesc(
            UUID ledgerAccountId
    );

    @Query("""
            SELECT COALESCE(
                SUM(
                    CASE
                        WHEN e.entryType = 'CREDIT' THEN e.amount
                        WHEN e.entryType = 'DEBIT' THEN -e.amount
                        ELSE 0
                    END
                ),
                0
            )
            FROM LedgerEntry e
            WHERE e.ledgerAccountId = :ledgerAccountId
            """)
    BigDecimal calculateBalance(UUID ledgerAccountId);
}