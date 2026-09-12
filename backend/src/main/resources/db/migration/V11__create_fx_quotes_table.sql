CREATE TABLE fx_quotes (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,

    source_amount NUMERIC(19, 4) NOT NULL,
    exchange_rate NUMERIC(19, 8) NOT NULL,
    converted_amount NUMERIC(19, 4) NOT NULL,

    fee_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_fx_quote_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT chk_fx_quote_currencies_different
        CHECK (from_currency <> to_currency),

    CONSTRAINT chk_fx_quote_source_amount
        CHECK (source_amount > 0),

    CONSTRAINT chk_fx_quote_rate
        CHECK (exchange_rate > 0),

    CONSTRAINT chk_fx_quote_converted_amount
        CHECK (converted_amount > 0),

    CONSTRAINT chk_fx_quote_fee
        CHECK (fee_amount >= 0),

    CONSTRAINT chk_fx_quote_status
        CHECK (
            status IN (
                'ACTIVE',
                'EXPIRED',
                'USED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_fx_quotes_user
    ON fx_quotes(user_id);

CREATE INDEX idx_fx_quotes_expires_at
    ON fx_quotes(expires_at);