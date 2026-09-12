package com.crosspay.fx.controller;

import com.crosspay.fx.dto.FxRateRequest;
import com.crosspay.fx.dto.FxRateResponse;
import com.crosspay.fx.entity.FxRate;
import com.crosspay.fx.service.FxRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(value = "/api/v1/fx-rates", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "FX Rates", description = "Authenticated FX rate management and lookup.")
public class FxRateController {

    private final FxRateService fxRateService;

    public FxRateController(
            FxRateService fxRateService
    ) {
        this.fxRateService = fxRateService;
    }

    @PostMapping
    @Operation(summary = "Create or update an FX rate", description = "Creates or updates the configured rate for a currency pair.")
    public ResponseEntity<FxRateResponse> createOrUpdateRate(
            @Valid @RequestBody FxRateRequest request
    ) {
        FxRate fxRate =
                fxRateService.createOrUpdateRate(
                        request.baseCurrency(),
                        request.quoteCurrency(),
                        request.rate()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(fxRate));
    }

    @GetMapping("/{baseCurrency}/{quoteCurrency}")
    @Operation(summary = "Get an FX rate", description = "Returns the configured rate details for a base and quote currency pair.")
    public ResponseEntity<FxRateResponse> getRate(
            @PathVariable String baseCurrency,
            @PathVariable String quoteCurrency
    ) {
        FxRate fxRate =
                fxRateService.getRateDetails(
                        baseCurrency,
                        quoteCurrency
                );

        return ResponseEntity.ok(
                toResponse(fxRate)
        );
    }

    private FxRateResponse toResponse(
            FxRate fxRate
    ) {
        return new FxRateResponse(
                fxRate.getId(),
                fxRate.getBaseCurrency(),
                fxRate.getQuoteCurrency(),
                fxRate.getRate(),
                fxRate.getSource(),
                fxRate.getFetchedAt(),
                fxRate.getCreatedAt()
        );
    }
}
