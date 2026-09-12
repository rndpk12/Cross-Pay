package com.crosspay.fx.service;

import com.crosspay.fx.entity.FxQuote;
import com.crosspay.fx.repository.FxQuoteRepository;
import com.crosspay.common.observability.FinancialOperationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class FxQuoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FxQuoteService.class);

    private static final int QUOTE_VALIDITY_MINUTES = 10;

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(4);

    private final FxRateService fxRateService;
    private final FxQuoteRepository fxQuoteRepository;
    private final FinancialOperationMetrics metrics;

    public FxQuoteService(
            FxRateService fxRateService,
            FxQuoteRepository fxQuoteRepository,
            FinancialOperationMetrics metrics
    ) {
        this.fxRateService = fxRateService;
        this.fxQuoteRepository = fxQuoteRepository;
        this.metrics = metrics;
    }

    @Transactional
    public FxQuote createQuote(
            UUID userId,
            BigDecimal sourceAmount,
            String fromCurrency,
            String toCurrency
    ) {
        validateUserId(userId);

        if (sourceAmount == null
                || sourceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (sourceAmount.scale() > 4) {
            throw new IllegalArgumentException(
                    "Amount must not have more than 4 decimal places"
            );
        }

        String from =
                normalizeCurrency(fromCurrency);

        String to =
                normalizeCurrency(toCurrency);

        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Source and target currencies must be different"
            );
        }

        BigDecimal exchangeRate =
                fxRateService.getRate(
                        from,
                        to
                );

        BigDecimal convertedAmount =
                sourceAmount
                        .multiply(exchangeRate)
                        .setScale(
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal feeAmount =
                calculateFee(sourceAmount);

        OffsetDateTime now =
                OffsetDateTime.now();

        FxQuote quote =
                new FxQuote();

        quote.setId(UUID.randomUUID());
        quote.setUserId(userId);
        quote.setFromCurrency(from);
        quote.setToCurrency(to);
        quote.setSourceAmount(sourceAmount);
        quote.setExchangeRate(exchangeRate);
        quote.setConvertedAmount(convertedAmount);
        quote.setFeeAmount(feeAmount);
        quote.setStatus("ACTIVE");
        quote.setExpiresAt(
                now.plusMinutes(
                        QUOTE_VALIDITY_MINUTES
                )
        );
        quote.setCreatedAt(now);

        return fxQuoteRepository.save(quote);
    }

    @Transactional(readOnly = true)
    public FxQuote getQuote(
            UUID userId,
            UUID quoteId
    ) {
        validateUserId(userId);

        if (quoteId == null) {
            throw new IllegalArgumentException(
                    "Quote ID is required"
            );
        }

        FxQuote quote =
                fxQuoteRepository
                        .findByIdAndUserId(
                                quoteId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Quote not found"
                                )
                        );

        if ("ACTIVE".equals(quote.getStatus())
                && OffsetDateTime.now()
                .isAfter(quote.getExpiresAt())) {

            quote.setStatus("EXPIRED");

            fxQuoteRepository.save(quote);
            metrics.quoteExpired();
            LOGGER.info("event=fx_quote_expired quoteId={} userId={}", quote.getId(), userId);

            throw new IllegalArgumentException(
                    "Quote has expired"
            );
        }

        return quote;
    }

    @Transactional
    public FxQuote useQuote(
            UUID userId,
            UUID quoteId
    ) {
        validateUserId(userId);

        if (quoteId == null) {
            throw new IllegalArgumentException(
                    "Quote ID is required"
            );
        }

        /*
         * Quote consumption is a financial state transition. Locking the row
         * makes ACTIVE -> USED atomic across application instances; a second
         * transfer waits, then observes USED and rolls back its own work.
         */
        FxQuote quote =
                fxQuoteRepository
                        .findByIdAndUserIdForUpdate(
                                quoteId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Quote not found"
                                )
                        );

        if (OffsetDateTime.now().isAfter(quote.getExpiresAt())) {
            throw new IllegalArgumentException(
                    "Quote has expired"
            );
        }

        if (!"ACTIVE".equals(quote.getStatus())) {
            throw new IllegalArgumentException(
                    "Quote cannot be used because its status is "
                            + quote.getStatus()
            );
        }

        quote.setStatus("USED");
        FxQuote usedQuote = fxQuoteRepository.save(quote);
        metrics.quoteUsed();
        LOGGER.info("event=fx_quote_used quoteId={} userId={}", usedQuote.getId(), userId);
        return usedQuote;
    }

    private BigDecimal calculateFee(
            BigDecimal sourceAmount
    ) {
        /*
         * Initial learning implementation:
         * fixed 1% fee.
         *
         * Example:
         * EUR 100 -> EUR 1 fee
         */
        return sourceAmount
                .multiply(
                        new BigDecimal("0.01")
                )
                .setScale(
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private String normalizeCurrency(
            String currency
    ) {
        if (currency == null
                || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalized =
                currency
                        .trim()
                        .toUpperCase();

        if (normalized.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter ISO currency code"
            );
        }

        return normalized;
    }

    private void validateUserId(
            UUID userId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }
    }
}
