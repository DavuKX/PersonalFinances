# 💰 PersonalFinance

A full-stack **personal finance management platform** built with a microservices architecture. Track income and expenses across multiple wallets, set budgets and spending limits, visualize analytics, and manage users — all through a modern Angular frontend backed by Spring Boot services.

---

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker Compose (Recommended)](#run-with-docker-compose-recommended)
  - [Run Services Individually](#run-services-individually)
  - [Run the Frontend](#run-the-frontend)
- [API Reference](#-api-reference)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [AWS Production Deployment](#-aws-production-deployment)
- [CI/CD Pipelines](#-cicd-pipelines)
- [Testing](#-testing)
- [Environment Variables](#-environment-variables)

---

## 🏗 Architecture

```
                          ┌──────────────────────┐
                          │   Angular Frontend   │
                          │  (Port 4200 / S3)    │
                          └──────────┬───────────┘
                                     │
                          ┌──────────▼───────────┐
                          │     API Gateway      │
                          │   (Port 8080)        │
                          │  Spring Cloud GW     │
                          │  JWT Auth + Redis    │
                          └──┬─────┬────┬────┬───┘
                             │     │    │    │
              ┌──────────────┘     │    │    └──────────────┐
              ▼                    ▼    ▼                    ▼
     ┌────────────────┐ ┌──────────────────┐ ┌──────────────────┐
     │  User Service  │ │Transaction Service│ │ Wallet Service   │
     │  (Port 8081)   │ │   (Port 8083)    │ │  (Port 8082)     │
     │  Auth + Users  │ │ Txns + Categories│ │ Wallets + Budgets│
     └───────┬────────┘ └────────┬─────────┘ └────────┬─────────┘
             │                   │                     │
             │           ┌───────▼─────────┐           │
             │           │   RabbitMQ      │           │
             │           │  (Event Bus)    │◄──────────┘
             │           └───────┬─────────┘
             │                   │
             │           ┌───────▼─────────┐
             │           │Analytics Service│
             │           │  (Port 8084)    │
             │           └───────┬─────────┘
             │                   │
     ┌───────▼───────────────────▼─────────┐     ┌─────────┐
     │         PostgreSQL (4 DBs)          │     │  Redis  │
     │  userdb · transactiondb · walletdb  │     │ (Cache) │
     │           · analyticsdb             │     └─────────┘
     └─────────────────────────────────────┘
```

**Communication patterns:**
- **Synchronous** — REST via the API Gateway (client → gateway → service)
- **Asynchronous** — RabbitMQ events (transaction-service → wallet-service, analytics-service)
- **Token blocklist** — Redis shared between API Gateway and User Service

---

## 🧰 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Language |
| **Spring Boot** | 4.0.x | Microservice framework |
| **Spring Cloud Gateway** | 2025.0.x | API Gateway (reactive) |
| **Spring Data JPA** | — | Database access |
| **Spring Security** | — | Authentication & authorization |
| **Spring AMQP** | — | RabbitMQ messaging |
| **PostgreSQL** | 17 | Relational database |
| **Redis** | 7 | Token blocklist & session caching |
| **RabbitMQ** | 4.x | Asynchronous event bus |
| **JJWT** | 0.12.6 | JWT token generation & validation |
| **Maven** | — | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| **Angular** | 21 | SPA framework |
| **TypeScript** | 5.9 | Language |
| **Tailwind CSS** | 4.x | Utility-first styling |
| **Chart.js** | 4.x | Analytics charts |
| **RxJS** | 7.8 | Reactive state management |
| **Vitest** | 4.x | Unit testing |

### Infrastructure
| Technology | Purpose |
|---|---|
| **Docker / Docker Compose** | Local containerization |
| **Kubernetes** | Production orchestration |
| **Terraform** | Infrastructure as Code (AWS) |
| **GitHub Actions** | CI/CD pipelines |
| **AWS (EKS, RDS, ElastiCache, Amazon MQ, S3, CloudFront)** | Cloud hosting |

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication with access + refresh tokens
- Redis-backed token blocklist for secure logout
- Role-based access control (`ROLE_USER`, `ROLE_ADMIN`)
- API Gateway validates JWT and injects `X-User-Id` header into downstream requests
- Silent token refresh on 401 responses

### 👛 Wallet Management
- Create, update, and delete wallets with custom names and currencies
- Real-time balance tracking (auto-updated via transaction events)
- Spending limits (daily / weekly / monthly) with configurable thresholds
- Archive and restore wallets
- Wallet totals aggregation across all user wallets
- Paginated wallet listing with sort options

### 💸 Transaction Tracking
- Record income and expense transactions
- Assign transactions to wallets and categories/subcategories
- Filter by type, category, date range with pagination and sorting
- Wallet balances auto-update via RabbitMQ events
- Spending summary per wallet and time period

### 📂 Category Management
- Create hierarchical categories (parent → subcategory)
- Separate category trees for income and expense types
- System-wide default categories + user-created custom categories

### 📊 Budgets (50/30/20 Rule)
- Per-wallet, per-category budget allocation
- Fixed amount or percentage-based budgets
- Bulk budget allocation wizard (e.g., 50% Needs / 30% Wants / 20% Savings)
- Real-time spending tracking against budget limits via RabbitMQ events
- Budget summary with spent amount, remaining amount, and percent used

### 📈 Analytics & Insights
- Monthly financial overview (total income, expenses, net savings)
- Category-wise spending breakdown
- Savings rate calculation
- Income vs. expense trend charts (configurable N-month window)
- Per-wallet breakdown comparison

### 👤 User Profile
- View and edit username/email
- Change password with current password verification

### 🛡 Admin Panel
- Paginated user management (list, search, sort)
- View user details and modify roles
- Delete user accounts
- Protected by `ROLE_ADMIN` guard

### 🎨 Frontend UI/UX
- **Light & Dark mode** with system preference detection and localStorage persistence
- Responsive design — mobile sidebar overlay, desktop fixed sidebar
- Shared component library: Button, Card, Modal, Pagination, Toast notifications, Spinner, Empty state, Form fields, Badge, Confirmation dialog, Chart wrapper, Dropdown
- Lazy-loaded feature modules for optimal bundle size
- Custom pipes: currency formatting, relative dates, text truncation

---

## 📁 Project Structure

```
PersonalFinances/
├── api-gateway/              # Spring Cloud Gateway — JWT auth, routing, Redis blocklist
├── user-service/             # User registration, login, profile, admin endpoints
├── transaction-service/      # Transactions, categories, spending summaries
├── wallet-service/           # Wallets, spending limits, budgets, event-driven balance updates
├── analytics-service/        # Aggregated analytics consumed from RabbitMQ events
├── personal-finance-frontend/# Angular 21 SPA with Tailwind CSS
├── infra/                    # Database init scripts
├── k8s/                      # Kubernetes manifests (deployments, services, configmaps, secrets, ingress)
├── terraform/                # AWS infrastructure as code (EKS, RDS, ElastiCache, MQ, S3, CloudFront)
├── docs/                     # Architecture docs and feature plans
└── docker-compose.yml        # Local development environment
```

### Backend Service Architecture (per service)

Each microservice follows a **clean architecture / hexagonal pattern**:

```
src/main/java/com/personalfinance/<service>/
├── domain/
│   ├── model/          # Immutable domain entities and value objects
│   ├── port/           # Repository interfaces (driven ports)
│   └── exception/      # Domain-specific exceptions
├── application/
│   ├── dto/            # Commands, queries, and DTOs
│   ├── usecase/        # Use case interfaces (driving ports)
│   └── service/        # Application service implementations
├── infrastructure/
│   ├── persistence/    # JPA entities, repositories, mappers, adapters
│   ├── messaging/      # RabbitMQ listeners and event DTOs
│   ├── config/         # Spring configuration classes
│   └── security/       # Security filters and JWT utilities
└── presentation/
    ├── controller/     # REST controllers
    ├── request/        # Request DTOs with Jakarta validation
    └── response/       # Response DTOs
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Required For |
|---|---|---|
| **Docker** & **Docker Compose** | ≥ 24 | Running the full stack locally |
| **JDK** | 21 | Building/running backend services |
| **Maven** | ≥ 3.9 (or use included `mvnw`) | Building backend services |
| **Node.js** | ≥ 22 | Building/running the frontend |
| **npm** | ≥ 11 | Frontend dependency management |

### Run with Docker Compose (Recommended)

The fastest way to get everything running:

```bash
# Clone the repository
git clone https://github.com/<your-org>/PersonalFinances.git
cd PersonalFinances

# Start all services (PostgreSQL, Redis, RabbitMQ, all microservices)
docker compose up --build
```

This will start:

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| User Service | http://localhost:8081 |
| Wallet Service | http://localhost:8082 |
| Transaction Service | http://localhost:8083 |
| Analytics Service | http://localhost:8084 |
| PostgreSQL | localhost:5433 |
| Redis | localhost:6379 |
| RabbitMQ Management | http://localhost:15672 (user/password) |

Then start the frontend separately:

```bash
cd personal-finance-frontend
npm install
npm start
# → http://localhost:4200
```

### Run Services Individually

If you prefer to run services outside of Docker (e.g., for development):

**1. Start infrastructure:**

```bash
# Start only PostgreSQL, Redis, and RabbitMQ
docker compose up postgres redis rabbitmq
```

**2. Run each backend service:**

```bash
# In separate terminals:
cd user-service && ./mvnw spring-boot:run
cd wallet-service && ./mvnw spring-boot:run
cd transaction-service && ./mvnw spring-boot:run
cd analytics-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

**3. Run the frontend:**

```bash
cd personal-finance-frontend
npm install
npm start
```

### Run the Frontend

```bash
cd personal-finance-frontend

# Install dependencies
npm install

# Development server (with hot reload)
npm start
# → http://localhost:4200

# Production build
npm run build

# Run tests
npm test
```

---

## 📡 API Reference

All API requests go through the **API Gateway** at `http://localhost:8080`. Authenticated endpoints require an `Authorization: Bearer <token>` header.

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Login with email + password | ❌ |
| `POST` | `/api/v1/auth/refresh` | Refresh access token | ❌ |
| `POST` | `/api/v1/auth/logout` | Logout (blocklist token) | ✅ |

### Users (`/api/v1/users`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/users/register` | Register a new user | ❌ |
| `GET` | `/api/v1/users/me` | Get current user profile | ✅ |
| `PUT` | `/api/v1/users/{id}` | Update profile | ✅ |
| `PATCH` | `/api/v1/users/{id}/password` | Change password | ✅ |

### Wallets (`/api/v1/wallets`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/wallets` | Create a wallet | ✅ |
| `GET` | `/api/v1/wallets` | List all wallets | ✅ |
| `GET` | `/api/v1/wallets/paged` | List wallets (paginated) | ✅ |
| `GET` | `/api/v1/wallets/{id}` | Get wallet by ID | ✅ |
| `PUT` | `/api/v1/wallets/{id}` | Update wallet | ✅ |
| `DELETE` | `/api/v1/wallets/{id}` | Delete wallet | ✅ |
| `PUT` | `/api/v1/wallets/{id}/spending-limit` | Set spending limit | ✅ |
| `DELETE` | `/api/v1/wallets/{id}/spending-limit` | Remove spending limit | ✅ |
| `POST` | `/api/v1/wallets/{id}/archive` | Archive wallet | ✅ |
| `POST` | `/api/v1/wallets/{id}/restore` | Restore wallet | ✅ |
| `GET` | `/api/v1/wallets/totals` | Get aggregated totals | ✅ |

### Budgets (`/api/v1/wallets/{walletId}/budgets`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/wallets/{walletId}/budgets` | Create a budget | ✅ |
| `GET` | `/api/v1/wallets/{walletId}/budgets` | List budgets with spending summary | ✅ |
| `GET` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Get budget detail | ✅ |
| `PUT` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Update a budget | ✅ |
| `DELETE` | `/api/v1/wallets/{walletId}/budgets/{budgetId}` | Delete a budget | ✅ |
| `PUT` | `/api/v1/wallets/{walletId}/budgets/bulk` | Bulk set budgets (e.g., 50/30/20) | ✅ |

### Transactions (`/api/v1/transactions`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/transactions` | Create a transaction | ✅ |
| `GET` | `/api/v1/transactions` | List transactions (filtered, paginated) | ✅ |
| `GET` | `/api/v1/transactions/{id}` | Get transaction by ID | ✅ |
| `PUT` | `/api/v1/transactions/{id}` | Update transaction | ✅ |
| `DELETE` | `/api/v1/transactions/{id}` | Delete transaction | ✅ |
| `GET` | `/api/v1/wallets/{walletId}/transactions` | Get transactions by wallet | ✅ |
| `GET` | `/api/v1/wallets/{walletId}/spending-summary` | Spending summary for a period | ✅ |

### Categories (`/api/v1/categories`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/categories` | Create a category | ✅ |
| `GET` | `/api/v1/categories` | List categories (optionally by type) | ✅ |
| `GET` | `/api/v1/categories/{id}` | Get category by ID | ✅ |
| `GET` | `/api/v1/categories/{id}/subcategories` | Get subcategories | ✅ |
| `DELETE` | `/api/v1/categories/{id}` | Delete category | ✅ |

### Analytics (`/api/v1/analytics`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/v1/analytics/monthly` | Monthly financial overview | ✅ |
| `GET` | `/api/v1/analytics/by-category` | Spending breakdown by category | ✅ |
| `GET` | `/api/v1/analytics/savings-rate` | Savings rate | ✅ |
| `GET` | `/api/v1/analytics/trend` | Income/expense trend over N months | ✅ |
| `GET` | `/api/v1/analytics/wallet-breakdown` | Per-wallet breakdown | ✅ |

### Admin (`/api/v1/admin/users`) — Requires `ROLE_ADMIN`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/v1/admin/users` | List all users (paginated) | ✅ 🛡 |
| `GET` | `/api/v1/admin/users/{id}` | Get user details | ✅ 🛡 |
| `PUT` | `/api/v1/admin/users/{id}/roles` | Update user roles | ✅ 🛡 |
| `DELETE` | `/api/v1/admin/users/{id}` | Delete user | ✅ 🛡 |

---

## ☸ Kubernetes Deployment

The `k8s/` directory contains all manifests for deploying to a Kubernetes cluster:

```bash
# Create namespace
kubectl apply -f k8s/namespace.yml

# Apply secrets (for local/staging — production uses External Secrets)
kubectl apply -f k8s/secrets/

# Apply configmaps
kubectl apply -f k8s/configmaps/

# Deploy services
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/

# Apply ingress
kubectl apply -f k8s/ingress/

# (Production) Apply external secrets
kubectl apply -f k8s/external-secrets/
```

---

## ☁ AWS Production Deployment

The project includes a complete **Terraform** setup for AWS production deployment. See [`docs/aws-deployment.md`](docs/aws-deployment.md) for the full guide.

### Quick Start

```bash
# 1. Bootstrap Terraform backend (one-time)
./terraform/bootstrap.sh

# 2. Configure variables
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values

# 3. Provision infrastructure
terraform init
terraform plan
terraform apply

# 4. Tear down (when done)
./terraform/teardown.sh
```

### What Terraform Creates

| Resource | Service |
|---|---|
| **VPC** | Public, private, and database subnets |
| **EKS Cluster** | Managed Kubernetes with node groups |
| **ECR** | Docker image registries per microservice |
| **RDS PostgreSQL** | 4 databases (userdb, transactiondb, walletdb, analyticsdb) |
| **ElastiCache Redis** | Token blocklist and session caching |
| **Amazon MQ** | RabbitMQ broker for async events |
| **S3 + CloudFront** | Frontend hosting with CDN |
| **AWS Secrets Manager** | DB, RabbitMQ, and JWT secrets |
| **External Secrets Operator** | Syncs AWS secrets → K8s secrets |
| **AWS Load Balancer Controller** | ALB Ingress for the EKS cluster |
| **IAM OIDC Provider** | Passwordless GitHub Actions CI/CD |

---

## 🔄 CI/CD Pipelines

The project uses **GitHub Actions** for automated deployments:

### `deploy-backend.yml`
- **Trigger:** Push to `main` that changes any service directory or `k8s/`
- **Flow:** Detect changed services → Build & test → Push Docker image to ECR → Deploy K8s manifests
- Uses **matrix strategy** to only rebuild changed services

### `deploy-frontend.yml`
- **Trigger:** Push to `main` that changes `personal-finance-frontend/`
- **Flow:** Install deps → Build Angular → Sync to S3 → Invalidate CloudFront cache

### `terraform.yml`
- **Trigger:** Push to `main` that changes `terraform/`, or manual dispatch
- **Flow:** Init → Validate → Plan → Apply (on main) or Destroy (manual)

---

## 🧪 Testing

### Backend

Each service includes unit and integration tests:

```bash
# Run tests for a specific service
cd user-service && ./mvnw test
cd transaction-service && ./mvnw test
cd wallet-service && ./mvnw test
cd analytics-service && ./mvnw test
```

- **Unit tests** — Service layer, mappers, domain logic
- **Integration tests** — `@SpringBootTest` with `MockMvc` and H2 in-memory database
- Tests cover: CRUD operations, auth flows, event handling, validation, error scenarios

### Frontend

```bash
cd personal-finance-frontend
npm test
```

- **Framework:** Vitest with Angular TestBed
- **Coverage:** Services, guards, interceptors, components, pipes
- Mocked HTTP calls and service dependencies

---

## ⚙ Environment Variables

### Docker Compose Defaults

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_USER` | `postgres` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `password` | PostgreSQL password |
| `RABBITMQ_DEFAULT_USER` | `user` | RabbitMQ username |
| `RABBITMQ_DEFAULT_PASS` | `password` | RabbitMQ password |
| `JWT_SECRET` | (dev key) | JWT signing key (≥256 bits) |
| `JWT_EXPIRATION_MS` | `900000` (15 min) | Access token lifetime |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 days) | Refresh token lifetime |

### Service Ports

| Service | Port |
|---|---|
| API Gateway | 8080 |
| User Service | 8081 |
| Wallet Service | 8082 |
| Transaction Service | 8083 |
| Analytics Service | 8084 |
| PostgreSQL | 5433 (mapped from 5432) |
| Redis | 6379 |
| RabbitMQ (AMQP) | 5672 |
| RabbitMQ (Management UI) | 15672 |
| Angular Frontend | 4200 |

---

## 📄 License

This project is for educational and personal use.

