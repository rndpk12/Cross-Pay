ALTER TABLE transactions
ADD COLUMN amount NUMERIC(19, 4);

UPDATE transactions t
SET amount = (
    SELECT SUM(e.amount)
    FROM ledger_entries e
    WHERE e.transaction_id = t.id
      AND e.entry_type = 'DEBIT'
);

ALTER TABLE transactions
ALTER COLUMN amount SET NOT NULL;

ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_amount
CHECK (amount > 0);