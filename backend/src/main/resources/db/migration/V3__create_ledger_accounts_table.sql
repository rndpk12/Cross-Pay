CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_ledger_account_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT uq_ledger_account_wallet
        UNIQUE (wallet_id)
);