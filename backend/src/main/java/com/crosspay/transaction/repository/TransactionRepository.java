package com.crosspay.transaction.repository;

import com.crosspay.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.initiatedByUserId = :userId
               OR t.senderUserId = :userId
               OR t.recipientUserId = :userId
            """)
    Page<Transaction> findTransactionHistoryByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    Optional<Transaction> findBySenderUserIdAndIdempotencyKey(
            UUID senderUserId,
            String idempotencyKey
    );

    Optional<Transaction> findByInitiatedByUserIdAndIdempotencyKey(
            UUID initiatedByUserId,
            String idempotencyKey
    );
}
