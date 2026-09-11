package com.crosspay.wallet.repository;

import com.crosspay.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserIdAndCurrency(
            UUID userId,
            String currency
    );

    List<Wallet> findByUserId(UUID userId);
}