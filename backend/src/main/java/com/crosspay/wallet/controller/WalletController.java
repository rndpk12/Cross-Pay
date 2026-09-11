package com.crosspay.wallet.controller;

import com.crosspay.wallet.dto.CreateWalletRequest;
import com.crosspay.wallet.dto.WalletResponse;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            Authentication authentication,
            @Valid @RequestBody CreateWalletRequest request
    ) {

        UUID userId = (UUID) authentication.getPrincipal();

        Wallet wallet = walletService.createWallet(
                userId,
                request.currency()
        );

        WalletResponse response = new WalletResponse(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getStatus(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets(
            Authentication authentication
    ) {

        UUID userId = (UUID) authentication.getPrincipal();

        List<WalletResponse> responses = walletService
                .findByUserId(userId)
                .stream()
                .map(wallet -> new WalletResponse(
                        wallet.getId(),
                        wallet.getCurrency(),
                        walletService.calculateBalance(wallet.getId()),
                        wallet.getStatus(),
                        wallet.getCreatedAt(),
                        wallet.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }
}