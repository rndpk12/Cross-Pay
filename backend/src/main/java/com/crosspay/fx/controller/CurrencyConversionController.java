package com.crosspay.fx.controller;

import com.crosspay.fx.dto.CurrencyConversionRequest;
import com.crosspay.fx.dto.CurrencyConversionResponse;
import com.crosspay.fx.service.CurrencyConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(value = "/api/v1/fx", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "FX", description = "Authenticated FX rate lookup and conversion utilities.")
public class CurrencyConversionController {

    private final CurrencyConversionService conversionService;

    public CurrencyConversionController(
            CurrencyConversionService conversionService
    ) {
        this.conversionService = conversionService;
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert currency", description = "Calculates a conversion using the current configured FX rate. This does not create a quote or transfer.")
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
