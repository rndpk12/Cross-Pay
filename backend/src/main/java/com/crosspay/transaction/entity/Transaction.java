package com.crosspay.transaction.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "sender_user_id")
    private UUID senderUserId;

    @Column(name = "recipient_user_id")
    private UUID recipientUserId;

    @Column(name = "initiated_by_user_id")
    private UUID initiatedByUserId;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "source_currency", length = 3)
    private String sourceCurrency;

    @Column(name = "destination_currency", length = 3)
    private String destinationCurrency;

    @Column(name = "source_amount", precision = 19, scale = 4)
    private BigDecimal sourceAmount;

    @Column(name = "destination_amount", precision = 19, scale = 4)
    private BigDecimal destinationAmount;

    @Column(name = "fx_quote_id")
    private UUID fxQuoteId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public Transaction() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderUserId() {
        return senderUserId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public UUID getInitiatedByUserId() {
        return initiatedByUserId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public BigDecimal getDestinationAmount() {
        return destinationAmount;
    }

    public UUID getFxQuoteId() {
        return fxQuoteId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSenderUserId(UUID senderUserId) {
        this.senderUserId = senderUserId;
    }

    public void setRecipientUserId(UUID recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public void setInitiatedByUserId(UUID initiatedByUserId) {
        this.initiatedByUserId = initiatedByUserId;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public void setDestinationAmount(BigDecimal destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public void setFxQuoteId(UUID fxQuoteId) {
        this.fxQuoteId = fxQuoteId;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}