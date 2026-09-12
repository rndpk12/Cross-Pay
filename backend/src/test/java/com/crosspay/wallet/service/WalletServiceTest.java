package com.crosspay.wallet.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private LedgerAccountRepository ledgerAccountRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
                walletRepository, ledgerAccountRepository, ledgerEntryRepository
        );
    }

    @Test
    void shouldReturnOnlyAuthenticatedUsersActiveWalletsInCurrencyOrder() {
        UUID userId = UUID.randomUUID();
        Wallet aud = wallet(userId, "AUD", "ACTIVE");
        Wallet usd = wallet(userId, "USD", "ACTIVE");
        when(walletRepository.findByUserIdAndStatusOrderByCurrencyAsc(userId, "ACTIVE"))
                .thenReturn(List.of(aud, usd));

        List<Wallet> wallets = walletService.findByUserId(userId);

        assertEquals(List.of("AUD", "USD"), wallets.stream().map(Wallet::getCurrency).toList());
        verify(walletRepository).findByUserIdAndStatusOrderByCurrencyAsc(userId, "ACTIVE");
    }

    @Test
    void shouldFindActiveWalletUsingNormalizedCurrency() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "USD", "ACTIVE");
        when(walletRepository.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet));

        Optional<Wallet> result = walletService.findByUserIdAndCurrency(userId, " usd ");

        assertTrue(result.isPresent());
        assertEquals("USD", result.get().getCurrency());
    }

    @Test
    void shouldNotReturnInactiveWalletFromSingleWalletRead() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet(userId, "USD", "INACTIVE")));

        assertFalse(walletService.findByUserIdAndCurrency(userId, "USD").isPresent());
    }

    @Test
    void shouldDeriveWalletBalanceFromLedgerEntries() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = wallet(UUID.randomUUID(), "USD", "ACTIVE");
        wallet.setId(walletId);
        LedgerAccount account = new LedgerAccount();
        account.setId(UUID.randomUUID());
        account.setWalletId(walletId);
        account.setCurrency("USD");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(ledgerAccountRepository.findByWalletId(walletId)).thenReturn(Optional.of(account));
        when(ledgerEntryRepository.calculateBalance(account.getId()))
                .thenReturn(new BigDecimal("375.00"));

        BigDecimal balance = walletService.calculateBalance(walletId);

        assertEquals(new BigDecimal("375.00"), balance);
        verify(ledgerEntryRepository).calculateBalance(account.getId());
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.findByUserIdAndCurrency(UUID.randomUUID(), "XYZ")
        );
        assertEquals("Unsupported currency: XYZ", exception.getMessage());
    }

    @Test
    void shouldCreateActiveWalletWithZeroStoredBalanceAndLedgerAccount() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(userId, "usd");

        assertEquals("USD", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("ACTIVE", result.getStatus());
        ArgumentCaptor<LedgerAccount> accountCaptor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(accountCaptor.capture());
        assertEquals(result.getId(), accountCaptor.getValue().getWalletId());
        assertEquals("USD", accountCaptor.getValue().getCurrency());
    }

    @Test
    void shouldRejectDuplicateWalletCurrency() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet(userId, "USD", "ACTIVE")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.createWallet(userId, "USD")
        );

        assertEquals("Wallet already exists for this currency", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(ledgerAccountRepository, never()).save(any(LedgerAccount.class));
    }

    private Wallet wallet(UUID userId, String currency, String status) {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus(status);
        wallet.setBalance(BigDecimal.ZERO);
        return wallet;
    }
}
