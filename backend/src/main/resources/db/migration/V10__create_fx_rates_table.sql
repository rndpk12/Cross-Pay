CREATE TABLE fx_rates (
    id UUID PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    quote_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(19, 8) NOT NULL,
    source VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_fx_rate_currencies_different
        CHECK (base_currency <> quote_currency),

    CONSTRAINT chk_fx_rate_positive
        CHECK (rate > 0),

    CONSTRAINT uq_fx_rate_currency_pair
        UNIQUE (base_currency, quote_currency)
);