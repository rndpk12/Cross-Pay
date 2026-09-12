package com.crosspay.fx.service;

import com.crosspay.fx.entity.FxRate;
import com.crosspay.fx.repository.FxRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FxRateService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "USD",
            "EUR",
            "GBP",
            "INR",
            "CAD",
            "AUD",
            "SGD",
            "JPY"
    );

    private final FxRateRepository fxRateRepository;

    public FxRateService(
            FxRateRepository fxRateRepository
    ) {
        this.fxRateRepository = fxRateRepository;
    }

    @Transactional
    public FxRate createOrUpdateRate(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate
    ) {
        String base =
                normalizeAndValidateCurrency(baseCurrency);

        String quote =
                normalizeAndValidateCurrency(quoteCurrency);

        validateCurrencyPair(base, quote);

        if (rate == null
                || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Rate must be greater than zero"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        Optional<FxRate> existing =
                fxRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(
                                base,
                                quote
                        );

        FxRate fxRate;

        if (existing.isPresent()) {
            fxRate = existing.get();
            fxRate.setRate(rate);
            fxRate.setFetchedAt(now);
        } else {
            fxRate = new FxRate();

            fxRate.setId(UUID.randomUUID());
            fxRate.setBaseCurrency(base);
            fxRate.setQuoteCurrency(quote);
            fxRate.setRate(rate);
            fxRate.setSource("INTERNAL");
            fxRate.setFetchedAt(now);
            fxRate.setCreatedAt(now);
        }

        return fxRateRepository.save(fxRate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getRate(
            String baseCurrency,
            String quoteCurrency
    ) {
        String base =
                normalizeAndValidateCurrency(baseCurrency);

        String quote =
                normalizeAndValidateCurrency(quoteCurrency);

        validateCurrencyPair(base, quote);

        Optional<FxRate> directRate =
                fxRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(
                                base,
                                quote
                        );

        if (directRate.isPresent()) {
            return directRate.get().getRate();
        }

        Optional<FxRate> inverseRate =
                fxRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(
                                quote,
                                base
                        );

        if (inverseRate.isPresent()) {
            return BigDecimal.ONE
                    .divide(
                            inverseRate.get().getRate(),
                            8,
                            RoundingMode.HALF_UP
                    );
        }

        throw new IllegalArgumentException(
                "FX rate not found for "
                        + base
                        + "/"
                        + quote
        );
    }

    @Transactional(readOnly = true)
    public FxRate getRateDetails(
            String baseCurrency,
            String quoteCurrency
    ) {
        String base =
                normalizeAndValidateCurrency(baseCurrency);

        String quote =
                normalizeAndValidateCurrency(quoteCurrency);

        validateCurrencyPair(base, quote);

        Optional<FxRate> directRate =
                fxRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(
                                base,
                                quote
                        );

        if (directRate.isPresent()) {
            return directRate.get();
        }

        Optional<FxRate> inverseRate =
                fxRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(
                                quote,
                                base
                        );

        if (inverseRate.isPresent()) {
            FxRate original =
                    inverseRate.get();

            FxRate calculated =
                    new FxRate();

            calculated.setId(original.getId());
            calculated.setBaseCurrency(base);
            calculated.setQuoteCurrency(quote);

            calculated.setRate(
                    BigDecimal.ONE.divide(
                            original.getRate(),
                            8,
                            RoundingMode.HALF_UP
                    )
            );

            calculated.setSource(
                    original.getSource()
            );

            calculated.setFetchedAt(
                    original.getFetchedAt()
            );

            calculated.setCreatedAt(
                    original.getCreatedAt()
            );

            return calculated;
        }

        throw new IllegalArgumentException(
                "FX rate not found for "
                        + base
                        + "/"
                        + quote
        );
    }

    private String normalizeAndValidateCurrency(
            String currency
    ) {
        if (currency == null
                || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalized =
                currency.trim().toUpperCase();

        if (normalized.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter ISO currency code"
            );
        }

        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + normalized
            );
        }

        return normalized;
    }

    private void validateCurrencyPair(
            String baseCurrency,
            String quoteCurrency
    ) {
        if (baseCurrency.equals(quoteCurrency)) {
            throw new IllegalArgumentException(
                    "Base and quote currencies must be different"
            );
        }
    }
}