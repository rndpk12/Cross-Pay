package com.crosspay.withdrawal.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.ledger.service.LedgerEntryService;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.service.TransactionService;
import com.crosspay.user.entity.User;
import com.crosspay.user.repository.UserRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock private WalletService walletService;
    @Mock private LedgerAccountRepository ledgerAccountRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private LedgerEntryService ledgerEntryService;
    @Mock private TransactionService transactionService;
    @Mock private UserRepository userRepository;

    private WithdrawalService withdrawalService;

    @BeforeEach
    void setUp() {
        withdrawalService = new WithdrawalService(
                walletService, ledgerAccountRepository, ledgerEntryRepository,
                ledgerEntryService, transactionService, userRepository
        );
    }

    @Test
    void shouldSuccessfullyWithdrawMoney() {
        Fixture fixture = mockValidWithdrawal(new BigDecimal("50.00"));

        Transaction result = withdrawalService.withdraw(
                fixture.userId(), "USD", fixture.amount(), fixture.idempotencyKey()
        );

        assertEquals(fixture.transactionId(), result.getId());
        assertEquals("WITHDRAWAL", result.getTransactionType());
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(ledgerEntryRepository).calculateBalance(fixture.accountId());
        verify(ledgerEntryService).createEntry(
                fixture.accountId(), fixture.transactionId(), fixture.amount(),
                "DEBIT", "WITHDRAWAL", fixture.transactionId()
        );
        verify(transactionService).completeTransaction(fixture.transactionId());
    }

    @Test
    void shouldRejectNullAmount() {
        assertInvalidAmount(null, "Amount must be greater than zero");
    }

    @Test
    void shouldRejectZeroAmount() {
        assertInvalidAmount(BigDecimal.ZERO, "Amount must be greater than zero");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertInvalidAmount(new BigDecimal("-1.00"), "Amount must be greater than zero");
    }

    @Test
    void shouldRejectAmountWithMoreThanFourDecimalPlaces() {
        assertInvalidAmount(new BigDecimal("1.00001"),
                "Amount must not have more than 4 decimal places");
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        UUID.randomUUID(), "USD", BigDecimal.ONE, " "
                )
        );
        assertEquals("Idempotency key is required", exception.getMessage());
        verifyNoInteractions(userRepository, walletService, ledgerAccountRepository,
                ledgerEntryRepository, ledgerEntryService, transactionService);
    }

    @Test
    void shouldRejectIdempotencyKeyLongerThanOneHundredCharacters() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        UUID.randomUUID(), "USD", BigDecimal.ONE, "a".repeat(101)
                )
        );
        assertEquals("Idempotency key must not exceed 100 characters", exception.getMessage());
        verifyNoInteractions(userRepository, walletService, ledgerAccountRepository,
                ledgerEntryRepository, ledgerEntryService, transactionService);
    }

    @Test
    void shouldRejectWhenWalletDoesNotExist() {
        UUID userId = UUID.randomUUID();
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "missing-wallet"))
                .thenReturn(Optional.empty());
        when(walletService.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        userId, "USD", BigDecimal.ONE, "missing-wallet"
                )
        );

        assertEquals("Wallet not found for this currency", exception.getMessage());
        verify(transactionService, never()).createWithdrawalTransaction(
                any(UUID.class), anyString(), any(BigDecimal.class), anyString()
        );
        verifyNoLedgerMutation();
    }

    @Test
    void shouldRejectWhenLedgerAccountDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "USD");
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "missing-account"))
                .thenReturn(Optional.empty());
        when(walletService.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet));
        when(ledgerAccountRepository.findByWalletId(wallet.getId()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        userId, "USD", BigDecimal.ONE, "missing-account"
                )
        );

        assertEquals("Ledger account not found for wallet", exception.getMessage());
        verifyNoLedgerMutation();
    }

    @Test
    void shouldRejectWhenLedgerAccountCannotBeLocked() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "USD");
        LedgerAccount account = account(wallet, "USD");
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "lock-failure"))
                .thenReturn(Optional.empty());
        when(walletService.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet));
        when(ledgerAccountRepository.findByWalletId(wallet.getId()))
                .thenReturn(Optional.of(account));
        when(ledgerAccountRepository.findByIdForUpdate(account.getId()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        userId, "USD", BigDecimal.ONE, "lock-failure"
                )
        );

        assertEquals("Ledger account not found", exception.getMessage());
        verifyNoLedgerMutation();
    }

    @Test
    void shouldRejectWhenBalanceIsInsufficientWithoutCreatingFinancialRecords() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "USD");
        LedgerAccount account = account(wallet, "USD");
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "insufficient"))
                .thenReturn(Optional.empty());
        when(walletService.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet));
        when(ledgerAccountRepository.findByWalletId(wallet.getId()))
                .thenReturn(Optional.of(account));
        when(ledgerAccountRepository.findByIdForUpdate(account.getId()))
                .thenReturn(Optional.of(account));
        when(ledgerEntryRepository.calculateBalance(account.getId()))
                .thenReturn(new BigDecimal("49.99"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        userId, "USD", new BigDecimal("50.00"), "insufficient"
                )
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verifyNoLedgerMutation();
    }

    @Test
    void shouldReturnExistingWithdrawalForIdenticalIdempotencyKey() {
        UUID userId = UUID.randomUUID();
        Transaction existing = transaction(UUID.randomUUID(), userId, "USD",
                new BigDecimal("50.00"), "duplicate", "COMPLETED");
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "duplicate"))
                .thenReturn(Optional.of(existing));

        Transaction result = withdrawalService.withdraw(
                userId, "USD", new BigDecimal("50.00"), "duplicate"
        );

        assertEquals(existing.getId(), result.getId());
        verifyNoLedgerMutation();
    }

    @Test
    void shouldRejectDuplicateIdempotencyKeyWithDifferentAmount() {
        assertConflictingIdempotency("USD", new BigDecimal("51.00"));
    }

    @Test
    void shouldRejectDuplicateIdempotencyKeyWithDifferentCurrency() {
        assertConflictingIdempotency("EUR", new BigDecimal("50.00"));
    }

    private Fixture mockValidWithdrawal(BigDecimal amount) {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "USD");
        LedgerAccount account = account(wallet, "USD");
        UUID transactionId = UUID.randomUUID();
        String idempotencyKey = "withdrawal-success";
        Transaction pending = transaction(
                transactionId, userId, "USD", amount, idempotencyKey, "PENDING"
        );
        Transaction completed = transaction(
                transactionId, userId, "USD", amount, idempotencyKey, "COMPLETED"
        );
        completed.setCompletedAt(OffsetDateTime.now());

        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(walletService.findByUserIdAndCurrency(userId, "USD"))
                .thenReturn(Optional.of(wallet));
        when(ledgerAccountRepository.findByWalletId(wallet.getId()))
                .thenReturn(Optional.of(account));
        when(ledgerAccountRepository.findByIdForUpdate(account.getId()))
                .thenReturn(Optional.of(account));
        when(ledgerEntryRepository.calculateBalance(account.getId()))
                .thenReturn(new BigDecimal("100.00"));
        when(transactionService.createWithdrawalTransaction(
                userId, "USD", amount, idempotencyKey
        )).thenReturn(pending);
        when(transactionService.completeTransaction(transactionId)).thenReturn(completed);
        return new Fixture(userId, account.getId(), transactionId, amount, idempotencyKey);
    }

    private void assertInvalidAmount(BigDecimal amount, String message) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(
                        UUID.randomUUID(), "USD", amount, "invalid-amount"
                )
        );
        assertEquals(message, exception.getMessage());
        verifyNoInteractions(userRepository, walletService, ledgerAccountRepository,
                ledgerEntryRepository, ledgerEntryService, transactionService);
    }

    private void assertConflictingIdempotency(String currency, BigDecimal amount) {
        UUID userId = UUID.randomUUID();
        Transaction existing = transaction(UUID.randomUUID(), userId, "USD",
                new BigDecimal("50.00"), "duplicate", "COMPLETED");
        mockActiveUser(userId);
        when(transactionService.findByInitiatorAndIdempotencyKey(userId, "duplicate"))
                .thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.withdraw(userId, currency, amount, "duplicate")
        );

        assertEquals("Idempotency key was already used for a different transaction",
                exception.getMessage());
        verifyNoLedgerMutation();
    }

    private void mockActiveUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus("ACTIVE");
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
    }

    private Wallet wallet(UUID userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus("ACTIVE");
        return wallet;
    }

    private LedgerAccount account(Wallet wallet, String currency) {
        LedgerAccount account = new LedgerAccount();
        account.setId(UUID.randomUUID());
        account.setWalletId(wallet.getId());
        account.setCurrency(currency);
        account.setAccountType("USER_WALLET");
        return account;
    }

    private Transaction transaction(
            UUID transactionId, UUID userId, String currency,
            BigDecimal amount, String idempotencyKey, String status
    ) {
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setInitiatedByUserId(userId);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setStatus(status);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setCreatedAt(OffsetDateTime.now());
        return transaction;
    }

    private void verifyNoLedgerMutation() {
        verify(transactionService, never()).createWithdrawalTransaction(
                any(UUID.class), anyString(), any(BigDecimal.class), anyString()
        );
        verify(ledgerEntryService, never()).createEntry(
                any(UUID.class), any(UUID.class), any(BigDecimal.class),
                anyString(), anyString(), any(UUID.class)
        );
    }

    private record Fixture(
            UUID userId, UUID accountId, UUID transactionId,
            BigDecimal amount, String idempotencyKey
    ) {
    }
}
