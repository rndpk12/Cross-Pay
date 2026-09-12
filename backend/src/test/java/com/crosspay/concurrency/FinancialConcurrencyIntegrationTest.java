package com.crosspay.concurrency;

import com.crosspay.deposit.service.DepositService;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.transfer.service.TransferService;
import com.crosspay.withdrawal.service.WithdrawalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL-only tests for financial critical sections.  Each service call is
 * made from a separate thread, so Spring opens a separate database transaction
 * and PostgreSQL, rather than an in-memory lock, decides the outcome.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FinancialConcurrencyIntegrationTest {

    private static final BigDecimal ZERO = new BigDecimal("0.0000");
    private static final BigDecimal FIFTY = new BigDecimal("50.0000");
    private static final BigDecimal SEVENTY_FIVE = new BigDecimal("75.0000");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0000");

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> "test-only-secret-must-be-at-least-32-bytes");
        registry.add("jwt.expiration", () -> "900000");
    }

    @Autowired
    private DepositService depositService;

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private JdbcTemplate jdbc;

    private final List<UUID> users = new ArrayList<>();

    @AfterEach
    void cleanDatabase() {
        jdbc.execute("DROP TRIGGER IF EXISTS crosspay_test_fail_completion ON transactions");
        jdbc.execute("DROP FUNCTION IF EXISTS crosspay_test_fail_completion()");
        for (UUID userId : users) {
            jdbc.update("DELETE FROM ledger_entries WHERE ledger_account_id IN "
                    + "(SELECT la.id FROM ledger_accounts la JOIN wallets w ON w.id = la.wallet_id "
                    + "WHERE w.user_id = ?)", userId);
        }
        for (UUID userId : users) {
            jdbc.update("DELETE FROM transactions WHERE initiated_by_user_id = ? "
                    + "OR sender_user_id = ? OR recipient_user_id = ?", userId, userId, userId);
        }
        for (UUID userId : users) {
            jdbc.update("DELETE FROM fx_quotes WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM ledger_accounts WHERE wallet_id IN "
                    + "(SELECT id FROM wallets WHERE user_id = ?)", userId);
            jdbc.update("DELETE FROM wallets WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM users WHERE id = ?", userId);
        }
        users.clear();
    }

    @Test
    @Timeout(20)
    void concurrentDepositsAreBothAppliedWithoutLostUpdates() throws Exception {
        WalletAccount wallet = wallet("USD");

        List<Outcome> outcomes = concurrently(
                () -> depositService.deposit(wallet.userId(), "USD", ONE_HUNDRED, "deposit-a"),
                () -> depositService.deposit(wallet.userId(), "USD", ONE_HUNDRED, "deposit-b")
        );

        assertSuccessful(outcomes, 2);
        assertBalance(wallet.accountId(), new BigDecimal("200.0000"));
        assertEquals(2, transactionCount(wallet.userId(), "DEPOSIT"));
        assertEquals(2, ledgerCount(wallet.accountId(), "CREDIT", "DEPOSIT"));
    }

    @Test
    @Timeout(20)
    void concurrentWithdrawalsCannotOverspend() throws Exception {
        WalletAccount wallet = fundedWallet(ONE_HUNDRED);

        List<Outcome> outcomes = concurrently(
                () -> withdrawalService.withdraw(wallet.userId(), "USD", SEVENTY_FIVE, "withdraw-a"),
                () -> withdrawalService.withdraw(wallet.userId(), "USD", SEVENTY_FIVE, "withdraw-b")
        );

        assertSuccessful(outcomes, 1);
        assertBalance(wallet.accountId(), new BigDecimal("25.0000"));
        assertEquals(1, transactionCount(wallet.userId(), "WITHDRAWAL"));
        assertEquals(1, ledgerCount(wallet.accountId(), "DEBIT", "WITHDRAWAL"));
    }

    @Test
    @Timeout(20)
    void concurrentExactWithdrawalsCanBothComplete() throws Exception {
        WalletAccount wallet = fundedWallet(ONE_HUNDRED);

        List<Outcome> outcomes = concurrently(
                () -> withdrawalService.withdraw(wallet.userId(), "USD", FIFTY, "withdraw-a"),
                () -> withdrawalService.withdraw(wallet.userId(), "USD", FIFTY, "withdraw-b")
        );

        assertSuccessful(outcomes, 2);
        assertBalance(wallet.accountId(), ZERO);
        assertEquals(2, transactionCount(wallet.userId(), "WITHDRAWAL"));
    }

    @Test
    @Timeout(20)
    void concurrentTransfersCanSpendExactlyTheAvailableBalance() throws Exception {
        TransferFixture fixture = transferFixture(ONE_HUNDRED);
        UUID quoteA = quote(fixture.sender().userId(), FIFTY);
        UUID quoteB = quote(fixture.sender().userId(), FIFTY);

        List<Outcome> outcomes = concurrently(
                () -> transfer(fixture, fixture.recipient().userId(), quoteA, FIFTY, "transfer-a"),
                () -> transfer(fixture, fixture.recipient().userId(), quoteB, FIFTY, "transfer-b")
        );

        assertSuccessful(outcomes, 2);
        assertBalance(fixture.sender().accountId(), ZERO);
        assertBalance(fixture.recipient().accountId(), ONE_HUNDRED);
        assertEquals(2, transactionCount(fixture.sender().userId(), "TRANSFER"));
        assertEquals(2, ledgerCount(fixture.sender().accountId(), "DEBIT", "TRANSFER"));
        assertEquals(2, ledgerCount(fixture.recipient().accountId(), "CREDIT", "TRANSFER"));
    }

    @Test
    @Timeout(20)
    void concurrentTransfersCannotOverspend() throws Exception {
        TransferFixture fixture = transferFixture(ONE_HUNDRED);
        UUID quoteA = quote(fixture.sender().userId(), SEVENTY_FIVE);
        UUID quoteB = quote(fixture.sender().userId(), SEVENTY_FIVE);

        List<Outcome> outcomes = concurrently(
                () -> transfer(fixture, fixture.recipient().userId(), quoteA, SEVENTY_FIVE, "transfer-a"),
                () -> transfer(fixture, fixture.recipient().userId(), quoteB, SEVENTY_FIVE, "transfer-b")
        );

        assertSuccessful(outcomes, 1);
        assertBalance(fixture.sender().accountId(), new BigDecimal("25.0000"));
        assertBalance(fixture.recipient().accountId(), SEVENTY_FIVE);
        assertEquals(1, transactionCount(fixture.sender().userId(), "TRANSFER"));
        assertEquals(1, ledgerCount(fixture.sender().accountId(), "DEBIT", "TRANSFER"));
        assertEquals(1, ledgerCount(fixture.recipient().accountId(), "CREDIT", "TRANSFER"));
    }

    @Test
    @Timeout(20)
    void duplicateDepositIdempotencyCreatesOneCreditAndReturnsOneTransaction() throws Exception {
        WalletAccount wallet = wallet("USD");
        List<Outcome> outcomes = concurrently(
                () -> depositService.deposit(wallet.userId(), "USD", ONE_HUNDRED, "same-deposit"),
                () -> depositService.deposit(wallet.userId(), "USD", ONE_HUNDRED, "same-deposit")
        );

        assertSameTransaction(outcomes);
        assertBalance(wallet.accountId(), ONE_HUNDRED);
        assertEquals(1, transactionCount(wallet.userId(), "DEPOSIT"));
        assertEquals(1, ledgerCount(wallet.accountId(), "CREDIT", "DEPOSIT"));
    }

    @Test
    @Timeout(20)
    void duplicateWithdrawalIdempotencyCreatesOneDebitAndReturnsOneTransaction() throws Exception {
        WalletAccount wallet = fundedWallet(ONE_HUNDRED);
        List<Outcome> outcomes = concurrently(
                () -> withdrawalService.withdraw(wallet.userId(), "USD", SEVENTY_FIVE, "same-withdrawal"),
                () -> withdrawalService.withdraw(wallet.userId(), "USD", SEVENTY_FIVE, "same-withdrawal")
        );

        assertSameTransaction(outcomes);
        assertBalance(wallet.accountId(), new BigDecimal("25.0000"));
        assertEquals(1, transactionCount(wallet.userId(), "WITHDRAWAL"));
        assertEquals(1, ledgerCount(wallet.accountId(), "DEBIT", "WITHDRAWAL"));
    }

    @Test
    @Timeout(20)
    void duplicateTransferIdempotencyConsumesTheQuoteOnce() throws Exception {
        TransferFixture fixture = transferFixture(ONE_HUNDRED);
        UUID quote = quote(fixture.sender().userId(), FIFTY);
        List<Outcome> outcomes = concurrently(
                () -> transfer(fixture, fixture.recipient().userId(), quote, FIFTY, "same-transfer"),
                () -> transfer(fixture, fixture.recipient().userId(), quote, FIFTY, "same-transfer")
        );

        assertSameTransaction(outcomes);
        assertEquals("USED", quoteStatus(quote));
        assertBalance(fixture.sender().accountId(), FIFTY);
        assertBalance(fixture.recipient().accountId(), FIFTY);
        assertEquals(1, transactionCount(fixture.sender().userId(), "TRANSFER"));
    }

    @Test
    @Timeout(20)
    void conflictingIdempotencyHasOneWinnerAndOneFinancialMutation() throws Exception {
        WalletAccount wallet = wallet("USD");
        List<Outcome> outcomes = concurrently(
                () -> depositService.deposit(wallet.userId(), "USD", FIFTY, "conflicting-key"),
                () -> depositService.deposit(wallet.userId(), "USD", SEVENTY_FIVE, "conflicting-key")
        );

        assertSuccessful(outcomes, 1);
        assertEquals(1, transactionCount(wallet.userId(), "DEPOSIT"));
        assertEquals(1, ledgerCount(wallet.accountId(), "CREDIT", "DEPOSIT"));
        assertTrue(balance(wallet.accountId()).compareTo(FIFTY) == 0
                || balance(wallet.accountId()).compareTo(SEVENTY_FIVE) == 0);
    }

    @Test
    @Timeout(20)
    void sameQuoteCannotBeConsumedByTwoDifferentTransfers() throws Exception {
        TransferFixture fixture = transferFixture(ONE_HUNDRED);
        WalletAccount otherRecipient = wallet("EUR");
        UUID quote = quote(fixture.sender().userId(), FIFTY);
        List<Outcome> outcomes = concurrently(
                () -> transfer(fixture, fixture.recipient().userId(), quote, FIFTY, "quote-a"),
                () -> transfer(fixture, otherRecipient.userId(), quote, FIFTY, "quote-b")
        );

        assertSuccessful(outcomes, 1);
        assertEquals("USED", quoteStatus(quote));
        assertEquals(1, transactionCount(fixture.sender().userId(), "TRANSFER"));
        assertEquals(1, ledgerCount(fixture.sender().accountId(), "DEBIT", "TRANSFER"));
    }

    @Test
    @Timeout(20)
    void oppositeDirectionTransfersFinishWithoutDeadlock() throws Exception {
        WalletAccount userAUsd = fundedWallet(ONE_HUNDRED);
        WalletAccount userBEur = fundedWallet("EUR", ONE_HUNDRED);
        UUID quoteA = quote(userAUsd.userId(), FIFTY, "USD", "EUR");
        UUID quoteB = quote(userBEur.userId(), FIFTY, "EUR", "USD");

        List<Outcome> outcomes = concurrently(
                () -> transfer(userAUsd.userId(), userBEur.userId(), quoteA, FIFTY, "a-to-b"),
                () -> transfer(userBEur.userId(), userAUsd.userId(), quoteB, FIFTY, "b-to-a")
        );

        assertSuccessful(outcomes, 2);
        // Each shared account is debited once and credited once. More
        // importantly, opposite directions complete instead of deadlocking.
        assertBalance(userAUsd.accountId(), ONE_HUNDRED);
        assertBalance(userBEur.accountId(), ONE_HUNDRED);
    }

    @Test
    @Timeout(20)
    void completionFailureRollsBackAllDepositWithdrawalAndTransferMutations() {
        WalletAccount depositWallet = wallet("USD");
        installCompletionFailureTrigger();
        assertThrows(RuntimeException.class, () -> depositService.deposit(
                depositWallet.userId(), "USD", FIFTY, "rollback-deposit"));
        assertBalance(depositWallet.accountId(), ZERO);
        assertEquals(0, transactionCount(depositWallet.userId(), "DEPOSIT"));

        WalletAccount withdrawalWallet = fundedWallet(ONE_HUNDRED);
        assertThrows(RuntimeException.class, () -> withdrawalService.withdraw(
                withdrawalWallet.userId(), "USD", FIFTY, "rollback-withdrawal"));
        assertBalance(withdrawalWallet.accountId(), ONE_HUNDRED);
        assertEquals(0, transactionCount(withdrawalWallet.userId(), "WITHDRAWAL"));

        TransferFixture transfer = transferFixture(ONE_HUNDRED);
        UUID quote = quote(transfer.sender().userId(), FIFTY);
        assertThrows(RuntimeException.class, () -> transfer(
                transfer, transfer.recipient().userId(), quote, FIFTY, "rollback-transfer"));
        assertBalance(transfer.sender().accountId(), ONE_HUNDRED);
        assertBalance(transfer.recipient().accountId(), ZERO);
        assertEquals("ACTIVE", quoteStatus(quote));
        assertEquals(0, transactionCount(transfer.sender().userId(), "TRANSFER"));
    }

    private List<Outcome> concurrently(Callable<Transaction> first, Callable<Transaction> second)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Outcome> firstFuture = executor.submit(() -> callAtTheSameTime(first, ready, start));
            Future<Outcome> secondFuture = executor.submit(() -> callAtTheSameTime(second, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS), "workers did not become ready");
            start.countDown();
            return List.of(firstFuture.get(10, TimeUnit.SECONDS), secondFuture.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Outcome callAtTheSameTime(Callable<Transaction> work, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                return new Outcome(null, new IllegalStateException("test start timed out"));
            }
            return new Outcome(work.call(), null);
        } catch (Throwable throwable) {
            return new Outcome(null, throwable);
        }
    }

    private TransferFixture transferFixture(BigDecimal initialBalance) {
        WalletAccount sender = fundedWallet(initialBalance);
        WalletAccount recipient = wallet("EUR");
        return new TransferFixture(sender, recipient);
    }

    private Transaction transfer(TransferFixture fixture, UUID recipientId, UUID quoteId,
                                 BigDecimal amount, String key) {
        return transfer(fixture.sender().userId(), recipientId, quoteId, amount, key);
    }

    private Transaction transfer(UUID senderId, UUID recipientId, UUID quoteId,
                                 BigDecimal amount, String key) {
        return transferService.transfer(senderId, new TransferRequest(recipientId, quoteId, amount), key);
    }

    private WalletAccount fundedWallet(BigDecimal amount) {
        return fundedWallet("USD", amount);
    }

    private WalletAccount fundedWallet(String currency, BigDecimal amount) {
        WalletAccount wallet = wallet(currency);
        UUID transactionId = UUID.randomUUID();
        jdbc.update("INSERT INTO transactions (id, transaction_type, status, currency, amount, created_at, completed_at, initiated_by_user_id) "
                        + "VALUES (?, 'DEPOSIT', 'COMPLETED', ?, ?, ?, ?, ?)",
                transactionId, currency, amount, OffsetDateTime.now(), OffsetDateTime.now(), wallet.userId());
        jdbc.update("INSERT INTO ledger_entries (id, ledger_account_id, transaction_id, amount, entry_type, currency, reference_type, reference_id, created_at) "
                        + "VALUES (?, ?, ?, ?, 'CREDIT', ?, 'SEED', ?, ?)",
                UUID.randomUUID(), wallet.accountId(), transactionId, amount, currency, transactionId, OffsetDateTime.now());
        return wallet;
    }

    private WalletAccount wallet(String currency) {
        UUID userId = user();
        return walletFor(userId, currency);
    }

    private WalletAccount walletFor(UUID userId, String currency) {
        UUID walletId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        jdbc.update("INSERT INTO wallets (id, user_id, currency, balance, status, created_at, updated_at) "
                        + "VALUES (?, ?, ?, 0, 'ACTIVE', ?, ?)", walletId, userId, currency, now, now);
        jdbc.update("INSERT INTO ledger_accounts (id, wallet_id, account_type, currency, created_at) "
                        + "VALUES (?, ?, 'USER_WALLET', ?, ?)", accountId, walletId, currency, now);
        return new WalletAccount(userId, accountId);
    }

    private UUID user() {
        UUID userId = UUID.randomUUID();
        users.add(userId);
        OffsetDateTime now = OffsetDateTime.now();
        jdbc.update("INSERT INTO users (id, email, password_hash, first_name, last_name, country, status, created_at, updated_at) "
                        + "VALUES (?, ?, 'hash', 'Concurrency', 'Test', 'US', 'ACTIVE', ?, ?)",
                userId, userId + "@example.test", now, now);
        return userId;
    }

    private UUID quote(UUID userId, BigDecimal amount) {
        return quote(userId, amount, "USD", "EUR");
    }

    private UUID quote(UUID userId, BigDecimal amount, String from, String to) {
        UUID quoteId = UUID.randomUUID();
        jdbc.update("INSERT INTO fx_quotes (id, user_id, from_currency, to_currency, source_amount, exchange_rate, converted_amount, fee_amount, status, expires_at, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, 1.00000000, ?, 0, 'ACTIVE', ?, ?)",
                quoteId, userId, from, to, amount, amount,
                OffsetDateTime.now().plusMinutes(5), OffsetDateTime.now());
        return quoteId;
    }

    private void installCompletionFailureTrigger() {
        jdbc.execute("CREATE FUNCTION crosspay_test_fail_completion() RETURNS trigger LANGUAGE plpgsql AS "
                + "$$ BEGIN RAISE EXCEPTION 'forced completion failure'; END; $$");
        jdbc.execute("CREATE TRIGGER crosspay_test_fail_completion BEFORE UPDATE OF status ON transactions "
                + "FOR EACH ROW WHEN (NEW.status = 'COMPLETED') "
                + "EXECUTE FUNCTION crosspay_test_fail_completion()");
    }

    private void assertSameTransaction(List<Outcome> outcomes) {
        assertSuccessful(outcomes, 2);
        assertNotNull(outcomes.get(0).transaction());
        assertEquals(outcomes.get(0).transaction().getId(), outcomes.get(1).transaction().getId());
    }

    private void assertSuccessful(List<Outcome> outcomes, int expected) {
        long successful = outcomes.stream().filter(outcome -> outcome.transaction() != null).count();
        assertEquals(expected, successful, () -> "outcomes: " + outcomes);
        if (expected < outcomes.size()) {
            assertTrue(outcomes.stream().anyMatch(outcome -> outcome.failure() != null));
        }
    }

    private void assertBalance(UUID accountId, BigDecimal expected) {
        assertEquals(0, balance(accountId).compareTo(expected));
        assertTrue(balance(accountId).compareTo(ZERO) >= 0, "ledger-derived balance must not be negative");
    }

    private BigDecimal balance(UUID accountId) {
        BigDecimal value = jdbc.queryForObject("SELECT COALESCE(SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE -amount END), 0) "
                + "FROM ledger_entries WHERE ledger_account_id = ?", BigDecimal.class, accountId);
        assertNotNull(value);
        return value;
    }

    private int transactionCount(UUID initiatorId, String type) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM transactions "
                + "WHERE initiated_by_user_id = ? AND transaction_type = ?", Integer.class, initiatorId, type);
        return count == null ? 0 : count;
    }

    private int ledgerCount(UUID accountId, String entryType, String referenceType) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM ledger_entries "
                + "WHERE ledger_account_id = ? AND entry_type = ? AND reference_type = ?", Integer.class,
                accountId, entryType, referenceType);
        return count == null ? 0 : count;
    }

    private String quoteStatus(UUID quoteId) {
        return jdbc.queryForObject("SELECT status FROM fx_quotes WHERE id = ?", String.class, quoteId);
    }

    private record WalletAccount(UUID userId, UUID accountId) { }
    private record TransferFixture(WalletAccount sender, WalletAccount recipient) { }
    private record Outcome(Transaction transaction, Throwable failure) { }
}
