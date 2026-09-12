package com.crosspay.wallet.controller;

import com.crosspay.wallet.dto.CreateWalletRequest;
import com.crosspay.wallet.dto.WalletResponse;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import com.crosspay.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Wallets", description = "Authenticated wallet management. Displayed balances are ledger-derived.")
public class WalletController {

    private final WalletService walletService;

    public WalletController(
            WalletService walletService
    ) {
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(summary = "Create a wallet", description = "Creates an active wallet for the authenticated user and a supported three-letter currency.")
    public ResponseEntity<WalletResponse> createWallet(
            Authentication authentication,
            @Valid @RequestBody CreateWalletRequest request
    ) {

        UUID userId =
                (UUID) authentication.getPrincipal();

        Wallet wallet =
                walletService.createWallet(
                        userId,
                        request.currency()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toWalletResponse(wallet));
    }

    @GetMapping
    @Operation(summary = "List wallets", description = "Returns wallets owned by the authenticated user with ledger-derived balances.")
    public ResponseEntity<List<WalletResponse>> getWallets(
            Authentication authentication
    ) {

        UUID userId =
                (UUID) authentication.getPrincipal();

        List<WalletResponse> responses =
                walletService
                        .findByUserId(userId)
                        .stream()
                        .map(this::toWalletResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{currency}")
    @Operation(summary = "Get a wallet by currency", description = "Returns the authenticated user's wallet for a supported currency, or 404 when it does not exist.")
    public ResponseEntity<WalletResponse> getWallet(
            Authentication authentication,
            @PathVariable String currency
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        Wallet wallet = walletService.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for this currency"
                ));

        return ResponseEntity.ok(toWalletResponse(wallet));
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        BigDecimal balance = walletService.calculateBalance(wallet.getId());

        return new WalletResponse(
                wallet.getId(),
                wallet.getCurrency(),
                balance,
                wallet.getStatus(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
