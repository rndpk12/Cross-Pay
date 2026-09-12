package com.crosspay.fx.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "fx_rates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_fx_rate_currency_pair",
                        columnNames = {
                                "base_currency",
                                "quote_currency"
                        }
                )
        }
)
public class FxRate {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "base_currency",
            nullable = false,
            length = 3
    )
    private String baseCurrency;

    @Column(
            name = "quote_currency",
            nullable = false,
            length = 3
    )
    private String quoteCurrency;

    @Column(
            nullable = false,
            precision = 19,
            scale = 8
    )
    private BigDecimal rate;

    @Column(
            nullable = false,
            length = 50
    )
    private String source;

    @Column(
            nullable = false
    )
    private OffsetDateTime fetchedAt;

    @Column(
            nullable = false
    )
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(OffsetDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}