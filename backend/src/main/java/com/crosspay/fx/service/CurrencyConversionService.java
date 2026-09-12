package com.crosspay.fx.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConversionService {

    private final FxRateService fxRateService;

    public CurrencyConversionService(
            FxRateService fxRateService
    ) {
        this.fxRateService = fxRateService;
    }

    public BigDecimal convert(
            BigDecimal amount,
            String fromCurrency,
            String toCurrency
    ) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (fromCurrency == null
                || fromCurrency.isBlank()) {
            throw new IllegalArgumentException(
                    "Source currency is required"
            );
        }

        if (toCurrency == null
                || toCurrency.isBlank()) {
            throw new IllegalArgumentException(
                    "Target currency is required"
            );
        }

        String from =
                fromCurrency.trim().toUpperCase();

        String to =
                toCurrency.trim().toUpperCase();

        if (from.equals(to)) {
            return amount;
        }

        BigDecimal rate =
                fxRateService.getRate(from, to);

        return amount
                .multiply(rate)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public BigDecimal getRate(
            String fromCurrency,
            String toCurrency
    ) {
        return fxRateService.getRate(
                fromCurrency,
                toCurrency
        );
    }
}