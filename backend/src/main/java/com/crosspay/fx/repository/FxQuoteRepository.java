package com.crosspay.fx.repository;

import com.crosspay.fx.entity.FxQuote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FxQuoteRepository
        extends JpaRepository<FxQuote, UUID> {

    Optional<FxQuote> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT q
            FROM FxQuote q
            WHERE q.id = :id
              AND q.userId = :userId
            """)
    Optional<FxQuote> findByIdAndUserIdForUpdate(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );
}
