package com.crosspay.fx.controller;

import com.crosspay.fx.dto.FxQuoteRequest;
import com.crosspay.fx.dto.FxQuoteResponse;
import com.crosspay.fx.entity.FxQuote;
import com.crosspay.fx.service.FxQuoteService;
import com.crosspay.common.observability.FinancialOperationMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/fx/quotes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "FX Quotes", description = "Authenticated, short-lived FX quotes used to create cross-currency transfers.")
public class FxQuoteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FxQuoteController.class);

    private final FxQuoteService fxQuoteService;
    private final FinancialOperationMetrics metrics;

    public FxQuoteController(FxQuoteService fxQuoteService, FinancialOperationMetrics metrics) {
        this.fxQuoteService = fxQuoteService;
        this.metrics = metrics;
    }

    @PostMapping
    @Operation(summary = "Create an FX quote", description = "Issues an active quote for the authenticated user. Quotes expire after ten minutes and are consumed after a successful transfer.")
    public ResponseEntity<FxQuoteResponse> createQuote(
            Authentication authentication,
            @Valid @RequestBody FxQuoteRequest request
    ) {
        UUID userId =
                (UUID) authentication.getPrincipal();

        FxQuote quote;
        try {
            quote = fxQuoteService.createQuote(userId, request.amount(), request.fromCurrency(), request.toCurrency());
            metrics.quoteCreated();
        } catch (RuntimeException exception) {
            LOGGER.warn("event=fx_quote_failed userId={} reason=operation_rejected", userId);
            throw exception;
        }
        LOGGER.info("event=fx_quote_created quoteId={} userId={} fromCurrency={} toCurrency={}",
                quote.getId(), userId, quote.getFromCurrency(), quote.getToCurrency());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(quote));
    }

    @GetMapping("/{quoteId}")
    @Operation(summary = "Get an FX quote", description = "Returns a quote owned by the authenticated user, including its exchange rate, fee, expiration, and status.")
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
