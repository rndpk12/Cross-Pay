ALTER TABLE transactions
ADD COLUMN source_currency VARCHAR(3);

ALTER TABLE transactions
ADD COLUMN destination_currency VARCHAR(3);

ALTER TABLE transactions
ADD COLUMN source_amount NUMERIC(19, 4);

ALTER TABLE transactions
ADD COLUMN destination_amount NUMERIC(19, 4);

ALTER TABLE transactions
ADD COLUMN fx_quote_id UUID;

ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_fx_quote
    FOREIGN KEY (fx_quote_id)
    REFERENCES fx_quotes(id);

CREATE INDEX idx_transactions_fx_quote
    ON transactions(fx_quote_id);

ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_source_amount
    CHECK (source_amount IS NULL OR source_amount > 0);

ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_destination_amount
    CHECK (destination_amount IS NULL OR destination_amount > 0);
