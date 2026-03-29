
# Personal Finance Frontend — Implementation Plan

> Angular 21 · Tailwind CSS 4 · SOLID Principles · Light & Dark Themes

---

## 1. Folder Structure & Architecture

```
src/app/
├── core/
│   ├── interceptors/        (auth-token, error-handling, api-base-url)
│   ├── guards/              (auth, guest, role)
│   ├── services/            (auth-state, theme, token-storage)
│   ├── models/              (TypeScript interfaces mirroring backend DTOs)
│   ├── abstractions/        (abstract BaseApiService — Dependency Inversion)
│   └── layout/              (shell, sidebar, header, mobile-nav)
├── features/
│   ├── auth/                (login, register)
│   ├── dashboard/           (overview with analytics widgets)
│   ├── wallets/             (list, detail, create/edit, archive, spending-limits)
│   ├── transactions/        (list, create/edit, filterable table)
│   ├── categories/          (list, create, subcategory tree)
│   ├── analytics/           (monthly, trends, category/wallet breakdowns)
│   ├── profile/             (view/edit profile, change password)
│   └── admin/               (user management, roles, delete)
├── shared/
│   ├── components/          (button, card, modal, pagination, toast, spinner, ...)
│   ├── directives/          (click-outside, autofocus)
│   ├── pipes/               (currency-format, date-relative, truncate)
│   └── utils/               (date helpers, form validators)
├── app.ts / app.html / app.css
├── app.config.ts
└── app.routes.ts
```

### SOLID Mapping

| Principle | How It's Applied |
|---|---|
| **Single Responsibility** | Each feature service handles one domain; each component renders one concern |
| **Open/Closed** | Shared components accept config via `input()` signals — extensible without modification |
| **Liskov Substitution** | Abstract `BaseApiService<T>` in `core/abstractions/` — concrete services extend it interchangeably |
| **Interface Segregation** | Small focused TypeScript interfaces per domain (not one monolith) |
| **Dependency Inversion** | Components depend on abstract service interfaces via `InjectionToken`, not concrete HTTP implementations |

---

## 2. Theming — Light & Dark Mode

- Define a `@theme` block in `styles.css` using **Tailwind CSS 4** native theming with `@custom-variant dark (&:where(.dark, .dark *))`.
- **CSS custom properties** for both palettes under `:root` (light) and `.dark` (dark):
    - `--color-bg-primary`, `--color-bg-secondary`, `--color-bg-card`
    - `--color-text-primary`, `--color-text-muted`
    - `--color-accent`, `--color-success`, `--color-danger`, `--color-warning`, `--color-border`
- **`ThemeService`** (`core/services/`): Angular `signal()` holding `'light' | 'dark'`, toggles `.dark` on `document.documentElement`, persists to `localStorage`.
- **`ThemeToggleComponent`**: Sun/moon icon button in the header calling `ThemeService.toggle()`.
- All Tailwind usage follows `dark:` variant pattern (e.g., `bg-white dark:bg-gray-900`).

---

## 3. TypeScript Models (`core/models/`)

| File | Interfaces |
|---|---|
| `auth.models.ts` | `LoginRequest`, `LoginResponse` (`accessToken`, `refreshToken`, `expiresInSeconds`), `RegisterRequest`, `RefreshRequest` |
| `user.models.ts` | `UserResponse`, `UserPageResponse`, `UpdateProfileRequest`, `ChangePasswordRequest`, `UpdateRolesRequest` |
| `wallet.models.ts` | `WalletResponse`, `WalletPageResponse`, `CreateWalletRequest`, `UpdateWalletRequest`, `SpendingLimitRequest`, `WalletTotalsResponse`, `LimitPeriod` enum |
| `transaction.models.ts` | `TransactionResponse`, `TransactionPageResponse`, `CreateTransactionRequest`, `UpdateTransactionRequest`, `TransactionType` enum |
| `category.models.ts` | `CategoryResponse`, `CreateCategoryRequest` |
| `analytics.models.ts` | `MonthlyAnalyticsResponse`, `CategoryAnalyticsResponse`, `SavingsRateResponse`, `TrendResponse`, `WalletBreakdownResponse` |
| `pagination.models.ts` | Generic `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`) |

---

## 4. Services & State Management

### Core Services (`core/services/`)

| Service | Responsibility |
|---|---|
| `TokenStorageService` | Read/write tokens to `localStorage`; expose `accessToken()` signal |
| `AuthStateService` | Holds `currentUser` signal, `isAuthenticated` computed, `isAdmin` computed; login/logout/refresh orchestration |
| `ThemeService` | Theme state signal, toggle, localStorage persistence |

### Feature Services

| Service | API Base | Key Methods |
|---|---|---|
| `AuthApiService` | `/api/v1/auth` | `login()`, `refresh()`, `logout()` |
| `UserApiService` | `/api/v1/users` | `register()`, `getMe()`, `updateProfile()`, `changePassword()` |
| `WalletApiService` | `/api/v1/wallets` | `getAll()`, `getPaged()`, `getById()`, `create()`, `update()`, `delete()`, `setSpendingLimit()`, `removeSpendingLimit()`, `archive()`, `restore()`, `getTotals()` |
| `TransactionApiService` | `/api/v1/transactions` | `create()`, `getById()`, `getByWallet()`, `getAll()` (filtered), `update()`, `delete()` |
| `CategoryApiService` | `/api/v1/categories` | `create()`, `getAll()`, `getById()`, `getSubcategories()`, `delete()` |
| `AnalyticsApiService` | `/api/v1/analytics` | `getMonthly()`, `getByCategory()`, `getSavingsRate()`, `getTrend()`, `getWalletBreakdown()` |
| `AdminApiService` | `/api/v1/admin/users` | `listUsers()`, `getUser()`, `updateRoles()`, `deleteUser()` |

### HTTP Interceptors (`core/interceptors/`)

1. **`authInterceptor`** — Attach `Authorization: Bearer <token>` to non-public requests.
2. **`tokenRefreshInterceptor`** — Catch 401, attempt silent refresh, retry; redirect to `/login` on failure.
3. **`apiBaseUrlInterceptor`** — Prefix relative `/api/` calls with configured base URL via `InjectionToken<string>` (`API_BASE_URL`).

### State Approach
Angular **signals** + `computed()` — no NgRx. Each feature service holds local `signal()`-based state for caching. `AuthStateService` is the single source of truth for user state.

---

## 5. Routing & Guards

```
/                           → redirect to /dashboard
/login                      → LoginComponent                    [guestGuard]
/register                   → RegisterComponent                 [guestGuard]

/ (ShellComponent layout)                                       [authGuard]
├── /dashboard              → DashboardComponent
├── /wallets                → WalletListComponent
├── /wallets/:id            → WalletDetailComponent
├── /transactions           → TransactionListComponent
├── /transactions/new       → TransactionFormComponent
├── /transactions/:id/edit  → TransactionFormComponent
├── /categories             → CategoryListComponent
├── /analytics              → AnalyticsOverviewComponent
├── /profile                → ProfileComponent
├── /profile/password       → ChangePasswordComponent
├── /admin                  → AdminUserListComponent            [roleGuard('ROLE_ADMIN')]
├── /admin/users/:id        → AdminUserDetailComponent          [roleGuard('ROLE_ADMIN')]
└── **                      → NotFoundComponent
```

### Guards (`core/guards/`, functional `CanActivateFn`)

| Guard | Behavior |
|---|---|
| `authGuard` | Checks `AuthStateService.isAuthenticated()`, redirects to `/login` |
| `guestGuard` | Checks NOT authenticated, redirects to `/dashboard` |
| `roleGuard(role)` | Factory function checking `currentUser().roles.includes(role)` |

Each feature exports its own child `Routes` from a `*.routes.ts`, loaded via `loadChildren`.

---

## 6. Pages & Components

### Layout (`core/layout/`)

| Component | Description |
|---|---|
| `ShellComponent` | Full-page layout: sidebar + header + `<router-outlet>` |
| `SidebarComponent` | Collapsible nav with icons (Dashboard, Wallets, Transactions, Categories, Analytics, Admin*); active route highlighting |
| `HeaderComponent` | Page title (from route data), theme toggle, user avatar dropdown (profile, logout) |
| `MobileNavComponent` | Hamburger menu overlay for `<lg` breakpoints |

### Auth Feature

| Component | Description |
|---|---|
| `LoginComponent` | Email + password form, validation, link to register |
| `RegisterComponent` | Username + email + password + confirm form, link to login |

### Dashboard Feature

| Component | Description |
|---|---|
| `DashboardComponent` | Wallet totals cards, monthly KPI cards, 6-month trend mini line-chart, 5 recent transactions, quick-action buttons |

### Wallets Feature

| Component | Description |
|---|---|
| `WalletListComponent` | Grid of wallet cards (balance, currency, spending limit indicator); archived toggle; "Add Wallet" |
| `WalletDetailComponent` | Balance display, paginated transactions table, spending limit management, archive/restore |
| `WalletFormDialogComponent` | Modal: name, currency, initial balance |
| `SpendingLimitDialogComponent` | Modal: amount, period dropdown (DAILY/WEEKLY/MONTHLY) |

### Transactions Feature

| Component | Description |
|---|---|
| `TransactionListComponent` | Filterable (type, category, date range), sortable, paginated table; wallet selector |
| `TransactionFormComponent` | Full page: wallet dropdown, type toggle, amount, category/subcategory cascading dropdowns, description, date picker |

### Categories Feature

| Component | Description |
|---|---|
| `CategoryListComponent` | Two-column layout (INCOME / EXPENSE); parent→subcategory tree; add/delete |
| `CategoryFormDialogComponent` | Modal: name, optional parent, transaction type |

### Analytics Feature

| Component | Description |
|---|---|
| `AnalyticsOverviewComponent` | Month/year selector, optional wallet filter; hosts sub-components below |
| `MonthlyKpiCardsComponent` | Income, expenses, net savings, savings rate as colored KPI cards |
| `CategoryBreakdownChartComponent` | Doughnut/pie chart of spending by category |
| `TrendChartComponent` | Line chart: income, expenses, net savings over N months |
| `WalletBreakdownTableComponent` | Table comparing all wallets for selected month |

### Profile Feature

| Component | Description |
|---|---|
| `ProfileComponent` | Display + edit username/email |
| `ChangePasswordComponent` | Current password + new password + confirm form |

### Admin Feature

| Component | Description |
|---|---|
| `AdminUserListComponent` | Paginated user table, role badges, search |
| `AdminUserDetailComponent` | User info, role management (checkboxes/chips), save, delete with confirmation |

### Shared Components (`shared/components/`)

`ButtonComponent` · `CardComponent` · `ModalComponent` · `PaginationComponent` · `ToastService + ToastContainerComponent` · `SpinnerComponent` · `EmptyStateComponent` · `FormFieldComponent` · `BadgeComponent` · `ConfirmationDialogComponent` · `ChartWrapperComponent` · `DropdownComponent`

---

## 7. UI/UX Design System

### Color Palette (Tailwind CSS 4 `@theme`)

| Token | Light | Dark |
|---|---|---|
| Primary | Indigo-600 | Indigo-400 |
| Success | Emerald-500 | Emerald-400 |
| Danger | Rose-500 | Rose-400 |
| Warning | Amber-500 | Amber-400 |
| Background | White | Gray-950 |
| Card BG | Gray-50 | Gray-900 |
| Text Primary | Gray-900 | Gray-100 |
| Text Muted | Gray-500 | Gray-400 |
| Border | Gray-200 | Gray-700 |

### Typography
System font stack (`font-sans`); `text-sm` metadata · `text-base` body · `text-lg`/`text-xl` section titles · `text-2xl`/`text-3xl` page titles · `text-4xl` KPI numbers.

### Spacing
Consistent 4px grid; cards `p-6`; section gaps `gap-6`; page padding `px-6 py-8`.

### Cards
`rounded-xl`, `shadow-sm` (light) / `shadow-none` with border (dark), header/body/footer slots via content projection.

### Charts
**Chart.js** + **ng2-charts** — supports line, bar, doughnut with dark-mode–aware color binding.

### Responsive
Mobile-first; sidebar collapses to overlay at `<lg`; dashboard grid `grid-cols-1 md:grid-cols-2 xl:grid-cols-4`.

---

## 8. Testing Strategy

**Framework**: Vitest (already configured).

| Layer | What to Test | How |
|---|---|---|
| Services | Every API method (mock `HttpClient`); `AuthStateService` signal transitions; `ThemeService` toggle + localStorage | Vitest + `TestBed` |
| Interceptors | Header attachment; 401 retry; URL prefixing | Mock `HttpHandlerFn` chain |
| Guards | Block/allow based on auth state and roles | Mock `AuthStateService` signals |
| Components | Template bindings, user interactions (clicks, form submits) | `TestBed` + `fixture.nativeElement` |
| Pipes | Pure transform logic | Simple unit tests (no TestBed) |
| Shared | Input/output contracts; event emissions; conditional rendering | Component harness tests |

Co-located `.spec.ts` for every file. Target: **≥80%** coverage on services/guards, **≥70%** on components.

---

## 9. Implementation Phases

### Phase 1 — Foundation *(no backend needed)*
1. Create folder structure + all `core/models/` interfaces.
2. Set up theming in `styles.css` (`@theme`, dark variant), `ThemeService`, `ThemeToggleComponent`.
3. Build all shared components (Button, Card, Modal, Pagination, Toast, Spinner, EmptyState, FormField, Badge, ConfirmationDialog).
4. Build layout: `ShellComponent`, `SidebarComponent`, `HeaderComponent`, `MobileNavComponent`.
5. Set up routing skeleton with lazy loading and placeholder pages.
6. Write tests for shared components, `ThemeService`, pipes.

### Phase 2 — Auth & Core Services
1. Implement `TokenStorageService`, `AuthStateService`, `AuthApiService`, `UserApiService`.
2. Implement all 3 HTTP interceptors; register in `app.config.ts`.
3. Implement `authGuard`, `guestGuard`, `roleGuard`.
4. Build `LoginComponent` and `RegisterComponent`.
5. Wire header user dropdown with logout.
6. Tests for all auth services, interceptors, guards, and auth components.

### Phase 3 — Wallets & Categories
1. Implement `WalletApiService` and `CategoryApiService`.
2. Build wallet components: list, detail, form dialog, spending limit dialog.
3. Build category components: list, form dialog.
4. Tests for wallet and category services + components.

### Phase 4 — Transactions
1. Implement `TransactionApiService`.
2. Build `TransactionListComponent` (filters, pagination) and `TransactionFormComponent` (cascading categories).
3. Tests for transaction service + components.

### Phase 5 — Analytics & Dashboard
1. Install `chart.js` + `ng2-charts`; build `ChartWrapperComponent`.
2. Implement `AnalyticsApiService`.
3. Build analytics page: KPI cards, category breakdown chart, trend chart, wallet breakdown table.
4. Build `DashboardComponent` (wallet totals, monthly KPIs, trend mini-chart, recent transactions).
5. Tests for analytics service + all dashboard/analytics components.

### Phase 6 — Profile & Admin
1. Build `ProfileComponent` and `ChangePasswordComponent`.
2. Implement `AdminApiService`.
3. Build admin pages: user list, user detail with role management + delete.
4. Tests for profile and admin features.

### Phase 7 — Polish
1. Responsive QA across all breakpoints.
2. Accessibility audit (keyboard nav, ARIA labels, focus management on modals).
3. Empty states + loading skeletons for all data-fetching views.
4. Error pages (403, 404) and network error handling.
5. `NotFoundComponent` with wildcard route.

---

## Further Considerations

- **Environment config**: Use `InjectionToken<string>` for `API_BASE_URL` (more testable than `environment.ts`, aligns with Dependency Inversion).
- **Charts alternative**: Start with Chart.js; migrate to Apache ECharts (`ngx-echarts`) if richer interactivity is needed.
- **Real-time updates**: Backend uses RabbitMQ for inter-service events — a future phase could add WebSocket/SSE for live wallet balance updates. Leave room for `core/services/realtime.service.ts`.
