package com.crosspay.fx.dto;

import java.math.BigDecimal;

public record CurrencyConversionResponse(
        BigDecimal amount,
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        BigDecimal convertedAmount
) {
}