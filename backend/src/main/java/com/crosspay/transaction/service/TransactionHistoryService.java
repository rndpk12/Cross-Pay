package com.crosspay.transaction.service;

import com.crosspay.transaction.dto.TransactionHistoryResponse;
import com.crosspay.transaction.dto.TransactionResponse;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionHistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;

    public TransactionHistoryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public TransactionHistoryResponse getTransactionHistory(
            UUID userId,
            int page,
            int size
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Transaction> transactions = transactionRepository
                .findTransactionHistoryByUserId(userId, pageable);

        return new TransactionHistoryResponse(
                transactions.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSenderUserId(),
                transaction.getRecipientUserId(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getCurrency(),
                transaction.getAmount(),
                transaction.getSourceCurrency(),
                transaction.getDestinationCurrency(),
                transaction.getSourceAmount(),
                transaction.getDestinationAmount(),
                transaction.getFxQuoteId(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
