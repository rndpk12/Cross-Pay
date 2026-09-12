package com.crosspay.transfer;

import com.crosspay.fx.entity.FxQuote;
import com.crosspay.fx.service.FxQuoteService;
import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import com.crosspay.transaction.service.TransactionService;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.transfer.service.TransferService;
import com.crosspay.user.entity.User;
import com.crosspay.user.repository.UserRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private WalletService walletService;

    @Mock
    private FxQuoteService fxQuoteService;

    @InjectMocks
    private TransferService transferService;


    // =========================================================
    // TEST 1
    // Self transfer
    // =========================================================

    @Test
    void shouldRejectSelfTransfer() {

        UUID userId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                userId,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferService.transfer(
                                userId,
                                request,
                                "self-transfer-test"
                        )
                );

        assertEquals(
                "You cannot transfer money to yourself",
                exception.getMessage()
        );
    }


    // =========================================================
    // TEST 2
    // Insufficient balance
    // =========================================================

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {

        UUID senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        String idempotencyKey =
                "insufficient-balance-test";

        BigDecimal transferAmount =
                new BigDecimal("100.00");

        BigDecimal destinationAmount =
                new BigDecimal("8300.00");

        TransferRequest request =
                new TransferRequest(
                        recipientUserId,
                        quoteId,
                        transferAmount
                );

        // -----------------------------------------------------
        // Sender
        // -----------------------------------------------------

        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));

        when(
                userRepository.findById(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        // -----------------------------------------------------
        // Recipient
        // -----------------------------------------------------

        User recipient = new User();

        recipient.setId(recipientUserId);
        recipient.setStatus("ACTIVE");

        when(
                userRepository.findById(
                        recipientUserId
                )
        ).thenReturn(Optional.of(recipient));


        // -----------------------------------------------------
        // Idempotency
        // -----------------------------------------------------

        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());


        // -----------------------------------------------------
        // FX quote
        // -----------------------------------------------------

        FxQuote quote = new FxQuote();

        quote.setId(quoteId);
        quote.setUserId(senderUserId);
        quote.setFromCurrency("USD");
        quote.setToCurrency("INR");
        quote.setSourceAmount(transferAmount);
        quote.setConvertedAmount(destinationAmount);
        quote.setStatus("ACTIVE");

        when(
                fxQuoteService.getQuote(
                        senderUserId,
                        quoteId
                )
        ).thenReturn(quote);


        // -----------------------------------------------------
        // Sender wallet
        // -----------------------------------------------------

        Wallet senderWallet = new Wallet();

        senderWallet.setId(UUID.randomUUID());
        senderWallet.setUserId(senderUserId);
        senderWallet.setCurrency("USD");
        senderWallet.setStatus("ACTIVE");

        when(
                walletService.findByUserIdAndCurrency(
                        senderUserId,
                        "USD"
                )
        ).thenReturn(Optional.of(senderWallet));


        // -----------------------------------------------------
        // Recipient wallet
        // -----------------------------------------------------

        Wallet recipientWallet = new Wallet();

        recipientWallet.setId(UUID.randomUUID());
        recipientWallet.setUserId(recipientUserId);
        recipientWallet.setCurrency("INR");
        recipientWallet.setStatus("ACTIVE");

        when(
                walletService.findByUserIdAndCurrency(
                        recipientUserId,
                        "INR"
                )
        ).thenReturn(Optional.of(recipientWallet));


        // -----------------------------------------------------
        // Sender ledger account
        // -----------------------------------------------------

        LedgerAccount senderAccount =
                new LedgerAccount();

        senderAccount.setId(UUID.randomUUID());
        senderAccount.setWalletId(
                senderWallet.getId()
        );
        senderAccount.setCurrency("USD");
        senderAccount.setAccountType("USER_WALLET");

        when(
                ledgerAccountRepository.findByWalletId(
                        senderWallet.getId()
                )
        ).thenReturn(Optional.of(senderAccount));


        // -----------------------------------------------------
        // Recipient ledger account
        // -----------------------------------------------------

        LedgerAccount recipientAccount =
                new LedgerAccount();

        recipientAccount.setId(UUID.randomUUID());
        recipientAccount.setWalletId(
                recipientWallet.getId()
        );
        recipientAccount.setCurrency("INR");
        recipientAccount.setAccountType("USER_WALLET");

        when(
                ledgerAccountRepository.findByWalletId(
                        recipientWallet.getId()
                )
        ).thenReturn(Optional.of(recipientAccount));


        // -----------------------------------------------------
        // Ledger locks
        // -----------------------------------------------------

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        senderAccount.getId()
                )
        ).thenReturn(Optional.of(senderAccount));

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        recipientAccount.getId()
                )
        ).thenReturn(Optional.of(recipientAccount));


        // -----------------------------------------------------
        // Balance
        // -----------------------------------------------------

        when(
                ledgerEntryRepository.calculateBalance(
                        senderAccount.getId()
                )
        ).thenReturn(
                new BigDecimal("50.00")
        );


        // -----------------------------------------------------
        // Execute
        // -----------------------------------------------------

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferService.transfer(
                                senderUserId,
                                request,
                                idempotencyKey
                        )
                );


        assertEquals(
                "Insufficient balance",
                exception.getMessage()
        );


        // -----------------------------------------------------
        // No transaction
        // -----------------------------------------------------

        verify(
                transactionService,
                never()
        ).createTransferTransaction(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(BigDecimal.class),
                any(String.class),
                any(BigDecimal.class),
                any(UUID.class),
                any(String.class)
        );


        verify(
                ledgerEntryRepository,
                never()
        ).save(
                any(LedgerEntry.class)
        );
    }


    // =========================================================
    // TEST 3
    // Successful USD -> INR transfer
    // =========================================================

    @Test
    void shouldSuccessfullyTransferAcrossCurrencies() {

        UUID senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String idempotencyKey =
                "successful-transfer-test";

        BigDecimal sourceAmount =
                new BigDecimal("100.00");

        BigDecimal destinationAmount =
                new BigDecimal("8300.00");

        TransferRequest request =
                new TransferRequest(
                        recipientUserId,
                        quoteId,
                        sourceAmount
                );


        // -----------------------------------------------------
        // Sender
        // -----------------------------------------------------

        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));

        when(
                userRepository.findById(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        // -----------------------------------------------------
        // Recipient
        // -----------------------------------------------------

        User recipient = new User();

        recipient.setId(recipientUserId);
        recipient.setStatus("ACTIVE");

        when(
                userRepository.findById(
                        recipientUserId
                )
        ).thenReturn(Optional.of(recipient));


        // -----------------------------------------------------
        // Idempotency
        // -----------------------------------------------------

        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());


        // -----------------------------------------------------
        // FX quote
        // -----------------------------------------------------

        FxQuote quote = new FxQuote();

        quote.setId(quoteId);
        quote.setUserId(senderUserId);
        quote.setFromCurrency("USD");
        quote.setToCurrency("INR");
        quote.setSourceAmount(sourceAmount);
        quote.setConvertedAmount(destinationAmount);
        quote.setStatus("ACTIVE");

        when(
                fxQuoteService.getQuote(
                        senderUserId,
                        quoteId
                )
        ).thenReturn(quote);


        // -----------------------------------------------------
        // Sender wallet
        // -----------------------------------------------------

        Wallet senderWallet = new Wallet();

        senderWallet.setId(UUID.randomUUID());
        senderWallet.setUserId(senderUserId);
        senderWallet.setCurrency("USD");
        senderWallet.setStatus("ACTIVE");

        when(
                walletService.findByUserIdAndCurrency(
                        senderUserId,
                        "USD"
                )
        ).thenReturn(Optional.of(senderWallet));


        // -----------------------------------------------------
        // Recipient wallet
        // -----------------------------------------------------

        Wallet recipientWallet = new Wallet();

        recipientWallet.setId(UUID.randomUUID());
        recipientWallet.setUserId(recipientUserId);
        recipientWallet.setCurrency("INR");
        recipientWallet.setStatus("ACTIVE");

        when(
                walletService.findByUserIdAndCurrency(
                        recipientUserId,
                        "INR"
                )
        ).thenReturn(Optional.of(recipientWallet));


        // -----------------------------------------------------
        // Ledger accounts
        // -----------------------------------------------------

        LedgerAccount senderAccount =
                new LedgerAccount();

        senderAccount.setId(UUID.randomUUID());
        senderAccount.setWalletId(
                senderWallet.getId()
        );
        senderAccount.setCurrency("USD");
        senderAccount.setAccountType("USER_WALLET");


        LedgerAccount recipientAccount =
                new LedgerAccount();

        recipientAccount.setId(UUID.randomUUID());
        recipientAccount.setWalletId(
                recipientWallet.getId()
        );
        recipientAccount.setCurrency("INR");
        recipientAccount.setAccountType("USER_WALLET");


        when(
                ledgerAccountRepository.findByWalletId(
                        senderWallet.getId()
                )
        ).thenReturn(Optional.of(senderAccount));

        when(
                ledgerAccountRepository.findByWalletId(
                        recipientWallet.getId()
                )
        ).thenReturn(Optional.of(recipientAccount));


        // -----------------------------------------------------
        // Ledger locks
        // -----------------------------------------------------

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        senderAccount.getId()
                )
        ).thenReturn(Optional.of(senderAccount));

        when(
                ledgerAccountRepository.findByIdForUpdate(
                        recipientAccount.getId()
                )
        ).thenReturn(Optional.of(recipientAccount));


        // -----------------------------------------------------
        // Balance
        // -----------------------------------------------------

        when(
                ledgerEntryRepository.calculateBalance(
                        senderAccount.getId()
                )
        ).thenReturn(
                new BigDecimal("500.00")
        );


        // -----------------------------------------------------
        // Transaction
        // -----------------------------------------------------

        OffsetDateTime createdAt =
                OffsetDateTime.now();

        Transaction savedTransaction =
                new Transaction();

        savedTransaction.setId(transactionId);
        savedTransaction.setSenderUserId(senderUserId);
        savedTransaction.setRecipientUserId(recipientUserId);
        savedTransaction.setTransactionType("TRANSFER");
        savedTransaction.setStatus("PENDING");
        savedTransaction.setCurrency("USD");
        savedTransaction.setAmount(sourceAmount);
        savedTransaction.setSourceCurrency("USD");
        savedTransaction.setDestinationCurrency("INR");
        savedTransaction.setSourceAmount(sourceAmount);
        savedTransaction.setDestinationAmount(
                destinationAmount
        );
        savedTransaction.setFxQuoteId(quoteId);
        savedTransaction.setIdempotencyKey(
                idempotencyKey
        );
        savedTransaction.setCreatedAt(createdAt);


        when(
                transactionService.createTransferTransaction(
                        senderUserId,
                        recipientUserId,
                        "USD",
                        sourceAmount,
                        "INR",
                        destinationAmount,
                        quoteId,
                        idempotencyKey
                )
        ).thenReturn(savedTransaction);


        // -----------------------------------------------------
        // Completion
        // -----------------------------------------------------

        Transaction completedTransaction =
                new Transaction();

        completedTransaction.setId(transactionId);
        completedTransaction.setStatus("COMPLETED");
        completedTransaction.setCreatedAt(createdAt);

        when(
                transactionService.completeTransaction(
                        transactionId
                )
        ).thenReturn(completedTransaction);


        // -----------------------------------------------------
        // Execute
        // -----------------------------------------------------

        Transaction result =
                transferService.transfer(
                        senderUserId,
                        request,
                        idempotencyKey
                );


        // -----------------------------------------------------
        // Verify
        // -----------------------------------------------------

        assertEquals(
                transactionId,
                result.getId()
        );

        assertEquals(
                "COMPLETED",
                result.getStatus()
        );

        verify(
                ledgerEntryRepository,
                times(2)
        ).save(
                any(LedgerEntry.class)
        );

        verify(
                fxQuoteService
        ).useQuote(
                senderUserId,
                quoteId
        );

        verify(
                transactionService
        ).completeTransaction(
                transactionId
        );
    }


    // =========================================================
    // TEST 4
    // Duplicate idempotency key
    // =========================================================

    @Test
    void shouldReturnExistingTransactionForDuplicateIdempotencyKey() {

        UUID senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String idempotencyKey =
                "duplicate-transfer-test";

        BigDecimal amount =
                new BigDecimal("100.00");

        TransferRequest request =
                new TransferRequest(
                        recipientUserId,
                        quoteId,
                        amount
                );


        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setId(transactionId);
        existingTransaction.setSenderUserId(senderUserId);
        existingTransaction.setRecipientUserId(
                recipientUserId
        );
        existingTransaction.setTransactionType(
                "TRANSFER"
        );
        existingTransaction.setStatus(
                "COMPLETED"
        );
        existingTransaction.setCurrency("USD");
        existingTransaction.setAmount(amount);
        existingTransaction.setFxQuoteId(quoteId);
        existingTransaction.setIdempotencyKey(
                idempotencyKey
        );


        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(
                Optional.of(existingTransaction)
        );


        Transaction result =
                transferService.transfer(
                        senderUserId,
                        request,
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


        verify(
                transactionService,
                never()
        ).createTransferTransaction(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(BigDecimal.class),
                any(String.class),
                any(BigDecimal.class),
                any(UUID.class),
                any(String.class)
        );


        verify(
                ledgerEntryRepository,
                never()
        ).save(
                any(LedgerEntry.class)
        );


        verify(
                fxQuoteService,
                never()
        ).getQuote(
                any(UUID.class),
                any(UUID.class)
        );
    }


    // =========================================================
    // TEST 5
    // Same idempotency key, different transfer
    // =========================================================

    @Test
    void shouldRejectDifferentTransferUsingSameIdempotencyKey() {

        UUID senderUserId = UUID.randomUUID();

        UUID originalRecipientId =
                UUID.randomUUID();

        UUID differentRecipientId =
                UUID.randomUUID();

        UUID originalQuoteId =
                UUID.randomUUID();

        UUID newQuoteId =
                UUID.randomUUID();

        String idempotencyKey =
                "reused-idempotency-key";

        BigDecimal originalAmount =
                new BigDecimal("100.00");

        BigDecimal differentAmount =
                new BigDecimal("200.00");


        TransferRequest request =
                new TransferRequest(
                        differentRecipientId,
                        newQuoteId,
                        differentAmount
                );


        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setId(
                UUID.randomUUID()
        );

        existingTransaction.setSenderUserId(
                senderUserId
        );

        existingTransaction.setRecipientUserId(
                originalRecipientId
        );

        existingTransaction.setTransactionType(
                "TRANSFER"
        );

        existingTransaction.setStatus(
                "COMPLETED"
        );

        existingTransaction.setCurrency(
                "USD"
        );

        existingTransaction.setAmount(
                originalAmount
        );

        existingTransaction.setFxQuoteId(
                originalQuoteId
        );

        existingTransaction.setIdempotencyKey(
                idempotencyKey
        );


        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(
                Optional.of(existingTransaction)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferService.transfer(
                                senderUserId,
                                request,
                                idempotencyKey
                        )
                );


        assertEquals(
                "Idempotency key was already used for a different transfer",
                exception.getMessage()
        );


        verify(
                transactionService,
                never()
        ).createTransferTransaction(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(BigDecimal.class),
                any(String.class),
                any(BigDecimal.class),
                any(UUID.class),
                any(String.class)
        );


        verify(
                ledgerEntryRepository,
                never()
        ).save(
                any(LedgerEntry.class)
        );
    }


    // =========================================================
    // TEST 6
    // Expired / inactive FX quote
    // =========================================================

    @Test
    void shouldRejectTransferWhenFxQuoteIsExpired() {

        UUID senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        String idempotencyKey =
                "expired-quote-test";

        BigDecimal transferAmount =
                new BigDecimal("100.00");

        BigDecimal destinationAmount =
                new BigDecimal("8300.00");


        TransferRequest request =
                new TransferRequest(
                        recipientUserId,
                        quoteId,
                        transferAmount
                );


        // -----------------------------------------------------
        // Sender lock
        // -----------------------------------------------------

        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));

        // TransferService loads the sender again after the lock.
        when(
                userRepository.findById(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        // -----------------------------------------------------
        // Idempotency
        // -----------------------------------------------------

        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());


        // -----------------------------------------------------
        // Recipient
        // -----------------------------------------------------

        User recipient = new User();

        recipient.setId(recipientUserId);
        recipient.setStatus("ACTIVE");

        when(
                userRepository.findById(
                        recipientUserId
                )
        ).thenReturn(Optional.of(recipient));


        // -----------------------------------------------------
        // Expired quote
        // -----------------------------------------------------

        FxQuote expiredQuote =
                new FxQuote();

        expiredQuote.setId(quoteId);
        expiredQuote.setUserId(senderUserId);
        expiredQuote.setFromCurrency("USD");
        expiredQuote.setToCurrency("INR");
        expiredQuote.setSourceAmount(
                transferAmount
        );
        expiredQuote.setConvertedAmount(
                destinationAmount
        );
        expiredQuote.setStatus("EXPIRED");


        when(
                fxQuoteService.getQuote(
                        senderUserId,
                        quoteId
                )
        ).thenReturn(expiredQuote);


        // -----------------------------------------------------
        // Execute
        // -----------------------------------------------------

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferService.transfer(
                                senderUserId,
                                request,
                                idempotencyKey
                        )
                );


        assertEquals(
                "FX quote is not active",
                exception.getMessage()
        );


        // -----------------------------------------------------
        // No wallet lookup
        // -----------------------------------------------------

        verify(
                walletService,
                never()
        ).findByUserIdAndCurrency(
                senderUserId,
                "USD"
        );

        verify(
                walletService,
                never()
        ).findByUserIdAndCurrency(
                recipientUserId,
                "INR"
        );


        // -----------------------------------------------------
        // No ledger movement
        // -----------------------------------------------------

        verify(
                ledgerEntryRepository,
                never()
        ).save(
                any(LedgerEntry.class)
        );


        // -----------------------------------------------------
        // No transaction creation
        // -----------------------------------------------------

        verify(
                transactionService,
                never()
        ).createTransferTransaction(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(BigDecimal.class),
                any(String.class),
                any(BigDecimal.class),
                any(UUID.class),
                any(String.class)
        );


        // -----------------------------------------------------
        // Quote not consumed
        // -----------------------------------------------------

        verify(
                fxQuoteService,
                never()
        ).useQuote(
                senderUserId,
                quoteId
        );
    }


    // =========================================================
    // TEST 7
    // Transfer amount does not match quote
    // =========================================================

    @Test
    void shouldRejectTransferWhenAmountDoesNotMatchQuote() {

        UUID senderUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        String idempotencyKey =
                "amount-mismatch-test";

        BigDecimal requestedAmount =
                new BigDecimal("200.00");

        BigDecimal quotedAmount =
                new BigDecimal("100.00");

        BigDecimal destinationAmount =
                new BigDecimal("8300.00");


        TransferRequest request =
                new TransferRequest(
                        recipientUserId,
                        quoteId,
                        requestedAmount
                );


        // -----------------------------------------------------
        // Sender
        // -----------------------------------------------------

        User sender = new User();

        sender.setId(senderUserId);
        sender.setStatus("ACTIVE");

        when(
                userRepository.findByIdForUpdate(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));

        when(
                userRepository.findById(
                        senderUserId
                )
        ).thenReturn(Optional.of(sender));


        // -----------------------------------------------------
        // Recipient
        // -----------------------------------------------------

        User recipient = new User();

        recipient.setId(recipientUserId);
        recipient.setStatus("ACTIVE");

        when(
                userRepository.findById(
                        recipientUserId
                )
        ).thenReturn(Optional.of(recipient));


        // -----------------------------------------------------
        // Idempotency
        // -----------------------------------------------------

        when(
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                idempotencyKey
                        )
        ).thenReturn(Optional.empty());


        // -----------------------------------------------------
        // Quote
        // -----------------------------------------------------

        FxQuote quote = new FxQuote();

        quote.setId(quoteId);
        quote.setUserId(senderUserId);
        quote.setFromCurrency("USD");
        quote.setToCurrency("INR");
        quote.setSourceAmount(quotedAmount);
        quote.setConvertedAmount(destinationAmount);
        quote.setStatus("ACTIVE");


        when(
                fxQuoteService.getQuote(
                        senderUserId,
                        quoteId
                )
        ).thenReturn(quote);


        // -----------------------------------------------------
        // Execute
        // -----------------------------------------------------

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferService.transfer(
                                senderUserId,
                                request,
                                idempotencyKey
                        )
                );


        assertEquals(
                "Transfer amount does not match FX quote",
                exception.getMessage()
        );


        // -----------------------------------------------------
        // No wallet lookup
        // -----------------------------------------------------

        verify(
                walletService,
                never()
        ).findByUserIdAndCurrency(
                any(UUID.class),
                any(String.class)
        );


        // -----------------------------------------------------
        // No ledger movement
        // -----------------------------------------------------

        verify(
                ledgerEntryRepository,
                never()
        ).save(
                any(LedgerEntry.class)
        );


        // -----------------------------------------------------
        // No transaction creation
        // -----------------------------------------------------

        verify(
                transactionService,
                never()
        ).createTransferTransaction(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(BigDecimal.class),
                any(String.class),
                any(BigDecimal.class),
                any(UUID.class),
                any(String.class)
        );


        // -----------------------------------------------------
        // Quote not consumed
        // -----------------------------------------------------

        verify(
                fxQuoteService,
                never()
        ).useQuote(
                senderUserId,
                quoteId
        );
    }
}