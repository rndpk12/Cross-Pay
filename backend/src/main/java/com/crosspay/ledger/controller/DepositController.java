package com.crosspay.ledger.controller;

import com.crosspay.ledger.dto.DepositRequest;
import com.crosspay.ledger.dto.DepositResponse;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.service.LedgerEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deposits")
public class DepositController {

    private final LedgerEntryService ledgerEntryService;

    public DepositController(LedgerEntryService ledgerEntryService) {
        this.ledgerEntryService = ledgerEntryService;
    }

    @PostMapping
    public ResponseEntity<DepositResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody DepositRequest request
    ) {

        UUID userId = (UUID) authentication.getPrincipal();

        LedgerEntry entry = ledgerEntryService.deposit(
                userId,
                request.currency(),
                request.amount()
        );

        DepositResponse response = new DepositResponse(
                entry.getId(),
                entry.getCurrency(),
                entry.getAmount(),
                entry.getEntryType(),
                entry.getReferenceType(),
                entry.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}