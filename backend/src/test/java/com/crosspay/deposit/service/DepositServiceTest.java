package com.crosspay.deposit.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerEntryService ledgerEntryService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserRepository userRepository;

    private DepositService depositService;

    @BeforeEach
    void setUp() {
        depositService = new DepositService(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService,
                userRepository
        );
    }

    private void mockActiveUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setStatus("ACTIVE");

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));
    }

    // =========================================================
    // TEST 1
    // Successful deposit
    // =========================================================

    @Test
    void shouldSuccessfullyDepositMoney() {

        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID ledgerAccountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String currency = "USD";
        String idempotencyKey = "deposit-success-001";

        BigDecimal amount =
                new BigDecimal("500.00");

        Wallet wallet = new Wallet();

        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus("ACTIVE");

        LedgerAccount account = new LedgerAccount();

        account.setId(ledgerAccountId);
        account.setWalletId(walletId);
        account.setCurrency(currency);
        account.setAccountType("USER_WALLET");

        Transaction pendingTransaction =
                new Transaction();

        pendingTransaction.setId(transactionId);
        pendingTransaction.setInitiatedByUserId(userId);
        pendingTransaction.setTransactionType("DEPOSIT");
        pendingTransaction.setStatus("PENDING");
        pendingTransaction.setCurrency(currency);
        pendingTransaction.setAmount(amount);
        pendingTransaction.setIdempotencyKey(idempotencyKey);

        Transaction completedTransaction =
                new Transaction();

        completedTransaction.setId(transactionId);
        completedTransaction.setInitiatedByUserId(userId);
        completedTransaction.setTransactionType("DEPOSIT");
        completedTransaction.setStatus("COMPLETED");
        completedTransaction.setCurrency(currency);
        completedTransaction.setAmount(amount);
        completedTransaction.setIdempotencyKey(idempotencyKey);

        completedTransaction.setCreatedAt(
                OffsetDateTime.now()
        );

        completedTransaction.setCompletedAt(
                OffsetDateTime.now()
        );

        LedgerEntry ledgerEntry =
                new LedgerEntry();

        ledgerEntry.setId(UUID.randomUUID());
        ledgerEntry.setLedgerAccountId(ledgerAccountId);
        ledgerEntry.setTransactionId(transactionId);
        ledgerEntry.setAmount(amount);
        ledgerEntry.setEntryType("CREDIT");
        ledgerEntry.setCurrency(currency);

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());

        when(
                walletService.findByUserIdAndCurrency(
                        userId,
                        currency
                )
        ).thenReturn(Optional.of(wallet));

        when(
                ledgerAccountRepository.findByWalletId(
                        walletId
                )
        ).thenReturn(Optional.of(account));

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        ledgerAccountId
                )
        ).thenReturn(Optional.of(account));

        when(
                transactionService.createDepositTransaction(
                        userId,
                        currency,
                        amount,
                        idempotencyKey
                )
        ).thenReturn(pendingTransaction);

        when(
                ledgerEntryService.createEntry(
                        ledgerAccountId,
                        transactionId,
                        amount,
                        "CREDIT",
                        "DEPOSIT",
                        transactionId
                )
        ).thenReturn(ledgerEntry);

        when(
                transactionService.completeTransaction(
                        transactionId
                )
        ).thenReturn(completedTransaction);

        Transaction result =
                depositService.deposit(
                        userId,
                        currency,
                        amount,
                        idempotencyKey
                );

        assertNotNull(result);

        assertEquals(
                transactionId,
                result.getId()
        );

        assertEquals(
                "DEPOSIT",
                result.getTransactionType()
        );

        assertEquals(
                "COMPLETED",
                result.getStatus()
        );

        assertEquals(
                currency,
                result.getCurrency()
        );

        assertEquals(
                amount,
                result.getAmount()
        );

        verify(
                walletService
        ).findByUserIdAndCurrency(
                userId,
                currency
        );

        verify(
                ledgerAccountRepository
        ).findByWalletId(walletId);

        verify(
                ledgerAccountRepository
        ).findByIdForUpdate(ledgerAccountId);

        verify(
                transactionService
        ).createDepositTransaction(
                userId,
                currency,
                amount,
                idempotencyKey
        );

        verify(
                ledgerEntryService
        ).createEntry(
                ledgerAccountId,
                transactionId,
                amount,
                "CREDIT",
                "DEPOSIT",
                transactionId
        );

        verify(
                transactionService
        ).completeTransaction(
                transactionId
        );
    }

    // =========================================================
    // TEST 2
    // Null amount
    // =========================================================

    @Test
    void shouldRejectNullAmount() {

        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                null,
                                "deposit-null-amount"
                        )
                );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 3
    // Zero amount
    // =========================================================

    @Test
    void shouldRejectZeroAmount() {

        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                BigDecimal.ZERO,
                                "deposit-zero-amount"
                        )
                );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 4
    // Negative amount
    // =========================================================

    @Test
    void shouldRejectNegativeAmount() {

        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                new BigDecimal("-10.00"),
                                "deposit-negative-amount"
                        )
                );

        assertEquals(
                "Amount must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 5
    // Too many decimal places
    // =========================================================

    @Test
    void shouldRejectAmountWithMoreThanFourDecimalPlaces() {

        UUID userId = UUID.randomUUID();

        BigDecimal amount =
                new BigDecimal("100.12345");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                amount,
                                "deposit-scale-test"
                        )
                );

        assertEquals(
                "Amount must not have more than 4 decimal places",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 6
    // Missing idempotency key
    // =========================================================

    @Test
    void shouldRejectMissingIdempotencyKey() {

        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                new BigDecimal("100.00"),
                                null
                        )
                );

        assertEquals(
                "Idempotency key is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 7
    // Idempotency key too long
    // =========================================================

    @Test
    void shouldRejectTooLongIdempotencyKey() {

        UUID userId = UUID.randomUUID();

        String idempotencyKey =
                "a".repeat(101);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                new BigDecimal("100.00"),
                                idempotencyKey
                        )
                );

        assertEquals(
                "Idempotency key must not exceed 100 characters",
                exception.getMessage()
        );

        verifyNoInteractions(
                walletService,
                ledgerAccountRepository,
                ledgerEntryService,
                transactionService
        );
    }

    // =========================================================
    // TEST 8
    // Wallet not found
    // =========================================================

    @Test
    void shouldRejectWhenWalletDoesNotExist() {

        UUID userId = UUID.randomUUID();

        String currency = "USD";

        String idempotencyKey =
                "deposit-wallet-missing";

        BigDecimal amount =
                new BigDecimal("100.00");

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());

        when(
                walletService.findByUserIdAndCurrency(
                        userId,
                        currency
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                currency,
                                amount,
                                idempotencyKey
                        )
                );

        assertEquals(
                "Wallet not found for this currency",
                exception.getMessage()
        );

        verify(
                walletService
        ).findByUserIdAndCurrency(
                userId,
                currency
        );

        verify(
                ledgerAccountRepository,
                never()
        ).findByWalletId(any(UUID.class));

        verify(
                transactionService,
                never()
        ).createDepositTransaction(
                any(UUID.class),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );
    }

    // =========================================================
    // TEST 9
    // Ledger account not found
    // =========================================================

    @Test
    void shouldRejectWhenLedgerAccountDoesNotExist() {

        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        String currency = "USD";

        String idempotencyKey =
                "deposit-ledger-missing";

        BigDecimal amount =
                new BigDecimal("100.00");

        Wallet wallet = new Wallet();

        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus("ACTIVE");

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());

        when(
                walletService.findByUserIdAndCurrency(
                        userId,
                        currency
                )
        ).thenReturn(Optional.of(wallet));

        when(
                ledgerAccountRepository.findByWalletId(
                        walletId
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                currency,
                                amount,
                                idempotencyKey
                        )
                );

        assertEquals(
                "Ledger account not found for wallet",
                exception.getMessage()
        );

        verify(
                ledgerAccountRepository
        ).findByWalletId(walletId);

        verify(
                ledgerAccountRepository,
                never()
        ).findByIdForUpdate(any(UUID.class));

        verify(
                transactionService,
                never()
        ).createDepositTransaction(
                any(UUID.class),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );
    }

    // =========================================================
    // TEST 10
    // Ledger account lock fails
    // =========================================================

    @Test
    void shouldRejectWhenLedgerAccountCannotBeLocked() {

        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID ledgerAccountId = UUID.randomUUID();

        String currency = "USD";

        String idempotencyKey =
                "deposit-lock-failure";

        BigDecimal amount =
                new BigDecimal("100.00");

        Wallet wallet = new Wallet();

        wallet.setId(walletId);
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setStatus("ACTIVE");

        LedgerAccount account =
                new LedgerAccount();

        account.setId(ledgerAccountId);
        account.setWalletId(walletId);
        account.setCurrency(currency);

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());

        when(
                walletService.findByUserIdAndCurrency(
                        userId,
                        currency
                )
        ).thenReturn(Optional.of(wallet));

        when(
                ledgerAccountRepository.findByWalletId(
                        walletId
                )
        ).thenReturn(Optional.of(account));

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        ledgerAccountId
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                currency,
                                amount,
                                idempotencyKey
                        )
                );

        assertEquals(
                "Ledger account not found",
                exception.getMessage()
        );

        verify(
                ledgerAccountRepository
        ).findByIdForUpdate(ledgerAccountId);

        verify(
                transactionService,
                never()
        ).createDepositTransaction(
                any(UUID.class),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );
    }

    // =========================================================
    // TEST 11
    // Duplicate idempotency key
    // =========================================================

    @Test
    void shouldReturnExistingDepositForDuplicateIdempotencyKey() {

        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String currency = "USD";

        String idempotencyKey =
                "duplicate-deposit";

        BigDecimal amount =
                new BigDecimal("250.00");

        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setId(transactionId);
        existingTransaction.setInitiatedByUserId(userId);
        existingTransaction.setTransactionType("DEPOSIT");
        existingTransaction.setStatus("COMPLETED");
        existingTransaction.setCurrency(currency);
        existingTransaction.setAmount(amount);
        existingTransaction.setIdempotencyKey(
                idempotencyKey
        );

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(
                Optional.of(existingTransaction)
        );

        Transaction result =
                depositService.deposit(
                        userId,
                        currency,
                        amount,
                        idempotencyKey
                );

        assertEquals(
                transactionId,
                result.getId()
        );

        assertEquals(
                "COMPLETED",
                result.getStatus()
        );

        assertEquals(
                amount,
                result.getAmount()
        );

        verify(
                walletService,
                never()
        ).findByUserIdAndCurrency(
                any(UUID.class),
                anyString()
        );

        verify(
                ledgerAccountRepository,
                never()
        ).findByWalletId(any(UUID.class));

        verify(
                ledgerEntryService,
                never()
        ).createEntry(
                any(UUID.class),
                any(UUID.class),
                any(BigDecimal.class),
                anyString(),
                anyString(),
                any(UUID.class)
        );

        verify(
                transactionService,
                never()
        ).createDepositTransaction(
                any(UUID.class),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );

        verify(
                transactionService,
                never()
        ).completeTransaction(any(UUID.class));
    }

    // =========================================================
    // TEST 12
    // Same idempotency key, different deposit
    // =========================================================

    @Test
    void shouldRejectDifferentDepositUsingSameIdempotencyKey() {

        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String idempotencyKey =
                "reused-deposit-key";

        BigDecimal originalAmount =
                new BigDecimal("100.00");

        BigDecimal differentAmount =
                new BigDecimal("200.00");

        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setId(transactionId);
        existingTransaction.setInitiatedByUserId(userId);
        existingTransaction.setTransactionType("DEPOSIT");
        existingTransaction.setStatus("COMPLETED");
        existingTransaction.setCurrency("USD");
        existingTransaction.setAmount(originalAmount);
        existingTransaction.setIdempotencyKey(
                idempotencyKey
        );

        mockActiveUser(userId);

        when(
                transactionService
                        .findByInitiatorAndIdempotencyKey(
                                userId,
                                idempotencyKey
                        )
        ).thenReturn(
                Optional.of(existingTransaction)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> depositService.deposit(
                                userId,
                                "USD",
                                differentAmount,
                                idempotencyKey
                        )
                );

        assertEquals(
                "Idempotency key was already used for a different transaction",
                exception.getMessage()
        );

        verify(
                walletService,
                never()
        ).findByUserIdAndCurrency(
                any(UUID.class),
                anyString()
        );

        verify(
                ledgerAccountRepository,
                never()
        ).findByWalletId(any(UUID.class));

        verify(
                ledgerEntryService,
                never()
        ).createEntry(
                any(UUID.class),
                any(UUID.class),
                any(BigDecimal.class),
                anyString(),
                anyString(),
                any(UUID.class)
        );

        verify(
                transactionService,
                never()
        ).createDepositTransaction(
                any(UUID.class),
                anyString(),
                any(BigDecimal.class),
                anyString()
        );
    }
}
