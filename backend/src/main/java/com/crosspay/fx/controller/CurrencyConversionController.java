package com.crosspay.fx.controller;

import com.crosspay.fx.dto.CurrencyConversionRequest;
import com.crosspay.fx.dto.CurrencyConversionResponse;
import com.crosspay.fx.service.CurrencyConversionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/fx")
public class CurrencyConversionController {

    private final CurrencyConversionService conversionService;

    public CurrencyConversionController(
            CurrencyConversionService conversionService
    ) {
        this.conversionService = conversionService;
    }

    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convert(
            @Valid @RequestBody CurrencyConversionRequest request
    ) {
        BigDecimal rate =
                conversionService.getRate(
                        request.fromCurrency(),
                        request.toCurrency()
                );

        BigDecimal convertedAmount =
                conversionService.convert(
                        request.amount(),
                        request.fromCurrency(),
                        request.toCurrency()
                );

        CurrencyConversionResponse response =
                new CurrencyConversionResponse(
                        request.amount(),
                        request.fromCurrency().trim().toUpperCase(),
                        request.toCurrency().trim().toUpperCase(),
                        rate,
                        convertedAmount
                );

        return ResponseEntity.ok(response);
    }
}