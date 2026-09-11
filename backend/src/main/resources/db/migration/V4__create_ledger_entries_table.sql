CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    ledger_account_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference_type VARCHAR(30),
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_ledger_entry_account
        FOREIGN KEY (ledger_account_id)
        REFERENCES ledger_accounts(id),

    CONSTRAINT chk_ledger_entry_amount
        CHECK (amount > 0),

    CONSTRAINT chk_ledger_entry_type
        CHECK (entry_type IN ('CREDIT', 'DEBIT'))
);