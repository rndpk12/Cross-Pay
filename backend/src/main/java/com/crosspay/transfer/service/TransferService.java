package com.crosspay.transfer.service;

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
import com.crosspay.user.entity.User;
import com.crosspay.user.repository.UserRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final FxQuoteService fxQuoteService;

    public TransferService(
            UserRepository userRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            TransactionRepository transactionRepository,
            TransactionService transactionService,
            WalletService walletService,
            FxQuoteService fxQuoteService
    ) {
        this.userRepository = userRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.fxQuoteService = fxQuoteService;
    }

    @Transactional
    public Transaction transfer(
            UUID senderUserId,
            TransferRequest request,
            String idempotencyKey
    ) {

        // Validate sender
        if (senderUserId == null) {
            throw new IllegalArgumentException(
                    "Sender user ID is required"
            );
        }

        // Validate idempotency key
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required"
            );
        }

        String normalizedIdempotencyKey = idempotencyKey.trim();

        if (normalizedIdempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 100 characters"
            );
        }

        // Validate request
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transfer request is required"
            );
        }

        if (request.recipientUserId() == null) {
            throw new IllegalArgumentException(
                    "Recipient user ID is required"
            );
        }

        if (request.quoteId() == null) {
            throw new IllegalArgumentException(
                    "Quote ID is required"
            );
        }

        if (request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        // Prevent self-transfer
        if (senderUserId.equals(request.recipientUserId())) {
            throw new IllegalArgumentException(
                    "You cannot transfer money to yourself"
            );
        }

        /*
         * Lock the sender before checking idempotency.
         *
         * This prevents two concurrent requests from the same sender
         * from both passing the idempotency check at the same time.
         */
        userRepository.findByIdForUpdate(senderUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender user not found"
                        )
                );

        /*
         * Check idempotency BEFORE loading the FX quote.
         *
         * A retry of a completed transfer has a USED FX quote,
         * so the existing transaction must be returned first.
         */
        Transaction existingTransaction =
                transactionRepository
                        .findBySenderUserIdAndIdempotencyKey(
                                senderUserId,
                                normalizedIdempotencyKey
                        )
                        .orElse(null);

        if (existingTransaction != null) {

            boolean sameRecipient =
                    request.recipientUserId()
                            .equals(existingTransaction.getRecipientUserId());

            boolean sameAmount =
                    request.amount().compareTo(
                            existingTransaction.getAmount()
                    ) == 0;

            if (!sameRecipient || !sameAmount) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different transfer"
                );
            }

            return existingTransaction;
        }

        // Load sender
        User sender =
                userRepository.findById(senderUserId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Sender user not found"
                                )
                        );

        if (!"ACTIVE".equals(sender.getStatus())) {
            throw new IllegalArgumentException(
                    "Sender account is not active"
            );
        }

        // Load recipient
        User recipient =
                userRepository.findById(request.recipientUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Recipient user not found"
                                )
                        );

        if (!"ACTIVE".equals(recipient.getStatus())) {
            throw new IllegalArgumentException(
                    "Recipient account is not active"
            );
        }

        // Load FX quote
        FxQuote quote =
                fxQuoteService.getQuote(
                        senderUserId,
                        request.quoteId()
                );

        if (!"ACTIVE".equals(quote.getStatus())) {
            throw new IllegalArgumentException(
                    "FX quote is not active"
            );
        }

        // Verify transfer amount matches quote
        if (request.amount().compareTo(
                quote.getSourceAmount()
        ) != 0) {

            throw new IllegalArgumentException(
                    "Transfer amount does not match FX quote"
            );
        }

        String sourceCurrency = quote.getFromCurrency();
        String destinationCurrency = quote.getToCurrency();

        BigDecimal sourceAmount = quote.getSourceAmount();
        BigDecimal destinationAmount = quote.getConvertedAmount();

        // Find sender wallet
        Wallet senderWallet =
                walletService.findByUserIdAndCurrency(
                        senderUserId,
                        sourceCurrency
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender wallet not found or is not active for this currency"
                        )
                );

        // Find recipient wallet
        Wallet recipientWallet =
                walletService.findByUserIdAndCurrency(
                        request.recipientUserId(),
                        destinationCurrency
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient wallet not found or is not active for this currency"
                        )
                );

        // Find sender ledger account
        LedgerAccount senderAccount =
                ledgerAccountRepository.findByWalletId(
                        senderWallet.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender ledger account not found"
                        )
                );

        // Find recipient ledger account
        LedgerAccount recipientAccount =
                ledgerAccountRepository.findByWalletId(
                        recipientWallet.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient ledger account not found"
                        )
                );

        UUID senderAccountId = senderAccount.getId();
        UUID recipientAccountId = recipientAccount.getId();

        // Lock both ledger accounts in deterministic order
        UUID firstAccountId;
        UUID secondAccountId;

        if (senderAccountId.compareTo(recipientAccountId) < 0) {
            firstAccountId = senderAccountId;
            secondAccountId = recipientAccountId;
        } else {
            firstAccountId = recipientAccountId;
            secondAccountId = senderAccountId;
        }

        ledgerAccountRepository.findByIdForUpdate(firstAccountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ledger account not found"
                        )
                );

        ledgerAccountRepository.findByIdForUpdate(secondAccountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ledger account not found"
                        )
                );

        // Calculate sender balance
        BigDecimal senderBalance =
                ledgerEntryRepository.calculateBalance(
                        senderAccountId
                );

        if (senderBalance.compareTo(sourceAmount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        // Create transaction
        Transaction savedTransaction =
                transactionService.createTransferTransaction(
                        senderUserId,
                        request.recipientUserId(),
                        sourceCurrency,
                        sourceAmount,
                        destinationCurrency,
                        destinationAmount,
                        quote.getId(),
                        normalizedIdempotencyKey
                );

        OffsetDateTime now = savedTransaction.getCreatedAt();

        // Create sender debit
        LedgerEntry debit = new LedgerEntry();

        debit.setId(UUID.randomUUID());
        debit.setLedgerAccountId(senderAccountId);
        debit.setTransactionId(savedTransaction.getId());
        debit.setAmount(sourceAmount);
        debit.setEntryType("DEBIT");
        debit.setCurrency(sourceCurrency);
        debit.setReferenceType("TRANSFER");
        debit.setReferenceId(savedTransaction.getId());
        debit.setCreatedAt(now);

        ledgerEntryRepository.save(debit);

        // Create recipient credit
        LedgerEntry credit = new LedgerEntry();

        credit.setId(UUID.randomUUID());
        credit.setLedgerAccountId(recipientAccountId);
        credit.setTransactionId(savedTransaction.getId());
        credit.setAmount(destinationAmount);
        credit.setEntryType("CREDIT");
        credit.setCurrency(destinationCurrency);
        credit.setReferenceType("TRANSFER");
        credit.setReferenceId(savedTransaction.getId());
        credit.setCreatedAt(now);

        ledgerEntryRepository.save(credit);

        // Mark FX quote as used
        fxQuoteService.useQuote(
                senderUserId,
                quote.getId()
        );

        // Complete transaction
        return transactionService.completeTransaction(
                savedTransaction.getId()
        );
    }
}