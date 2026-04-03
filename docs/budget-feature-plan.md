
# Budget Feature — Implementation Plan

> Per-wallet, per-category budget allocation (e.g., 50/30/20 rule)

---

## Overview

Allow users to allocate their income across categories within each wallet. For example, with a $10,000 monthly income:
- **50%** → Needs (rent, groceries, utilities)
- **30%** → Personal spending (entertainment, dining out)
- **20%** → Savings (emergency fund, investments)

Budgets live in **wallet-service** — no new microservice needed. Endpoints are nested under `/api/v1/wallets/{walletId}/budgets`, so **no API Gateway, Kubernetes, or Docker changes are required**.

---

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Where to put the budget domain? | `wallet-service` | Budgets are scoped per-wallet; keeps the domain close to the owning aggregate |
| How to resolve category names? | Client-side join | Frontend already calls `CategoryApiService.getAll()` from transaction-service; avoids sync REST between backends |
| Budget period | `MONTHLY` only (initially) | Matches common budgeting models (50/30/20); can extend to `WEEKLY`/`YEARLY` later |
| Income storage | New optional `monthlyIncome` field on `Wallet` | Enables richer budget analysis and percentage calculations server-side |
| Spending tracking | Event-sourced via RabbitMQ | The transaction-service already publishes `categoryId` in events; wallet-service currently ignores it |

---

## Step 1 — Extend wallet-service Event DTOs

The transaction-service **already publishes** `categoryId`, `subCategoryId`, and `transactionDate` in both `TransactionCreatedEvent` and `TransactionDeletedEvent`. However, the wallet-service copies of these DTOs are missing those fields — they only have `transactionId`, `userId`, `walletId`, `type`, `amount`, `currency`.

### Files to modify

| File | Change |
|---|---|
| `wallet-service/.../messaging/event/TransactionCreatedEvent.java` | Add `categoryId` (UUID), `subCategoryId` (UUID), `transactionDate` (OffsetDateTime) fields + getters/setters |
| `wallet-service/.../messaging/event/TransactionDeletedEvent.java` | Same additions |

### Checklist
- [x] Add `categoryId`, `subCategoryId`, `transactionDate` to `TransactionCreatedEvent` (wallet-service copy)
- [x] Add `categoryId`, `subCategoryId`, `transactionDate` to `TransactionDeletedEvent` (wallet-service copy)
- [x] Verify JSON deserialization works with the enriched payload

---

## Step 2 — Budget Domain Model (wallet-service)

Create the budget domain following the same immutable model pattern used by `Wallet` and `SpendingLimit`.

### New files

```
wallet-service/src/main/java/com/personalfinance/walletservice/
├── domain/
│   ├── model/
│   │   ├── Budget.java              ← immutable domain entity
│   │   ├── BudgetType.java          ← enum: FIXED, PERCENTAGE
│   │   └── BudgetPeriod.java        ← enum: MONTHLY (extensible)
│   ├── port/
│   │   └── BudgetRepository.java    ← repository interface (port)
│   └── exception/
│       ├── BudgetNotFoundException.java
│       └── BudgetLimitExceededException.java
```

### `Budget.java` — Key fields

| Field | Type | Description |
|---|---|---|
| `id` | `UUID` | Primary key |
| `walletId` | `UUID` | FK to wallet |
| `userId` | `UUID` | Owner |
| `categoryId` | `UUID` | References a category in transaction-service |
| `budgetType` | `BudgetType` | `FIXED` (absolute $) or `PERCENTAGE` (of income) |
| `amount` | `BigDecimal` | The budget amount or percentage value |
| `period` | `BudgetPeriod` | `MONTHLY` |
| `createdAt` | `OffsetDateTime` | |
| `updatedAt` | `OffsetDateTime` | |

### `BudgetRepository.java` — Port methods

```java
save(Budget budget): Budget
findById(UUID id): Optional<Budget>
findByWalletIdAndUserId(UUID walletId, UUID userId): List<Budget>
findByWalletIdAndCategoryId(UUID walletId, UUID categoryId): Optional<Budget>
deleteById(UUID id): void
deleteAllByWalletId(UUID walletId): void
```

### Wallet income field (optional)

Add an optional `monthlyIncome` (`BigDecimal`) field to the existing `Wallet` domain model for percentage-based budget calculations.

### Checklist
- [x] Create `BudgetType` enum
- [x] Create `BudgetPeriod` enum
- [x] Create `Budget` domain model (immutable, builder pattern)
- [x] Create `BudgetRepository` port interface
- [x] Create `BudgetNotFoundException`
- [x] Create `BudgetLimitExceededException`
- [x] Add `monthlyIncome` field to `Wallet` (optional, nullable)

---

## Step 3 — Budget Persistence Layer (wallet-service)

### New files

```
wallet-service/src/main/java/com/personalfinance/walletservice/
├── infrastructure/
│   └── persistence/
│       ├── entity/
│       │   └── BudgetJpaEntity.java
│       ├── repository/
│       │   └── BudgetJpaRepository.java
│       ├── mapper/
│       │   └── BudgetJpaMapper.java
│       └── adapter/
│           └── BudgetRepositoryAdapter.java
```

### `budgets` table schema

```sql
CREATE TABLE budgets (
    id              UUID PRIMARY KEY,
    wallet_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    category_id     UUID NOT NULL,
    budget_type     VARCHAR(20) NOT NULL,     -- 'FIXED' or 'PERCENTAGE'
    amount          NUMERIC(19,4) NOT NULL,
    period          VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    UNIQUE (wallet_id, category_id)
);

CREATE INDEX idx_budget_wallet_user ON budgets (wallet_id, user_id);
```

### Files to modify

| File | Change |
|---|---|
| `infra/init-databases.sql` | Add `budgets` and `budget_spending` table creation under `walletdb` |
| `WalletJpaEntity.java` | Add `monthlyIncome` column (if income field is added to Wallet) |

### Checklist
- [x] Create `BudgetJpaEntity` with table `budgets` and unique constraint `(wallet_id, category_id)`
- [x] Create `BudgetJpaRepository` (Spring Data JPA)
- [x] Create `BudgetJpaMapper` (domain ↔ JPA entity mapping)
- [x] Create `BudgetRepositoryAdapter` implementing `BudgetRepository` port
- [x] Update `infra/init-databases.sql` with new tables
- [x] Update `WalletJpaEntity` if adding `monthlyIncome`

---

## Step 4 — Spending Tracking Entity & RabbitMQ Integration

Track actual spending per category per period to compare against budgets.

### New files

```
wallet-service/src/main/java/com/personalfinance/walletservice/
├── domain/
│   ├── model/
│   │   └── BudgetSpending.java
│   └── port/
│       └── BudgetSpendingRepository.java
├── infrastructure/
│   └── persistence/
│       ├── entity/
│       │   └── BudgetSpendingJpaEntity.java
│       ├── repository/
│       │   └── BudgetSpendingJpaRepository.java
│       ├── mapper/
│       │   └── BudgetSpendingJpaMapper.java
│       └── adapter/
│           └── BudgetSpendingRepositoryAdapter.java
```

### `budget_spending` table schema

```sql
CREATE TABLE budget_spending (
    id              UUID PRIMARY KEY,
    wallet_id       UUID NOT NULL,
    user_id         UUID NOT NULL,
    category_id     UUID NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    total_spent     NUMERIC(19,4) NOT NULL DEFAULT 0,
    UNIQUE (wallet_id, category_id, period_start)
);

CREATE INDEX idx_spending_wallet_period ON budget_spending (wallet_id, period_start, period_end);
```

### Files to modify

| File | Change |
|---|---|
| `TransactionEventListener.java` | In `handleTransactionCreated`: if type is `EXPENSE`, upsert `BudgetSpending` row for `(walletId, categoryId, currentMonthStart)` incrementing `totalSpent` |
| | In `handleTransactionDeleted`: decrement `totalSpent` for the matching row |

### Event handling logic (pseudocode)

```
on TransactionCreatedEvent:
    1. adjustBalance(...)            ← existing logic
    2. if type == EXPENSE:
        upsert BudgetSpending(walletId, categoryId, monthStart)
            totalSpent += event.amount

on TransactionDeletedEvent:
    1. adjustBalance(...)            ← existing logic (reversed)
    2. if type == EXPENSE:
        upsert BudgetSpending(walletId, categoryId, monthStart)
            totalSpent -= event.amount
```

### Checklist
- [x] Create `BudgetSpending` domain model
- [x] Create `BudgetSpendingRepository` port
- [x] Create JPA entity, repository, mapper, and adapter for `BudgetSpending`
- [x] Update `TransactionEventListener.handleTransactionCreated` to track spending
- [x] Update `TransactionEventListener.handleTransactionDeleted` to reverse spending
- [x] Add `budget_spending` table to `infra/init-databases.sql`

---

## Step 5 — Budget Application Layer (wallet-service)

### New files

```
wallet-service/src/main/java/com/personalfinance/walletservice/
├── application/
│   ├── dto/
│   │   ├── CreateBudgetCommand.java
│   │   ├── UpdateBudgetCommand.java
│   │   ├── BulkBudgetCommand.java
│   │   ├── BudgetDto.java
│   │   └── BudgetSummaryDto.java
│   ├── usecase/
│   │   └── BudgetUseCase.java
│   └── service/
│       └── BudgetApplicationService.java
```

### DTOs

| DTO | Fields |
|---|---|
| `CreateBudgetCommand` | `categoryId`, `budgetType`, `amount` |
| `UpdateBudgetCommand` | `budgetType`, `amount` |
| `BulkBudgetCommand` | `monthlyIncome`, `List<BudgetAllocation>` where each has `categoryId`, `budgetType`, `amount` |
| `BudgetDto` | `id`, `walletId`, `userId`, `categoryId`, `budgetType`, `amount`, `period`, `createdAt`, `updatedAt` |
| `BudgetSummaryDto` | extends `BudgetDto` + `spentAmount`, `remainingAmount`, `percentUsed` |

### `BudgetUseCase` interface

```java
BudgetDto create(UUID walletId, UUID userId, CreateBudgetCommand command);
BudgetDto update(UUID budgetId, UUID userId, UpdateBudgetCommand command);
void delete(UUID budgetId, UUID userId);
List<BudgetSummaryDto> listByWallet(UUID walletId, UUID userId);
BudgetSummaryDto getByWalletAndCategory(UUID walletId, UUID categoryId, UUID userId);
List<BudgetDto> setBulkBudgets(UUID walletId, UUID userId, BulkBudgetCommand command);
```

### `BudgetApplicationService` — Validation rules

1. **Unique constraint**: Only one budget per `(walletId, categoryId)`.
2. **Percentage cap**: Total of all `PERCENTAGE` budgets in a wallet must not exceed 100%.
3. **Ownership**: Budget wallet must belong to `userId`.
4. **Wallet state**: Cannot create budgets on archived wallets.
5. **Positive amounts**: Budget amount must be > 0; percentage must be between 0 and 100.

### Checklist
- [x] Create `CreateBudgetCommand` record
- [x] Create `UpdateBudgetCommand` record
- [x] Create `BulkBudgetCommand` record
- [x] Create `BudgetDto` record
- [x] Create `BudgetSummaryDto` record
- [x] Create `BudgetUseCase` interface
- [x] Create `BudgetApplicationService` with all validation rules
- [x] Wire `BudgetSpendingRepository` into the service for summary calculations

---

## Step 6 — Budget REST API (wallet-service)

### New files

```
wallet-service/src/main/java/com/personalfinance/walletservice/
├── presentation/
│   ├── controller/
│   │   └── BudgetController.java
│   ├── request/
│   │   ├── CreateBudgetRequest.java
│   │   ├── UpdateBudgetRequest.java
│   │   └── BulkBudgetRequest.java
│   └── response/
│       ├── BudgetResponse.java
│       └── BudgetSummaryResponse.java
```

### Endpoints

| Method | Path | Description | Request Body |
|---|---|---|---|
| `POST` | `/api/v1/wallets/{walletId}/budgets` | Create a budget for a category | `CreateBudgetRequest` |
| `GET` | `/api/v1/wallets/{walletId}/budgets` | List all budgets with spending summary | — |
| `GET` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Get single budget with summary | — |
| `PUT` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Update a budget | `UpdateBudgetRequest` |
| `DELETE` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Delete a budget | — |
| `PUT` | `/api/v1/wallets/{walletId}/budgets/bulk` | Set/replace all budgets at once | `BulkBudgetRequest` |

### Request/Response shapes

```json
// CreateBudgetRequest
{
    "categoryId": "uuid",
    "budgetType": "FIXED | PERCENTAGE",
    "amount": 5000.00
}

// BulkBudgetRequest (allocation wizard — e.g., 50/30/20)
{
    "monthlyIncome": 10000.00,
    "allocations": [
        { "categoryId": "uuid-needs", "budgetType": "PERCENTAGE", "amount": 50 },
        { "categoryId": "uuid-personal", "budgetType": "PERCENTAGE", "amount": 30 },
        { "categoryId": "uuid-savings", "budgetType": "PERCENTAGE", "amount": 20 }
    ]
}

// BudgetSummaryResponse
{
    "id": "uuid",
    "walletId": "uuid",
    "categoryId": "uuid",
    "budgetType": "PERCENTAGE",
    "amount": 50.00,
    "resolvedAmount": 5000.00,     // 50% of 10,000 income
    "period": "MONTHLY",
    "spentAmount": 3200.00,
    "remainingAmount": 1800.00,
    "percentUsed": 64.0,
    "createdAt": "...",
    "updatedAt": "..."
}
```

### Files to modify

| File | Change |
|---|---|
| `GlobalExceptionHandler.java` | Add handlers for `BudgetNotFoundException`, `BudgetLimitExceededException` |

### Checklist
- [x] Create `CreateBudgetRequest` with Jakarta validation annotations
- [x] Create `UpdateBudgetRequest`
- [x] Create `BulkBudgetRequest`
- [x] Create `BudgetResponse`
- [x] Create `BudgetSummaryResponse`
- [x] Create `BudgetController` with all 6 endpoints
- [x] Update `GlobalExceptionHandler` with new exception mappings
- [ ] Test all endpoints with Postman/curl

---

## Step 7 — Frontend: Models & API Service

### New files

```
personal-finance-frontend/src/app/
├── core/
│   ├── models/
│   │   └── budget.models.ts
│   └── services/
│       ├── budget-api.service.ts
│       └── budget-api.service.spec.ts
```

### `budget.models.ts`

```typescript
export enum BudgetType { FIXED = 'FIXED', PERCENTAGE = 'PERCENTAGE' }

export interface BudgetResponse {
    id: string;
    walletId: string;
    categoryId: string;
    budgetType: BudgetType;
    amount: number;
    resolvedAmount: number;
    period: string;
    spentAmount: number;
    remainingAmount: number;
    percentUsed: number;
    createdAt: string;
    updatedAt: string;
}

export interface CreateBudgetRequest {
    categoryId: string;
    budgetType: BudgetType;
    amount: number;
}

export interface BulkBudgetRequest {
    monthlyIncome: number;
    allocations: CreateBudgetRequest[];
}
```

### `BudgetApiService` methods

| Method | HTTP | Path |
|---|---|---|
| `create(walletId, request)` | `POST` | `/api/v1/wallets/{walletId}/budgets` |
| `listByWallet(walletId)` | `GET` | `/api/v1/wallets/{walletId}/budgets` |
| `getById(walletId, budgetId)` | `GET` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` |
| `update(walletId, budgetId, request)` | `PUT` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` |
| `delete(walletId, budgetId)` | `DELETE` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` |
| `setBulk(walletId, request)` | `PUT` | `/api/v1/wallets/{walletId}/budgets/bulk` |

### Checklist
- [x] Create `budget.models.ts`
- [x] Create `BudgetApiService`
- [ ] Create `budget-api.service.spec.ts`

---

## Step 8 — Frontend: Budget Components

### New files

```
personal-finance-frontend/src/app/features/wallets/
├── budget-list/
│   ├── budget-list.component.ts
│   ├── budget-list.component.html
│   ├── budget-list.component.css
│   └── budget-list.component.spec.ts
├── budget-form-dialog/
│   ├── budget-form-dialog.component.ts
│   ├── budget-form-dialog.component.html
│   ├── budget-form-dialog.component.css
│   └── budget-form-dialog.component.spec.ts
└── budget-allocation-wizard/          ← optional
    ├── budget-allocation-wizard.component.ts
    ├── budget-allocation-wizard.component.html
    ├── budget-allocation-wizard.component.css
    └── budget-allocation-wizard.component.spec.ts
```

### `BudgetListComponent`

- Receives `walletId` as input
- Fetches budgets via `BudgetApiService.listByWallet()`
- Fetches categories via `CategoryApiService.getAll()` to resolve category names
- Renders each budget as a card with:
  - Category name + icon
  - Progress bar: `spentAmount / resolvedAmount` (color-coded: green < 70%, yellow 70-90%, red > 90%)
  - Text: "$3,200 / $5,000 spent (64%)"
  - Edit / Delete actions
- "Add Budget" button opens `BudgetFormDialogComponent`
- "Allocation Wizard" button opens `BudgetAllocationWizardComponent`

### `BudgetFormDialogComponent`

- Follows the `SpendingLimitDialogComponent` dialog pattern
- Uses shared `ModalComponent`, `FormFieldComponent`, `ButtonComponent`
- Form fields:
  - Category dropdown (filtered to categories without existing budgets)
  - Budget type toggle: Fixed / Percentage
  - Amount input (shows `$` prefix for FIXED, `%` suffix for PERCENTAGE)
- Emits `budgetCreated` or `budgetUpdated` event on save

### `BudgetAllocationWizardComponent` (optional, nice-to-have)

- Full-screen dialog for the 50/30/20 rule setup
- Step 1: Enter monthly income
- Step 2: For each category, set a percentage — a running total bar shows remaining %
- Step 3: Review & confirm → calls `PUT .../budgets/bulk`
- Preset templates: "50/30/20 Rule", "70/20/10", "Custom"

### Files to modify

| File | Change |
|---|---|
| `wallet-detail.component.ts` | Add a "Budgets" section between Spending Limit and Transactions |
| `wallet-detail.component.html` | Embed `<app-budget-list [walletId]="wallet.id">` |
| `wallets.routes.ts` | No new routes needed (budgets render inline in wallet detail) |

### Checklist
- [x] Create `BudgetListComponent` with progress bars
- [x] Create `BudgetFormDialogComponent`
- [x] Create `BudgetAllocationWizardComponent` (optional)
- [x] Integrate budget list into `WalletDetailComponent`
- [x] Style with Tailwind CSS (dark mode support)

---

## Step 9 — Database Migration

### `infra/init-databases.sql` additions

```sql
-- ============================================================
-- WALLET DB — Budget tables
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
```

### Checklist
- [x] Add budget tables to `infra/init-databases.sql`
- [x] Verify JPA auto-DDL handles the schema in dev (`ddl-auto=update`)

---

## Step 10 — Testing

### Backend — Unit Tests

| Test Class | What to Test |
|---|---|
| `BudgetApplicationServiceTest` | CRUD operations, percentage validation (sum ≤ 100%), unique constraint per `(wallet, category)`, ownership checks, archived wallet rejection |
| `TransactionEventListenerTest` | `handleTransactionCreated` now upserts `BudgetSpending` for EXPENSE types; `handleTransactionDeleted` decrements correctly |
| `BudgetJpaMapperTest` | Domain ↔ JPA entity mapping correctness |

### Backend — Integration Tests

| Test Class | What to Test |
|---|---|
| `BudgetControllerIntegrationTest` | All 6 REST endpoints with `@SpringBootTest` + TestContainers (follow `WalletControllerIntegrationTest` pattern) |

### Frontend — Unit Tests

| Test File | What to Test |
|---|---|
| `budget-api.service.spec.ts` | All HTTP methods with mocked `HttpClient` |
| `budget-list.component.spec.ts` | Renders budgets, progress bars, handles empty state |
| `budget-form-dialog.component.spec.ts` | Form validation, type toggle, emit on save |

### Checklist
- [x] Create `BudgetApplicationServiceTest`
- [ ] Update `TransactionEventListenerTest`
- [ ] Create `BudgetControllerIntegrationTest`
- [x] Create frontend spec files (`budget-api.service.spec.ts`)
- [ ] Verify ≥80% coverage on services, ≥70% on components

---

## Summary — New File Count

| Layer | New Files | Modified Files |
|---|---|---|
| **Domain model** | 5 (`Budget`, `BudgetType`, `BudgetPeriod`, `BudgetSpending`, exceptions) | 1 (`Wallet.java` — optional income field) |
| **Domain ports** | 2 (`BudgetRepository`, `BudgetSpendingRepository`) | — |
| **Persistence** | 8 (entity, repo, mapper, adapter × 2) | 1 (`WalletJpaEntity` — optional income) |
| **Application** | 7 (DTOs, use case, service) | — |
| **Presentation** | 6 (controller, requests, responses) | 1 (`GlobalExceptionHandler`) |
| **Messaging** | — | 3 (2 event DTOs + `TransactionEventListener`) |
| **Frontend models** | 1 | — |
| **Frontend services** | 2 (service + spec) | — |
| **Frontend components** | 8–12 (3–4 components × ts/html/css/spec) | 2 (`wallet-detail` ts + html) |
| **Infrastructure** | — | 1 (`init-databases.sql`) |
| **Tests** | 3–4 backend + 3 frontend | 1 (`TransactionEventListenerTest`) |
| **Total** | **~35–40 new files** | **~10 modified files** |

---

## No Changes Needed

- ✅ **API Gateway** — budget routes are under `/api/v1/wallets/**` which is already proxied
- ✅ **Kubernetes manifests** — no new services, configmaps, or deployments
- ✅ **Docker Compose** — wallet-service container stays the same
- ✅ **transaction-service** — already publishes `categoryId` in events, no changes needed

