ALTER TABLE transactions
ADD COLUMN sender_user_id UUID;

ALTER TABLE transactions
ADD COLUMN recipient_user_id UUID;

ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_sender
    FOREIGN KEY (sender_user_id)
    REFERENCES users(id);

ALTER TABLE transactions
ADD CONSTRAINT fk_transaction_recipient
    FOREIGN KEY (recipient_user_id)
    REFERENCES users(id);

CREATE INDEX idx_transactions_sender
    ON transactions(sender_user_id);

CREATE INDEX idx_transactions_recipient
    ON transactions(recipient_user_id);