# AWS Production Deployment Guide — PersonalFinance

This project uses **Terraform** for infrastructure-as-code, **GitHub Actions** for CI/CD,
**AWS Secrets Manager** (via External Secrets Operator) for secrets, and **S3 + CloudFront** for the frontend.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CloudFront                              │
│                   (frontend + /api/* proxy)                     │
└──────┬──────────────────────────────┬───────────────────────────┘
       │ static assets                │ /api/*
       ▼                              ▼
   S3 Bucket                    ALB Ingress
 (Angular SPA)                       │
                                     ▼
                              ┌─── EKS Cluster ───────────────┐
                              │  api-gateway (2 replicas)      │
                              │  user-service (2 replicas)     │
                              │  transaction-service (2 reps)  │
                              │  wallet-service (2 replicas)   │
                              │  analytics-service (2 reps)    │
                              │                                │
                              │  External Secrets Operator     │
                              │  AWS LB Controller             │
                              └──┬──────────┬──────────┬───────┘
                                 │          │          │
                      ┌──────────┘          │          └──────────┐
                      ▼                     ▼                     ▼
               RDS PostgreSQL        ElastiCache Redis      Amazon MQ
              (4 databases)          (session/cache)       (RabbitMQ)
```

---

## Prerequisites

| Tool        | Version  | Purpose                          |
|-------------|----------|----------------------------------|
| AWS CLI     | ≥ 2.x    | AWS operations                   |
| Terraform   | ≥ 1.5    | Infrastructure provisioning      |
| kubectl     | ≥ 1.28   | Kubernetes management            |
| Docker      | ≥ 24     | Building container images        |
| Node.js     | ≥ 22     | Frontend build (CI only)         |
| JDK         | 21       | Backend builds (CI only)         |

---

## 1. Bootstrap Terraform Backend (one-time)

Creates the S3 bucket and DynamoDB table for Terraform remote state:

```bash
./terraform/bootstrap.sh
```

## 2. Configure Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your desired values
```

## 3. Provision Infrastructure

```bash
cd terraform
terraform init
terraform plan          # review what will be created
terraform apply         # provision everything
```

This creates:
- **VPC** with public, private, and database subnets
- **EKS cluster** with managed node group
- **ECR repositories** for each microservice
- **RDS PostgreSQL** (with 4 databases: userdb, transactiondb, walletdb, analyticsdb)
- **ElastiCache Redis** cluster
- **Amazon MQ** RabbitMQ broker
- **S3 bucket + CloudFront** distribution for the Angular frontend
- **AWS Secrets Manager** secrets (DB, RabbitMQ, JWT)
- **External Secrets Operator** (syncs AWS secrets → K8s secrets)
- **AWS Load Balancer Controller** (for ALB Ingress)
- **IAM OIDC provider** for GitHub Actions (passwordless CI/CD)

## 4. Configure GitHub Secrets

After `terraform apply`, set these GitHub repository secrets:

| Secret                         | Value (from Terraform output)                       |
|-------------------------------|-----------------------------------------------------|
| `AWS_ROLE_ARN`                | `terraform output github_actions_role_arn`           |
| `EKS_CLUSTER_NAME`           | `terraform output eks_cluster_name`                  |
| `RDS_ADDRESS`                | `terraform output rds_address`                       |
| `REDIS_ENDPOINT`             | `terraform output redis_endpoint`                    |
| `MQ_ENDPOINT`                | extracted from `terraform output mq_broker_endpoints`|
| `FRONTEND_BUCKET`            | `terraform output frontend_bucket_name`              |
| `CLOUDFRONT_DISTRIBUTION_ID` | `terraform output cloudfront_distribution_id`        |

> **Tip:** Also restrict the GitHub Actions OIDC trust to your specific repo.
> Edit `terraform/iam.tf` → change `repo:*:ref:refs/heads/main` to
> `repo:<your-org>/<your-repo>:ref:refs/heads/main`.

## 5. First Deployment

Push to `main` to trigger the pipelines, or run them manually:

```bash
# Deploy backend services
gh workflow run deploy-backend.yml

# Deploy frontend
gh workflow run deploy-frontend.yml
```

## 6. Verify

```bash
# Update kubeconfig
aws eks update-kubeconfig --name $(terraform -chdir=terraform output -raw eks_cluster_name)

# Check pods
kubectl get pods -n personalfinance

# Check services
kubectl get svc -n personalfinance

# Check ingress (ALB URL)
kubectl get ingress -n personalfinance

# Frontend URL
terraform -chdir=terraform output cloudfront_domain_name
```

---

## CI/CD Pipelines

### `.github/workflows/deploy-backend.yml`
- **Trigger:** push to `main` that changes any service directory or `k8s/`
- **Steps:** detect changed services → build & test → push Docker image to ECR → deploy K8s manifests to EKS
- Uses **matrix strategy** — only rebuilds services that actually changed

### `.github/workflows/deploy-frontend.yml`
- **Trigger:** push to `main` that changes `personal-finance-frontend/`
- **Steps:** install deps → build Angular → sync to S3 → invalidate CloudFront

### `.github/workflows/terraform.yml`
- **Trigger:** push to `main` that changes `terraform/`, or manual dispatch
- **Steps:** init → validate → plan → apply (on main) or destroy (manual)
- Posts plan output as a PR comment on pull requests

---

## Secrets Management

Secrets flow: **Terraform** → **AWS Secrets Manager** → **External Secrets Operator** → **K8s Secrets**

The following K8s secrets are automatically synced from AWS Secrets Manager:

| K8s Secret          | AWS Secret Path                                  | Keys                              |
|--------------------|--------------------------------------------------|-----------------------------------|
| `db-secrets`       | `personalfinance/prod/db-credentials`            | `DB_USERNAME`, `DB_PASSWORD`      |
| `rabbitmq-secrets` | `personalfinance/prod/rabbitmq-credentials`      | `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` |
| `jwt-secret`       | `personalfinance/prod/jwt-secret`                | `JWT_SECRET`                      |

The old `k8s/secrets/` YAML files are no longer applied in production.

---

## Tear Down Infrastructure

To completely destroy all AWS resources:

```bash
./terraform/teardown.sh          # interactive — asks for confirmation
./terraform/teardown.sh --auto   # non-interactive
```

Or via GitHub Actions:
1. Go to **Actions → Terraform → Run workflow**
2. Select action: **destroy**

> ⚠️ This permanently deletes all data (RDS, Redis, S3). Back up first!

The Terraform state bucket and DynamoDB lock table are NOT deleted automatically.
Remove them manually if done for good:

```bash
aws s3 rb s3://personalfinance-terraform-state --force
aws dynamodb delete-table --table-name personalfinance-terraform-lock
```

---

## Production Hardening Checklist

- [ ] Restrict `iam.tf` OIDC trust to your exact GitHub repo
- [ ] Set `rds_multi_az = true` for database high availability
- [ ] Set `deletion_protection = true` on RDS
- [ ] Switch from `single_nat_gateway` to `one_nat_gateway_per_az` for HA
- [ ] Add a custom domain + ACM certificate for CloudFront
- [ ] Replace `JPA_DDL_AUTO: validate` with Flyway/Liquibase migrations
- [ ] Configure Horizontal Pod Autoscalers (HPA) per service
- [ ] Enable CloudWatch Container Insights on EKS
- [ ] Set up Prometheus + Grafana for metrics dashboards
- [ ] Configure WAF on the ALB for API protection
- [ ] Enable RDS automated backups with longer retention
- [ ] Set `eks_cluster_endpoint_public_access = false` and use a bastion/VPN
