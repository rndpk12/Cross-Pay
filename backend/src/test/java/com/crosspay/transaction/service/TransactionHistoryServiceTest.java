package com.crosspay.transaction.service;

import com.crosspay.transaction.dto.TransactionHistoryResponse;
import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionHistoryService transactionHistoryService;

    @BeforeEach
    void setUp() {
        transactionHistoryService = new TransactionHistoryService(transactionRepository);
    }

    @Test
    void shouldReturnTransactionsForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        Transaction transaction = transaction(userId, null, null, "DEPOSIT", 1);
        mockHistory(userId, List.of(transaction), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals(1, response.content().size());
        assertEquals(transaction.getId(), response.content().getFirst().transactionId());
    }

    @Test
    void shouldIncludeDepositThroughInitiatorOwnership() {
        UUID userId = UUID.randomUUID();
        Transaction deposit = transaction(userId, null, null, "DEPOSIT", 1);
        mockHistory(userId, List.of(deposit), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals("DEPOSIT", response.content().getFirst().transactionType());
    }

    @Test
    void shouldIncludeWithdrawalThroughInitiatorOwnership() {
        UUID userId = UUID.randomUUID();
        Transaction withdrawal = transaction(userId, null, null, "WITHDRAWAL", 1);
        mockHistory(userId, List.of(withdrawal), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals("WITHDRAWAL", response.content().getFirst().transactionType());
    }

    @Test
    void shouldIncludeSentTransferThroughSenderOwnership() {
        UUID userId = UUID.randomUUID();
        Transaction transfer = transaction(userId, userId, UUID.randomUUID(), "TRANSFER", 1);
        mockHistory(userId, List.of(transfer), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals(userId, response.content().getFirst().senderUserId());
    }

    @Test
    void shouldIncludeReceivedTransferThroughRecipientOwnership() {
        UUID userId = UUID.randomUUID();
        Transaction transfer = transaction(UUID.randomUUID(), UUID.randomUUID(), userId,
                "TRANSFER", 1);
        mockHistory(userId, List.of(transfer), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals(userId, response.content().getFirst().recipientUserId());
    }

    @Test
    void shouldRequestNewestFirstSortingFromDatabase() {
        UUID userId = UUID.randomUUID();
        Transaction newest = transaction(userId, null, null, "DEPOSIT", 2);
        Transaction oldest = transaction(userId, null, null, "WITHDRAWAL", 1);
        mockHistory(userId, List.of(newest, oldest), 0, 20, 2);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findTransactionHistoryByUserId(eq(userId), pageableCaptor.capture());
        assertTrue(pageableCaptor.getValue().getSort().getOrderFor("createdAt").isDescending());
        assertEquals(newest.getId(), response.content().getFirst().transactionId());
    }

    @Test
    void shouldReturnRequestedPageMetadata() {
        UUID userId = UUID.randomUUID();
        Transaction transaction = transaction(userId, null, null, "DEPOSIT", 1);
        mockHistory(userId, List.of(transaction), 1, 1, 2);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 1, 1);

        assertEquals(1, response.page());
        assertEquals(1, response.size());
        assertEquals(2, response.totalElements());
        assertEquals(2, response.totalPages());
    }

    @Test
    void shouldRespectRequestedPageSize() {
        UUID userId = UUID.randomUUID();
        mockHistory(userId, List.of(), 0, 5, 0);

        transactionHistoryService.getTransactionHistory(userId, 0, 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findTransactionHistoryByUserId(eq(userId), pageableCaptor.capture());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldAllowMaximumPageSize() {
        UUID userId = UUID.randomUUID();
        mockHistory(userId, List.of(), 0, 100, 0);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 100);

        assertEquals(100, response.size());
    }

    @Test
    void shouldRejectPageSizeAboveMaximum() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionHistoryService.getTransactionHistory(UUID.randomUUID(), 0, 101)
        );
        assertEquals("Page size must be between 1 and 100", exception.getMessage());
        verify(transactionRepository, never()).findTransactionHistoryByUserId(any(), any());
    }

    @Test
    void shouldRejectNegativePage() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionHistoryService.getTransactionHistory(UUID.randomUUID(), -1, 20)
        );
        assertEquals("Page must not be negative", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidPageSize() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionHistoryService.getTransactionHistory(UUID.randomUUID(), 0, 0)
        );
        assertEquals("Page size must be between 1 and 100", exception.getMessage());
    }

    @Test
    void shouldNotExposeAnotherUsersTransactions() {
        UUID authenticatedUserId = UUID.randomUUID();
        mockHistory(authenticatedUserId, List.of(), 0, 20, 0);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(authenticatedUserId, 0, 20);

        assertTrue(response.content().isEmpty());
        verify(transactionRepository).findTransactionHistoryByUserId(eq(authenticatedUserId), any());
    }

    @Test
    void shouldNotIncludeTransactionsBelongingToNeitherUser() {
        UUID authenticatedUserId = UUID.randomUUID();
        mockHistory(authenticatedUserId, List.of(), 0, 20, 0);

        assertTrue(transactionHistoryService
                .getTransactionHistory(authenticatedUserId, 0, 20)
                .content().isEmpty());
    }

    @Test
    void shouldMapFxTransactionFields() {
        UUID userId = UUID.randomUUID();
        Transaction transfer = transaction(userId, userId, UUID.randomUUID(), "TRANSFER", 1);
        transfer.setSourceCurrency("USD");
        transfer.setDestinationCurrency("EUR");
        transfer.setSourceAmount(new BigDecimal("100.00"));
        transfer.setDestinationAmount(new BigDecimal("84.25"));
        UUID quoteId = UUID.randomUUID();
        transfer.setFxQuoteId(quoteId);
        mockHistory(userId, List.of(transfer), 0, 20, 1);

        var response = transactionHistoryService.getTransactionHistory(userId, 0, 20)
                .content().getFirst();

        assertEquals("USD", response.sourceCurrency());
        assertEquals("EUR", response.destinationCurrency());
        assertEquals(quoteId, response.fxQuoteId());
    }

    @Test
    void shouldNotDuplicateTransactionMatchingMultipleOwnershipConditions() {
        UUID userId = UUID.randomUUID();
        Transaction selfOwnedTransfer = transaction(userId, userId, userId, "TRANSFER", 1);
        mockHistory(userId, List.of(selfOwnedTransfer), 0, 20, 1);

        TransactionHistoryResponse response = transactionHistoryService
                .getTransactionHistory(userId, 0, 20);

        assertEquals(1, response.content().size());
    }

    private void mockHistory(
            UUID userId,
            List<Transaction> content,
            int page,
            int size,
            long totalElements
    ) {
        when(transactionRepository.findTransactionHistoryByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(page, size), totalElements));
    }

    private Transaction transaction(
            UUID initiatorUserId,
            UUID senderUserId,
            UUID recipientUserId,
            String type,
            int createdAtOffset
    ) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setInitiatedByUserId(initiatorUserId);
        transaction.setSenderUserId(senderUserId);
        transaction.setRecipientUserId(recipientUserId);
        transaction.setTransactionType(type);
        transaction.setStatus("COMPLETED");
        transaction.setCurrency("USD");
        transaction.setAmount(BigDecimal.ONE);
        transaction.setCreatedAt(OffsetDateTime.now().plusMinutes(createdAtOffset));
        transaction.setCompletedAt(OffsetDateTime.now().plusMinutes(createdAtOffset));
        return transaction;
    }
}
