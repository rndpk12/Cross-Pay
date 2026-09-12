package com.crosspay.fx.repository;

import com.crosspay.fx.entity.FxQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FxQuoteRepository
        extends JpaRepository<FxQuote, UUID> {

    Optional<FxQuote> findByIdAndUserId(
            UUID id,
            UUID userId
    );
}