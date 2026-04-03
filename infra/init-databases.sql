CREATE DATABASE userdb;
CREATE DATABASE transactiondb;
CREATE DATABASE walletdb;
CREATE DATABASE analyticsdb;

-- ============================================================
-- WALLET DB — Budget tables (run after initial schema creation)
-- ============================================================
\c walletdb;

CREATE TABLE IF NOT EXISTS budgets (
    id              UUID PRIMARY KEY,
    wallet_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    category_id     UUID NOT NULL,
    budget_type     VARCHAR(20) NOT NULL,
    amount          NUMERIC(19,4) NOT NULL,
    period          VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_budget_wallet_category UNIQUE (wallet_id, category_id)
);

CREATE INDEX IF NOT EXISTS idx_budget_wallet_user ON budgets (wallet_id, user_id);

CREATE TABLE IF NOT EXISTS budget_spending (
    id              UUID PRIMARY KEY,
    wallet_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    category_id     UUID NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    total_spent     NUMERIC(19,4) NOT NULL DEFAULT 0,
    CONSTRAINT uq_spending_wallet_category_period UNIQUE (wallet_id, category_id, period_start)
);

CREATE INDEX IF NOT EXISTS idx_spending_wallet_period ON budget_spending (wallet_id, period_start, period_end);
