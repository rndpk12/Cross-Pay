package com.crosspay.transaction.repository;

import com.crosspay.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    List<Transaction> findBySenderUserIdOrRecipientUserIdOrderByCreatedAtDesc(
            UUID senderUserId,
            UUID recipientUserId
    );

    Optional<Transaction> findBySenderUserIdAndIdempotencyKey(
            UUID senderUserId,
            String idempotencyKey
    );
}