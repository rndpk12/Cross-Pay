package com.crosspay.wallet.controller;

import com.crosspay.common.exception.ResourceNotFoundException;
import com.crosspay.wallet.dto.WalletResponse;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock private WalletService walletService;
    @Mock private Authentication authentication;

    private WalletController walletController;

    @BeforeEach
    void setUp() {
        walletController = new WalletController(walletService);
    }

    @Test
    void shouldListOnlyWalletsForAuthenticatedPrincipal() {
        UUID authenticatedUserId = UUID.randomUUID();
        Wallet wallet = wallet(authenticatedUserId, "USD");
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId);
        when(walletService.findByUserId(authenticatedUserId)).thenReturn(List.of(wallet));
        when(walletService.calculateBalance(wallet.getId())).thenReturn(new BigDecimal("375.00"));

        List<WalletResponse> response = walletController.getWallets(authentication).getBody();

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("375.00"), response.getFirst().balance());
        verify(walletService).findByUserId(authenticatedUserId);
    }

    @Test
    void shouldLookUpSingleWalletOnlyForAuthenticatedPrincipal() {
        UUID authenticatedUserId = UUID.randomUUID();
        Wallet wallet = wallet(authenticatedUserId, "USD");
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId);
        when(walletService.findByUserIdAndCurrency(authenticatedUserId, "usd"))
                .thenReturn(Optional.of(wallet));
        when(walletService.calculateBalance(wallet.getId())).thenReturn(BigDecimal.TEN);

        WalletResponse response = walletController.getWallet(authentication, "usd").getBody();

        assertEquals("USD", response.currency());
        verify(walletService).findByUserIdAndCurrency(authenticatedUserId, "usd");
    }

    @Test
    void shouldReturnNotFoundWhenAuthenticatedUserHasNoWalletForCurrency() {
        UUID authenticatedUserId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId);
        when(walletService.findByUserIdAndCurrency(authenticatedUserId, "USD"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> walletController.getWallet(authentication, "USD")
        );

        assertEquals("Wallet not found for this currency", exception.getMessage());
    }

    private Wallet wallet(UUID userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus("ACTIVE");
        return wallet;
    }
}
