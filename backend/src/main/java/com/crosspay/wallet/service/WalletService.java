package com.crosspay.wallet.service;

import com.crosspay.ledger.entity.LedgerAccount;
import com.crosspay.ledger.repository.LedgerAccountRepository;
import com.crosspay.ledger.repository.LedgerEntryRepository;
import com.crosspay.wallet.entity.Wallet;
import com.crosspay.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(
            WalletRepository walletRepository,
            LedgerAccountRepository ledgerAccountRepository,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Wallet createWallet(UUID userId, String currency) {

        String normalizedCurrency = currency.trim().toUpperCase();

        if (walletRepository
                .findByUserIdAndCurrency(userId, normalizedCurrency)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Wallet already exists for this currency"
            );
        }

        Wallet wallet = new Wallet();

        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setCurrency(normalizedCurrency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus("ACTIVE");

        OffsetDateTime now = OffsetDateTime.now();
        wallet.setCreatedAt(now);
        wallet.setUpdatedAt(now);

        Wallet savedWallet = walletRepository.save(wallet);

        LedgerAccount ledgerAccount = new LedgerAccount();

        ledgerAccount.setId(UUID.randomUUID());
        ledgerAccount.setWalletId(savedWallet.getId());
        ledgerAccount.setAccountType("USER_WALLET");
        ledgerAccount.setCurrency(normalizedCurrency);
        ledgerAccount.setCreatedAt(now);

        ledgerAccountRepository.save(ledgerAccount);

        return savedWallet;
    }

    public Optional<Wallet> findByUserIdAndCurrency(
            UUID userId,
            String currency
    ) {
        return walletRepository.findByUserIdAndCurrency(
                userId,
                currency.trim().toUpperCase()
        );
    }

    public List<Wallet> findByUserId(UUID userId) {
        return walletRepository.findByUserId(userId);
    }

    public BigDecimal calculateBalance(UUID walletId) {

        LedgerAccount account = ledgerAccountRepository
                .findByWalletId(walletId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ledger account not found for wallet"
                        )
                );

        return ledgerEntryRepository.calculateBalance(
                account.getId()
        );
    }
}