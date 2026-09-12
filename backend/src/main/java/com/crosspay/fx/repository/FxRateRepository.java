package com.crosspay.fx.repository;

import com.crosspay.fx.entity.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FxRateRepository
        extends JpaRepository<FxRate, UUID> {

    Optional<FxRate> findByBaseCurrencyAndQuoteCurrency(
            String baseCurrency,
            String quoteCurrency
    );
}