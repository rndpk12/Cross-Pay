ALTER TABLE transactions
ADD COLUMN idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX uq_transactions_sender_idempotency
ON transactions(sender_user_id, idempotency_key)
WHERE idempotency_key IS NOT NULL;