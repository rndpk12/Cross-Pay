ALTER TABLE transactions
ADD COLUMN initiated_by_user_id UUID;

ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_initiated_by_user
FOREIGN KEY (initiated_by_user_id)
REFERENCES users(id);

UPDATE transactions
SET initiated_by_user_id = sender_user_id
WHERE sender_user_id IS NOT NULL;

CREATE INDEX idx_transactions_initiated_by_user
ON transactions(initiated_by_user_id);

CREATE UNIQUE INDEX uq_transactions_initiator_idempotency
ON transactions(initiated_by_user_id, idempotency_key)
WHERE initiated_by_user_id IS NOT NULL
  AND idempotency_key IS NOT NULL;