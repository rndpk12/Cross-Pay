ALTER TABLE ledger_entries
ADD COLUMN transaction_id UUID;

ALTER TABLE ledger_entries
ADD CONSTRAINT fk_ledger_entry_transaction
    FOREIGN KEY (transaction_id)
    REFERENCES transactions(id);

CREATE INDEX idx_ledger_entries_transaction_id
    ON ledger_entries(transaction_id);