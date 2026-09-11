package com.crosspay.transfer.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.entity.LedgerEntry;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.service.TransactionService;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.user.entity.User;
import com.crosspay.user.repository.UserRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionService transactionService;

    public TransferService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            TransactionService transactionService
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Transaction transfer(
            UUID senderUserId,
            TransferRequest request
    ) {

        // 1. Prevent self-transfer
        if (senderUserId.equals(request.recipientUserId())) {
            throw new IllegalArgumentException(
                    "You cannot transfer money to yourself"
            );
        }

        // 2. Validate recipient
        User recipient = userRepository
                .findById(request.recipientUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient user not found"
                        )
                );

        if (!recipient.getStatus().equals("ACTIVE")) {
            throw new IllegalArgumentException(
                    "Recipient account is not active"
            );
        }

        // 3. Normalize transfer data
        String currency = request.currency()
                .trim()
                .toUpperCase();

        BigDecimal amount = request.amount();

        // 4. Find sender wallet
        Wallet senderWallet = walletRepository
                .findByUserIdAndCurrency(
                        senderUserId,
                        currency
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender wallet not found for this currency"
                        )
                );

        // 5. Find recipient wallet
        Wallet recipientWallet = walletRepository
                .findByUserIdAndCurrency(
                        request.recipientUserId(),
                        currency
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient wallet not found for this currency"
                        )
                );

        // 6. Find sender ledger account
        LedgerAccount senderAccount = ledgerAccountRepository
                .findByWalletId(senderWallet.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender ledger account not found"
                        )
                );

        // 7. Find recipient ledger account
        LedgerAccount recipientAccount = ledgerAccountRepository
                .findByWalletId(recipientWallet.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient ledger account not found"
                        )
                );

        // 8. Lock sender ledger account
        senderAccount = ledgerAccountRepository
                .findByIdForUpdate(senderAccount.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender ledger account not found"
                        )
                );

        // 9. Lock recipient ledger account
        recipientAccount = ledgerAccountRepository
                .findByIdForUpdate(recipientAccount.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipient ledger account not found"
                        )
                );

        // 10. Calculate sender balance after acquiring lock
        BigDecimal senderBalance =
                ledgerEntryRepository.calculateBalance(
                        senderAccount.getId()
                );

        if (senderBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        // 11. Create pending transfer transaction
        Transaction savedTransaction =
                transactionService.createTransferTransaction(
                        senderUserId,
                        request.recipientUserId(),
                        currency
                );

        OffsetDateTime now = savedTransaction.getCreatedAt();

        // 12. Create sender debit
        LedgerEntry debit = new LedgerEntry();

        debit.setId(UUID.randomUUID());
        debit.setLedgerAccountId(senderAccount.getId());
        debit.setTransactionId(savedTransaction.getId());
        debit.setAmount(amount);
        debit.setEntryType("DEBIT");
        debit.setCurrency(currency);
        debit.setReferenceType("TRANSFER");
        debit.setReferenceId(savedTransaction.getId());
        debit.setCreatedAt(now);

        ledgerEntryRepository.save(debit);

        // 13. Create recipient credit
        LedgerEntry credit = new LedgerEntry();

        credit.setId(UUID.randomUUID());
        credit.setLedgerAccountId(recipientAccount.getId());
        credit.setTransactionId(savedTransaction.getId());
        credit.setAmount(amount);
        credit.setEntryType("CREDIT");
        credit.setCurrency(currency);
        credit.setReferenceType("TRANSFER");
        credit.setReferenceId(savedTransaction.getId());
        credit.setCreatedAt(now);

        ledgerEntryRepository.save(credit);

        // 14. Complete transaction
        return transactionService.completeTransaction(
                savedTransaction.getId()
        );
    }
}