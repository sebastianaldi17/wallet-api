-- =========================================================
-- Ledger / Wallet API - PostgreSQL init script
-- =========================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- for gen_random_uuid()

-- ---------------------------------------------------------
-- Enums
-- ---------------------------------------------------------
CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');
CREATE TYPE account_type AS ENUM ('SYSTEM', 'USER');
CREATE TYPE transaction_type AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER');

-- ---------------------------------------------------------
-- Generic updated_at trigger function
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------
-- users
-- ---------------------------------------------------------
CREATE TABLE users (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    description text NOT NULL,
    role        user_role NOT NULL DEFAULT 'USER',
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------
-- accounts
-- ---------------------------------------------------------
CREATE TABLE accounts (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id  uuid REFERENCES users(id), -- NULL allowed for SYSTEM accounts (e.g. clearing account)
    account_type   account_type NOT NULL DEFAULT 'USER',
    created_at     timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT chk_account_owner CHECK (
        (account_type = 'USER' AND owner_user_id IS NOT NULL) OR
        (account_type = 'SYSTEM' AND owner_user_id IS NULL)
    )
);

CREATE INDEX idx_accounts_owner_user_id ON accounts(owner_user_id);

-- ---------------------------------------------------------
-- user_api_keys
-- ---------------------------------------------------------
CREATE TABLE user_api_keys (
    id          bigserial PRIMARY KEY,
    user_id     uuid NOT NULL REFERENCES users(id),
    api_key     text NOT NULL UNIQUE,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_api_keys_user_id ON user_api_keys(user_id);
CREATE INDEX idx_user_api_keys_api_key ON user_api_keys(api_key);

CREATE TRIGGER trg_user_api_keys_updated_at
BEFORE UPDATE ON user_api_keys
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------
-- balances
-- ---------------------------------------------------------
CREATE TABLE balances (
    id          bigserial PRIMARY KEY,
    account_id  uuid NOT NULL UNIQUE REFERENCES accounts(id),
    available   numeric(20, 8) NOT NULL DEFAULT 0,
    locked      numeric(20, 8) NOT NULL DEFAULT 0,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT chk_balances_non_negative CHECK (available >= 0 AND locked >= 0)
);

CREATE TRIGGER trg_balances_updated_at
BEFORE UPDATE ON balances
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------
-- transactions
-- ---------------------------------------------------------
CREATE TABLE transactions (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       uuid NOT NULL REFERENCES accounts(id),
    idempotency_key  text NOT NULL,
    type             transaction_type NOT NULL,
    description      text,
    amount           numeric(20, 8) NOT NULL CHECK (amount > 0),
    created_at       timestamptz NOT NULL DEFAULT now(),

    UNIQUE (account_id, idempotency_key)
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- ---------------------------------------------------------
-- ledger
-- ---------------------------------------------------------
CREATE TABLE ledger (
    id              bigserial PRIMARY KEY,
    transaction_id  uuid NOT NULL REFERENCES transactions(id),
    account_id      uuid NOT NULL REFERENCES accounts(id),
    description     text,
    credit          numeric(20, 8) NOT NULL DEFAULT 0 CHECK (credit >= 0),
    debit           numeric(20, 8) NOT NULL DEFAULT 0 CHECK (debit >= 0),
    created_at      timestamptz NOT NULL DEFAULT now(),

    -- exactly one of credit/debit should be non-zero per row
    CONSTRAINT chk_ledger_single_side CHECK (
        (credit > 0 AND debit = 0) OR (debit > 0 AND credit = 0)
    )
);

CREATE INDEX idx_ledger_transaction_id ON ledger(transaction_id);
CREATE INDEX idx_ledger_account_id ON ledger(account_id);
CREATE INDEX idx_ledger_account_created_at ON ledger(account_id, created_at DESC);

-- ---------------------------------------------------------
-- reconciliation_discrepancies (output of the reconciliation job)
-- ---------------------------------------------------------
CREATE TABLE reconciliation_discrepancies (
    id                  bigserial PRIMARY KEY,
    account_id          uuid NOT NULL REFERENCES accounts(id),
    stored_balance      numeric(20, 8) NOT NULL,
    derived_balance     numeric(20, 8) NOT NULL,
    detected_at         timestamptz NOT NULL DEFAULT now(),
    resolved_at         timestamptz
);

CREATE INDEX idx_reconciliation_account_id ON reconciliation_discrepancies(account_id);
CREATE INDEX idx_reconciliation_unresolved ON reconciliation_discrepancies(account_id) WHERE resolved_at IS NULL;

-- ---------------------------------------------------------
-- Seed: system clearing account
-- ---------------------------------------------------------
INSERT INTO accounts (id, owner_user_id, account_type)
VALUES ('00000000-0000-0000-0000-000000000001', NULL, 'SYSTEM');

INSERT INTO balances (account_id, available, locked)
VALUES ('00000000-0000-0000-0000-000000000001', 0, 0);