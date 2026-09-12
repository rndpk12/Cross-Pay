package com.crosspay.fx.controller;

import com.crosspay.fx.dto.FxRateRequest;
import com.crosspay.fx.dto.FxRateResponse;
import com.crosspay.fx.entity.FxRate;
import com.crosspay.fx.service.FxRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/fx-rates")
public class FxRateController {

    private final FxRateService fxRateService;

    public FxRateController(
            FxRateService fxRateService
    ) {
        this.fxRateService = fxRateService;
    }

    @PostMapping
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