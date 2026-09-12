-- Each history lookup filters by one ownership column and sorts newest first.
-- These indexes support PostgreSQL's ownership scans without reading all rows
-- for a user before applying the requested created_at DESC ordering.
CREATE INDEX idx_transactions_initiator_created_at
    ON transactions(initiated_by_user_id, created_at DESC);

CREATE INDEX idx_transactions_sender_created_at
    ON transactions(sender_user_id, created_at DESC);

CREATE INDEX idx_transactions_recipient_created_at
    ON transactions(recipient_user_id, created_at DESC);
