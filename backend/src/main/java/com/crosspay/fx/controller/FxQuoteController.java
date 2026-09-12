package com.crosspay.fx.controller;

import com.crosspay.fx.dto.FxQuoteRequest;
import com.crosspay.fx.dto.FxQuoteResponse;
import com.crosspay.fx.entity.FxQuote;
import com.crosspay.fx.service.FxQuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fx/quotes")
public class FxQuoteController {

    private final FxQuoteService fxQuoteService;

    public FxQuoteController(
            FxQuoteService fxQuoteService
    ) {
        this.fxQuoteService = fxQuoteService;
    }

    @PostMapping
    public ResponseEntity<FxQuoteResponse> createQuote(
            Authentication authentication,
            @Valid @RequestBody FxQuoteRequest request
    ) {
        UUID userId =
                (UUID) authentication.getPrincipal();

        FxQuote quote =
                fxQuoteService.createQuote(
                        userId,
                        request.amount(),
                        request.fromCurrency(),
                        request.toCurrency()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(quote));
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<FxQuoteResponse> getQuote(
            Authentication authentication,
            @PathVariable UUID quoteId
    ) {
        UUID userId =
                (UUID) authentication.getPrincipal();

        FxQuote quote =
                fxQuoteService.getQuote(
                        userId,
                        quoteId
                );

        return ResponseEntity.ok(
                toResponse(quote)
        );
    }

    private FxQuoteResponse toResponse(
            FxQuote quote
    ) {
        return new FxQuoteResponse(
                quote.getId(),
                quote.getFromCurrency(),
                quote.getToCurrency(),
                quote.getSourceAmount(),
                quote.getExchangeRate(),
                quote.getConvertedAmount(),
                quote.getFeeAmount(),
                quote.getStatus(),
                quote.getExpiresAt(),
                quote.getCreatedAt()
        );
    }
}