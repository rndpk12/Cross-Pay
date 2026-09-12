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
import java.util.Set;
import java.util.UUID;

@Service
public class WalletService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "USD",
            "EUR",
            "GBP",
            "INR",
            "CAD",
            "AUD",
            "SGD",
            "JPY"
    );

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
    public Wallet createWallet(
            UUID userId,
            String currency
    ) {

        validateUserId(userId);

        String normalizedCurrency =
                normalizeAndValidateCurrency(currency);

        /*
         * Prevent duplicate wallet for the same
         * user and currency.
         */
        if (walletRepository
                .findByUserIdAndCurrency(
                        userId,
                        normalizedCurrency
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Wallet already exists for this currency"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        /*
         * Create wallet.
         */
        Wallet wallet =
                new Wallet();

        wallet.setId(
                UUID.randomUUID()
        );

        wallet.setUserId(
                userId
        );

        wallet.setCurrency(
                normalizedCurrency
        );

        wallet.setBalance(
                BigDecimal.ZERO
        );

        wallet.setStatus(
                "ACTIVE"
        );

        wallet.setCreatedAt(
                now
        );

        wallet.setUpdatedAt(
                now
        );

        Wallet savedWallet =
                walletRepository.save(wallet);

        /*
         * Every wallet gets exactly one
         * corresponding ledger account.
         */
        LedgerAccount ledgerAccount =
                new LedgerAccount();

        ledgerAccount.setId(
                UUID.randomUUID()
        );

        ledgerAccount.setWalletId(
                savedWallet.getId()
        );

        ledgerAccount.setAccountType(
                "USER_WALLET"
        );

        ledgerAccount.setCurrency(
                normalizedCurrency
        );

        ledgerAccount.setCreatedAt(
                now
        );

        ledgerAccountRepository.save(
                ledgerAccount
        );

        return savedWallet;
    }

    public Optional<Wallet> findByUserIdAndCurrency(
            UUID userId,
            String currency
    ) {

        validateUserId(userId);

        String normalizedCurrency =
                normalizeAndValidateCurrency(currency);

        return walletRepository
                .findByUserIdAndCurrency(
                        userId,
                        normalizedCurrency
                )
                .filter(wallet ->
                        "ACTIVE".equals(wallet.getStatus())
                );
    }

    public List<Wallet> findByUserId(
            UUID userId
    ) {

        validateUserId(userId);

        return walletRepository
                .findByUserId(userId)
                .stream()
                .filter(wallet ->
                        "ACTIVE".equals(wallet.getStatus())
                )
                .toList();
    }

    public BigDecimal calculateBalance(
            UUID walletId
    ) {

        if (walletId == null) {
            throw new IllegalArgumentException(
                    "Wallet ID is required"
            );
        }

        Wallet wallet =
                walletRepository.findById(walletId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Wallet not found"
                                )
                        );

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new IllegalArgumentException(
                    "Wallet is not active"
            );
        }

        LedgerAccount account =
                ledgerAccountRepository
                        .findByWalletId(walletId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ledger account not found for wallet"
                                )
                        );

        /*
         * Balance is derived from ledger entries.
         *
         * The ledger is the source of truth for
         * financial balance.
         */
        return ledgerEntryRepository.calculateBalance(
                account.getId()
        );
    }

    private String normalizeAndValidateCurrency(
            String currency
    ) {

        if (currency == null
                || currency.isBlank()) {

            throw new IllegalArgumentException(
                    "Currency is required"
            );
        }

        String normalizedCurrency =
                currency.trim().toUpperCase();

        if (normalizedCurrency.length() != 3) {

            throw new IllegalArgumentException(
                    "Currency must be a 3-letter ISO currency code"
            );
        }

        if (!SUPPORTED_CURRENCIES
                .contains(normalizedCurrency)) {

            throw new IllegalArgumentException(
                    "Unsupported currency: "
                            + normalizedCurrency
            );
        }

        return normalizedCurrency;
    }

    private void validateUserId(
            UUID userId
    ) {

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }
    }
}